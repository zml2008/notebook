package xyz.aoeu.notebook;


import com.google.common.io.Resources;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import java.util.function.Function;
import java.util.stream.Stream;

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

    public SqlNotebookStorage(DataSource db, Executor asyncExecutor) {
        this.db = db;
        this.asyncExecutor = asyncExecutor;
        performTaskAsynchronously(conn -> {
            DatabaseMetaData md;
            try {
                md = conn.getMetaData();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (!md.getTables(null, null, "notes", null).next()) {
                conn.setAutoCommit(false);
                for (String line : DEPLOY_DATA) {
                    conn.prepareStatement(line).execute();
                }
                conn.commit();
            }
            return null;
        });
    }

    <T> CompletableFuture<T> performTaskAsynchronously(Function<Connection, T> action) {
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

    /**
     * Get a stream of notes owned by a specific user ID.
     * <p/>
     * This method may populate notes asynchronously.
     *
     * @param owner The owner of the notes to get
     * @return A stream of notes owned by a specific user
     */
    @Override
    public Stream<Note> getNotes(UUID owner) {
        return performTaskAsynchronously(conn -> {
            PreparedStatement stmt = conn.prepareStatement("SELECT (id, owner, contents) FROM notes WHERE owner=?");
            stmt.setBytes(1, owner.getLeastSignificantBits();
            ResultSet rs = stmt.executeQuery();
        });
    }

    private byte[] uuidToBytes(UUID id) {

    }

    /**
     * Get a stream of all notes.
     * This method may return a stream that is populated asynchronously.
     *
     * @return All currently stored notes
     */
    @Override
    public Stream<Note> getAllNotes() {
        return null;
    }

    /**
     * Get all UUIDS that currently have notes
     * <p/>
     * This method may return a stream that is populated asynchronously
     *
     * @return A stream of all UUIDs that have stored notes
     */
    @Override
    public Stream<UUID> getKnownOwners() {
        return null;
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
            conn.prepareStatement("")
        })
        return null;
    }

    /**
     * Insert a new note into the database. The ID currently contained in the note will be ignored, and the returned Note object will be a different object, containing the new ID of the note.
     *
     * @param note The note to insert
     * @return A future with the inserted note object.
     */
    @Override
    public CompletableFuture<Note> insertNote(Note note) {
        return null;
    }

    /**
     * Update an existing note in the database. If no note exists, the returned future will fail.
     *
     * @param note The note to update
     * @return A future returning the update note object. Will probably be the same as the provided note object
     */
    @Override
    public CompletableFuture<Note> updateNote(Note note) {
        return null;
    }

    /**
     * Remove a note that may exist from the note database with the same ID, owner, and contents as the provided note
     *
     * @param note The note to remove
     * @return A future that will complete once the note has been removed, or return no value if no note was removed.
     */
    @Override
    public CompletableFuture<Optional<Note>> removeNote(Note note) {
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
        return null;
    }
}
