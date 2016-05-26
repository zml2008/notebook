package xyz.aoeu.notebook;

import rx.Observable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface NotebookStorage {
    /**
     * Get a stream of notes owned by a specific user ID.
     *
     * This method may populate notes asynchronously.
     *
     * @param owner The owner of the notes to get
     * @return A stream of notes owned by a specific user
     */
    Observable<Note> getNotes(UUID owner);

    /**
     * Get a stream of all notes.
     * This method may return a stream that is populated asynchronously.
     *
     * @return All currently stored notes
     */
    Observable<Note> getAllNotes();

    /**
     * Get all UUIDS that currently have notes
     *
     * This method may return a stream that is populated asynchronously
     * @return A stream of all UUIDs that have stored notes
     */
    Observable<UUID> getKnownOwners();

    /**
     * Get a note that may exist.
     *
     * @param id The id of the note to get
     * @return A future containing the returned note if a note exists with the given ID
     */
    CompletableFuture<Optional<Note>> getNote(int id);

    /**
     * Insert a new note into the database. The ID currently contained in the note will be ignored, and the returned Note object will be a different object, containing the new ID of the note.
     *
     * @param note The note to insert
     * @return A future with the inserted note object.
     */
    CompletableFuture<Note> insertNote(Note note);

    /**
     * Update an existing note in the database. If no note exists, the returned future will fail.
     *
     * @param note The note to update
     * @return A future returning the update note object. Will probably be the same as the provided note object
     */
    CompletableFuture<Note> updateNote(Note note);

    /**
     * Remove a note that may exist from the note database with the same ID, owner, and contents as the provided note
     *
     * @param note The note to remove
     * @return A future that will complete once the note has been removed, or return no value if no note was removed.
     */
    CompletableFuture<Optional<Note>> removeNote(Note note);


    /**
     * Remove the note with the given ID from the database. If this note does not exist, an empty {@link Optional} will be returned.
     *
     * @see #removeNote(Note) which is preferred if a note object is already known to prevent accidentally removing a note that is concurrently updated.
     * @param noteID The note ID to remove
     * @return A future that will complete once the note has been removed.
     */
    CompletableFuture<Optional<Note>> removeNote(int noteID);
}
