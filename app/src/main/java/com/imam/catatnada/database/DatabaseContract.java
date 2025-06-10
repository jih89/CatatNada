package com.imam.catatnada.database;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    public static final class PlaylistColumns implements BaseColumns {
        public static final String TABLE_NAME = "playlists";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CREATION_DATE = "creation_date";
    }

    public static final class TrackColumns implements BaseColumns {
        public static final String TABLE_NAME = "playlist_tracks";
        public static final String COLUMN_PLAYLIST_ID = "playlist_id";
        public static final String COLUMN_TRACK_NAME = "track_name";
        public static final String COLUMN_ARTIST_NAME = "artist_name";
        public static final String COLUMN_IMAGE_URL = "image_url";
    }
}