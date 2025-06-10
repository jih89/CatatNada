package com.imam.catatnada.database;

public class Playlist {
    private long id;
    private String name;
    private long creationDate;

    // Buat constructor, getter, dan setter
    public Playlist(long id, String name, long creationDate) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getCreationDate() { return creationDate; }
    public void setCreationDate(long creationDate) { this.creationDate = creationDate; }
}