package com.imam.catatnada.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LastFmModels {

    private LastFmModels() {}

    // == KELAS-KELAS UNTUK chart.getTopTracks ==
    public static class TopTracksResponse {
        private TopTracks tracks;
        public TopTracks getTracks() { return tracks; }
    }

    public static class TopTracks {
        @SerializedName("track")
        private List<TrackSimple> trackList;
        public List<TrackSimple> getTrackList() { return trackList; }
    }

    // Model sederhana untuk daftar chart
    public static class TrackSimple { // Track yang masih default, tidak ada info album
        private String name;
        private Artist artist;
        public String getName() { return name; }
        public Artist getArtist() { return artist; }
    }

    // == KELAS-KELAS BARU UNTUK track.getInfo ==
    public static class TrackInfoResponse {
        private TrackDetail track;
        public TrackDetail getTrack() { return track; }
    }

    // Model detail untuk satu lagu
    public static class TrackDetail {
        private String name;
        private String url;
        private Artist artist;
        private Album album; // <-- Di sini ada info album
        private String listeners;
        private String playcount;
        private TopTags toptags;
        private Wiki wiki;

        // Getters
        public String getName() { return name; }
        public String getUrl() { return url; }

        public Artist getArtist() { return artist; }
        public Album getAlbum() { return album; }
        public String getListeners() { return listeners; }
        public String getPlaycount() { return playcount; }
        public TopTags getToptags() { return toptags; }
        public Wiki getWiki() { return wiki; }
    }

    public static class Wiki {
        private String published;
        private String summary;
        private String content;

        // Getters
        public String getPublished() { return published; }
        public String getSummary() { return summary; }
        public String getContent() { return content; }
    }

    public static class Album {
        private String artist;
        private String title;
        private List<Image> image;
        public List<Image> getImage() { return image; }
        public String getTitle() { return title; }
    }

    public static class TopTags {
        private List<Tag> tag;
        public List<Tag> getTag() { return tag; }
    }

    public static class Tag {
        private String name;
        private String url;
        public String getName() { return name; }
    }

    // == KELAS-KELAS UMUM ==
    public static class Artist {
        private String name;
        public String getName() { return name; }
    }

    public static class Image {
        @SerializedName("#text")
        private String url;
        private String size;
        public String getUrl() { return url; }
        public String getSize() { return size; }
    }

    // Model paling luar untuk hasil pencarian
    public static class SearchResultsResponse {
        private SearchResults results;
        public SearchResults getResults() { return results; }
    }

    public static class SearchResults {
        @SerializedName("trackmatches")
        private TrackMatches trackMatches;
        public TrackMatches getTrackMatches() { return trackMatches; }
    }

    public static class TrackMatches {
        @SerializedName("track")
        private List<TrackSimpleSearch> trackList;
        public List<TrackSimpleSearch> getTrackList() { return trackList; }
    }

    // Model untuk satu lagu dari hasil pencarian
    // Mirip dengan TrackSimple, tapi nama artis ada di field berbeda
    public static class TrackSimpleSearch {
        private String name;
        private String artist; // Di sini nama artis langsung jadi string
        public String getName() { return name; }
        public String getArtist() { return artist; }
    }
}