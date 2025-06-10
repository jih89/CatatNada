package com.imam.catatnada.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "catatnada.db";
    private static final int DATABASE_VERSION = 2;

    private static final String SQL_CREATE_TABLE_PLAYLISTS =
            String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s INTEGER NOT NULL)",
                    DatabaseContract.PlaylistColumns.TABLE_NAME,
                    DatabaseContract.PlaylistColumns._ID,
                    DatabaseContract.PlaylistColumns.COLUMN_NAME,
                    DatabaseContract.PlaylistColumns.COLUMN_CREATION_DATE);

    private static final String SQL_CREATE_TABLE_TRACKS =
            String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT, FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE)",
                    DatabaseContract.TrackColumns.TABLE_NAME,
                    DatabaseContract.TrackColumns._ID,
                    DatabaseContract.TrackColumns.COLUMN_PLAYLIST_ID,
                    DatabaseContract.TrackColumns.COLUMN_TRACK_NAME,
                    DatabaseContract.TrackColumns.COLUMN_ARTIST_NAME,
                    DatabaseContract.TrackColumns.COLUMN_IMAGE_URL,
                    DatabaseContract.TrackColumns.COLUMN_PLAYLIST_ID,
                    DatabaseContract.PlaylistColumns.TABLE_NAME,
                    DatabaseContract.PlaylistColumns._ID);

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_PLAYLISTS);
        db.execSQL(SQL_CREATE_TABLE_TRACKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add creation_date column to existing playlists table
            db.execSQL("ALTER TABLE " + DatabaseContract.PlaylistColumns.TABLE_NAME + 
                      " ADD COLUMN " + DatabaseContract.PlaylistColumns.COLUMN_CREATION_DATE + 
                      " INTEGER NOT NULL DEFAULT " + System.currentTimeMillis());
        }
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TrackColumns.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.PlaylistColumns.TABLE_NAME);
        onCreate(db);
    }
}