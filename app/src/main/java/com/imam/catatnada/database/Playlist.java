package com.imam.catatnada.database;

public class Playlist {
    private long id;
    private String name;

    // Buat constructor, getter, dan setter
    public Playlist(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}