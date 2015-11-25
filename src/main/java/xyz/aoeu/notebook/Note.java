package xyz.aoeu.notebook;

import java.util.UUID;

public class Note {
    private int id = Integer.MIN_VALUE;
    private final UUID owner;
    private final String contents;

    public Note(UUID owner, String contents) {
        this.owner = owner;
        this.contents = contents;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getContents() {
        return contents;
    }

    public Note withOwner(UUID owner) {
        return new Note(owner, this.contents);
    }

    public Note withContents(String contents) {
        return new Note(this.owner, contents);
    }

    public int getId() {
        return id;
    }

    /**
     * Used to initialize Note objects that are virtual
     * @param id
     */
    void setId(int id) {
        this.id = id;
    }

    Note withId(int id) {
        Note ret = new Note(this.owner, this.contents);
        ret.setId(id);
        return ret;
    }
}
