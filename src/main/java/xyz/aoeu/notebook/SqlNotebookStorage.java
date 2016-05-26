package xyz.aoeu.notebook;

import com.github.davidmoten.rx.jdbc.ConnectionProviderFromDataSource;
import com.github.davidmoten.rx.jdbc.Database;
import com.google.common.io.Resources;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import javax.sql.DataSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * An implementation of {@link NotebookStorage} using an SQL database
 */
public class SqlNotebookStorage implements NotebookStorage {
    private static final List<String> DEPLOY_DATA;

    static {
        try {
            DEPLOY_DATA = Resources.readLines(SqlNotebookStorage.class.getResource("db/schema.sql"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final DataSource db;
    private final Executor asyncExecutor;
    private final Scheduler sched;
    private final Database.Builder dbBuilder;

    public SqlNotebookStorage(DataSource db, Executor asyncExecutor) {
        this.db = db;
        this.asyncExecutor = asyncExecutor;
        this.sched = Schedulers.from(asyncExecutor);
        performTaskAsynchronously(conn -> {
            DatabaseMetaData md = conn.getMetaData();
            if (!md.getTables(null, null, "notes", null).next()) {
                conn.setAutoCommit(false);
                for (String line : DEPLOY_DATA) {
                    conn.prepareStatement(line).execute();
                }
                conn.commit();
            }
        });
        this.dbBuilder = Database.builder()
                .connectionProvider(new ConnectionProviderFromDataSource(this.db))
                .nonTransactionalScheduler(() -> sched);
    }

    private <T> CompletableFuture<T> performTaskAsynchronously(FailableFunction<Connection, T, SQLException> action) {
        CompletableFuture<T> ret = new CompletableFuture<>();
        asyncExecutor.execute(() -> {
            try (Connection conn = this.db.getConnection()) {
                ret.complete(action.apply(conn));
            } catch (Exception e) {
                ret.completeExceptionally(e);
            }
        });
        return ret;
    }

    private CompletableFuture<Void> performTaskAsynchronously(FailableConsumer<Connection, SQLException> action) {
        CompletableFuture<Void> ret = new CompletableFuture<>();
        asyncExecutor.execute(() -> {
            try (Connection conn = this.db.getConnection()) {
                action.accept(conn);
                ret.complete(null);
            } catch (Exception e) {
                ret.completeExceptionally(e);
            }
        });
        return ret;
    }

    private Database openStatement() {
        return this.dbBuilder.build();
    }

    /**
     * Get a stream of notes owned by a specific user ID.
     * <p/>
     * This method may populate notes asynchronously.
     *
     * @param owner The owner of the notes to get
     * @return A stream of notes owned by a specific user
     */
    @Override
    public Observable<Note> getNotes(UUID owner) {
        return openStatement()
                .select("SELECT id, contents FROM notes WHERE owner=?")
                .parameter(owner.toString())
                .get(rs -> {
                    Note note = new Note(owner, rs.getString(2));
                    note.setId(rs.getInt(1));
                    return note;
                });
    }

    /*static byte[] uuidToBytes(UUID id) {
        long msb = id.getMostSignificantBits();
        long lsb = id.getLeastSignificantBits();
        return new byte[] {
                // MSB
                (byte) ((msb >> 0x38) & 0xFF),
                (byte) ((msb >> 0x30) & 0xFF),
                (byte) ((msb >> 0x28) & 0xFF),
                (byte) ((msb >> 0x20) & 0xFF),
                (byte) ((msb >> 0x18) & 0xFF),
                (byte) ((msb >> 0x10) & 0xFF),
                (byte) ((msb >> 0x08) & 0xFF),
                (byte) (msb & 0xFF),
                // LSB
                (byte) ((lsb >> 0x38) & 0xFF),
                (byte) ((lsb >> 0x30) & 0xFF),
                (byte) ((lsb >> 0x28) & 0xFF),
                (byte) ((lsb >> 0x20) & 0xFF),
                (byte) ((lsb >> 0x18) & 0xFF),
                (byte) ((lsb >> 0x10) & 0xFF),
                (byte) ((lsb >> 0x08) & 0xFF),
                (byte) (lsb & 0xFF)};
    }

    static UUID bytesToUUID(byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Input must be 16 bytes long");
        }

        return new UUID((long) bytes[0] << 0x38
                | (long) bytes[1] << 0x30
                | (long) bytes[2] << 0x28
                | (long) bytes[3] << 0x20
                | (long) bytes[4] << 0x18
                | (long) bytes[5] << 0x10
                | (long) bytes[6] << 0x08
                | (long) bytes[7],
                (long) bytes[8] << 0x38
                        | (long) bytes[9] << 0x30
                        | (long) bytes[10] << 0x28
                        | (long) bytes[11] << 0x20
                        | (long) bytes[12] << 0x18
                        | (long) bytes[13] << 0x10
                        | (long) bytes[14] << 0x08
                        | (long) bytes[15]);

    }*/

    /**
     * Get a stream of all notes.
     * This method may return a stream that is populated asynchronously.
     *
     * @return All currently stored notes
     */
    @Override
    public Observable<Note> getAllNotes() {
        return openStatement()
                .select("SELECT id, owner, contents FROM notes")
                .get(rs -> {
                    Note note = new Note(UUID.fromString(rs.getString(2)), rs.getString(3));
                    note.setId(rs.getInt(1));
                    return note;
                });
    }

    /**
     * Get all UUIDS that currently have notes
     * <p/>
     * This method may return a stream that is populated asynchronously
     *
     * @return A stream of all UUIDs that have stored notes
     */
    @Override
    public Observable<UUID> getKnownOwners() {
        return openStatement()
                .select("SELECT DISTINCT owner FROM notes")
                .get(rs -> UUID.fromString(rs.getString(1)));
    }

    /**
     * Get a note that may exist.
     *
     * @param id The id of the note to get
     * @return A future containing the returned note if a note exists with the given ID
     */
    @Override
    public CompletableFuture<Optional<Note>> getNote(int id) {
        return performTaskAsynchronously(conn -> {
            PreparedStatement stmt = conn.prepareStatement("SELECT (owner, contents) FROM notes WHERE id=?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            } else {
                Note ret = new Note(UUID.fromString(rs.getString(1)), rs.getString(2));
                ret.setId(id);
                return Optional.of(ret);
            }
        });
    }

    /**
     * Insert a new note into the database. The ID currently contained in the note will be ignored, and the returned Note object will be a different object, containing the new ID of the note.
     *
     * @param note The note to insert
     * @return A future with the inserted note object.
     */
    @Override
    public CompletableFuture<Note> insertNote(Note note) {
        return performTaskAsynchronously(conn -> {
            PreparedStatement stmt = conn.prepareStatement("INSERT (owner, contents) INTO notes VALUES (?, ?); CALL IDENTITY()");
            stmt.setString(1, note.getOwner().toString());
            stmt.setString(2, note.getContents());

            ResultSet rs = stmt.executeQuery();
            rs.next();
            int id = rs.getInt(1);
            return note.withId(id);
        });
    }

    /**
     * Update an existing note in the database. If no note exists, the returned future will fail.
     *
     * @param note The note to update
     * @return A future returning the update note object. Will probably be the same as the provided note object
     */
    @Override
    public CompletableFuture<Note> updateNote(Note note) {
        return performTaskAsynchronously(conn -> {
            PreparedStatement stmt = conn.prepareStatement("UPDATE notes SET contents=?, owner=? WHERE id=?");
            stmt.setString(1, note.getContents());
            stmt.setString(2, note.getOwner().toString());
            stmt.setInt(3, note.getId());
            stmt.execute();
            return note;
        });
    }

    /**
     * Remove a note that may exist from the note database with the same ID, owner, and contents as the provided note
     *
     * @param note The note to remove
     * @return A future that will complete once the note has been removed, or return no value if no note was removed.
     */
    @Override
    public CompletableFuture<Optional<Note>> removeNote(Note note) {
        /*return performTaskAsynchronously(conn -> {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM notes WHERE id=? AND contents=? AND owner=?");

        });*/
        return null;
    }

    /**
     * Remove the note with the given ID from the database. If this note does not exist, an empty {@link Optional} will be returned.
     *
     * @param noteID The note ID to remove
     * @return A future that will complete once the note has been removed.
     * @see #removeNote(Note) which is preferred if a note object is already known to prevent accidentally removing a note that is concurrently updated.
     */
    @Override
    public CompletableFuture<Optional<Note>> removeNote(int noteID) {
        /*return performTaskAsynchronously(conn -> {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM notes WHERE id=?");
            stmt.setInt(1, noteID);

        });*/
        return null;
    }
}
