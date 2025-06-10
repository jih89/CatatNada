package com.imam.catatnada.database;

public class Track {
    private long id;
    private long playlistId;
    private String trackName;
    private String artistName;
    private String albumArtUrl;

    // Constructor, Getter, dan Setter bisa Anda tambahkan sesuai kebutuhan
    // atau generate otomatis di Android Studio (Code > Generate > ...).

    public Track(long id, long playlistId, String trackName, String artistName, String albumArtUrl) {
        this.id = id;
        this.playlistId = playlistId;
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumArtUrl = albumArtUrl;
    }

    public long getId() { return id; }
    public long getPlaylistId() { return playlistId; }
    public String getTrackName() { return trackName; }
    public String getArtistName() { return artistName; }
    public String getAlbumArtUrl() { return albumArtUrl; }
}
