package com.imam.catatnada.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.imam.catatnada.database.Playlist;
// import com.imam.catatnada.model.Track; // Import saat dibutuhkan

import java.util.ArrayList;

public class PlaylistDataSource {

    private static volatile PlaylistDataSource INSTANCE;
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private PlaylistDataSource(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public static PlaylistDataSource getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PlaylistDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PlaylistDataSource(context);
                }
            }
        }
        return INSTANCE;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // --- Operasi untuk Playlist ---

    public long createPlaylist(String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.PlaylistColumns.COLUMN_NAME, name);
        return database.insert(DatabaseContract.PlaylistColumns.TABLE_NAME, null, values);
    }

    public ArrayList<Playlist> getAllPlaylists() {
        ArrayList<Playlist> playlists = new ArrayList<>();
        // Menggunakan pola MappingHelper dari PDF secara langsung
        Cursor cursor = database.query(
                DatabaseContract.PlaylistColumns.TABLE_NAME,
                null, null, null, null, null,
                DatabaseContract.PlaylistColumns.COLUMN_NAME + " ASC");

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.PlaylistColumns._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PlaylistColumns.COLUMN_NAME));
                playlists.add(new Playlist(id, name));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return playlists;
    }

    // --- Tambahkan metode lain di sini nanti ---
    public long addTrackToPlaylist(long playlistId, Track track) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.TrackColumns.COLUMN_PLAYLIST_ID, playlistId);
        values.put(DatabaseContract.TrackColumns.COLUMN_TRACK_NAME, track.getTrackName());
        values.put(DatabaseContract.TrackColumns.COLUMN_ARTIST_NAME, track.getArtistName());
        values.put(DatabaseContract.TrackColumns.COLUMN_IMAGE_URL, track.getAlbumArtUrl());

        return database.insert(DatabaseContract.TrackColumns.TABLE_NAME, null, values);
    }

    /**
     * Mengambil semua lagu dari sebuah playlist berdasarkan ID playlist.
     * @param playlistId ID dari playlist yang ingin ditampilkan.
     * @return Sebuah ArrayList berisi objek Track.
     */
    public ArrayList<Track> getTracksFromPlaylist(long playlistId) {
        ArrayList<Track> trackList = new ArrayList<>();
        String selection = DatabaseContract.TrackColumns.COLUMN_PLAYLIST_ID + " = ?";
        String[] selectionArgs = { String.valueOf(playlistId) };

        Cursor cursor = database.query(
                DatabaseContract.TrackColumns.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.TrackColumns._ID));
                String trackName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TrackColumns.COLUMN_TRACK_NAME));
                String artistName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TrackColumns.COLUMN_ARTIST_NAME));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TrackColumns.COLUMN_IMAGE_URL));

                trackList.add(new Track(id, playlistId, trackName, artistName, imageUrl));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trackList;
    }

    /**
     * Menghapus satu lagu dari database berdasarkan ID lagu.
     * @param trackId ID dari lagu yang akan dihapus.
     * @return Jumlah baris yang berhasil dihapus (seharusnya 1).
     */
    public int deleteTrackById(long trackId) {
        String selection = DatabaseContract.TrackColumns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(trackId) };
        return database.delete(DatabaseContract.TrackColumns.TABLE_NAME, selection, selectionArgs);
    }

    public int deletePlaylistById(long playlistId) {
        // Kita tidak perlu menghapus lagu satu per satu karena kita sudah
        // mengatur "ON DELETE CASCADE" di skema database.
        String selection = DatabaseContract.PlaylistColumns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(playlistId) };
        return database.delete(DatabaseContract.PlaylistColumns.TABLE_NAME, selection, selectionArgs);
    }
}