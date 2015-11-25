Notebook is a simple plugin that exists as a test of Sponge's SQL capabilities.

It's a simple note-taking plugin that stores a player's notes in an SQL database.

Commands
--------

- `/notes [player]`

    List notes, provide clickable links to delete notes
    
    **Permission:** `notebook.view.own` for self, `notebook.view.other.<player>` to view the notes of a different player

- `/note [-e <id>] [-p <other>]`

    Create a new note or edit an existing one
    
    **Permission:** `notebook.create.own` or `notebook.edit.own` to edit own note, or `notebook.create.other.<player>` or `notebook.edit.other.<player>`

- `/note -b [-p <other>]`

    Create a note from the contents of the currently held book
    
    **Permission:** `notebook.create.book.own` or `notebook.create.book.other.<player>`
