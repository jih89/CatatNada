package com.imam.catatnada.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.imam.catatnada.R;
import com.imam.catatnada.api.ApiService;
import com.imam.catatnada.api.LastFmModels;
import com.imam.catatnada.api.RetrofitClient;
import com.imam.catatnada.database.Playlist;
import com.imam.catatnada.database.PlaylistDataSource;
import com.imam.catatnada.database.Track;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackDetailActivity extends AppCompatActivity {

    private static final String TAG = "TrackDetailActivity";
    public static final String TRACK_NAME = "TRACK_NAME";
    public static final String ARTIST_NAME = "ARTIST_NAME";

    private ImageView imageViewDetailArt;
    private TextView textViewTrackName, textViewArtistName, textViewAlbumName, textViewSummary;
    private ChipGroup chipGroupTags;
    private Button buttonViewOnLastFm;
    private ApiService apiService;
    private Button buttonSaveToPlaylist;
    private PlaylistDataSource dataSource;
    private LastFmModels.TrackDetail currentTrackDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);

        initViews();

        String trackName = getIntent().getStringExtra(TRACK_NAME);
        String artistName = getIntent().getStringExtra(ARTIST_NAME);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        dataSource = PlaylistDataSource.getInstance(this);

        if (trackName != null && artistName != null) {
            fetchTrackDetails(artistName, trackName);
        } else {
            Toast.makeText(this, "Error: Track data not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        imageViewDetailArt = findViewById(R.id.imageViewDetailArt);
        textViewTrackName = findViewById(R.id.textViewDetailTrackName);
        textViewArtistName = findViewById(R.id.textViewDetailArtistName);
        textViewAlbumName = findViewById(R.id.textViewDetailAlbumName);
        textViewSummary = findViewById(R.id.textViewDetailSummary);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        buttonViewOnLastFm = findViewById(R.id.buttonViewOnLastFm);
        buttonSaveToPlaylist = findViewById(R.id.buttonSaveToPlaylist);
    }

    private void fetchTrackDetails(String artistName, String trackName) {
        String apiKey = "a604b5b421465fe9e7be6f7f96edf595";
        Call<LastFmModels.TrackInfoResponse> call = apiService.getTrackInfo(apiKey, artistName, trackName);

        call.enqueue(new Callback<LastFmModels.TrackInfoResponse>() {
            @Override
            public void onResponse(Call<LastFmModels.TrackInfoResponse> call, Response<LastFmModels.TrackInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getTrack() != null) {
                    LastFmModels.TrackDetail trackDetail = response.body().getTrack();
                    updateUi(trackDetail);
                } else {
                    Toast.makeText(TrackDetailActivity.this, "Failed to fetch track details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LastFmModels.TrackInfoResponse> call, Throwable t) {
                Toast.makeText(TrackDetailActivity.this, "Network Error.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private void updateUi(LastFmModels.TrackDetail track) {
        // Simpan info track saat ini ke variabel global agar bisa diakses nanti
        this.currentTrackDetail = track;

        textViewTrackName.setText(track.getName());
        textViewArtistName.setText(track.getArtist().getName());

        if (track.getAlbum() != null) {
            textViewAlbumName.setText(track.getAlbum().getTitle());
            if (track.getAlbum().getImage() != null && !track.getAlbum().getImage().isEmpty()) {
                String imageUrl = track.getAlbum().getImage().get(track.getAlbum().getImage().size() - 1).getUrl();
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imageViewDetailArt);
            } else {
                Glide.with(this)
                    .load(R.drawable.placeholder_image)
                    .into(imageViewDetailArt);
            }
        } else {
            textViewAlbumName.setText("N/A");
            Glide.with(this)
                .load(R.drawable.placeholder_image)
                .into(imageViewDetailArt);
        }

        if (track.getWiki() != null && track.getWiki().getSummary() != null) {
            String originalSummary = track.getWiki().getSummary();
            int linkPosition = originalSummary.indexOf("<a href=");
            String cleanSummary = (linkPosition != -1) ? originalSummary.substring(0, linkPosition).trim() : originalSummary.trim();
            textViewSummary.setText(cleanSummary);
        } else {
            textViewSummary.setVisibility(View.GONE);
        }

        if (track.getToptags() != null && !track.getToptags().getTag().isEmpty()) {
            chipGroupTags.removeAllViews();
            for (LastFmModels.Tag tag : track.getToptags().getTag()) {
                Chip chip = new Chip(this);
                chip.setText(tag.getName());
                chipGroupTags.addView(chip);
            }
        }

        buttonViewOnLastFm.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(track.getUrl()));
            startActivity(browserIntent);
        });

        // Set listener untuk tombol "Save to Playlist"
        buttonSaveToPlaylist.setOnClickListener(v -> showPlaylistSelectionDialog());
    }

    private void showPlaylistSelectionDialog() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Background thread
            dataSource.open();
            List<Playlist> playlists = dataSource.getAllPlaylists();
            dataSource.close();

            // UI thread
            handler.post(() -> {
                if (playlists.isEmpty()) {
                    Toast.makeText(this, "No playlists available. Create one first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                CharSequence[] playlistNames = new CharSequence[playlists.size()];
                for (int i = 0; i < playlists.size(); i++) {
                    playlistNames[i] = playlists.get(i).getName();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Save to...")
                        .setItems(playlistNames, (dialog, which) -> {
                            Playlist selectedPlaylist = playlists.get(which);
                            saveTrackToSelectedPlaylist(selectedPlaylist);
                        });
                builder.create().show();
            });
        });
    }

    private void saveTrackToSelectedPlaylist(Playlist selectedPlaylist) {
        if (currentTrackDetail == null) return;

        String trackName = currentTrackDetail.getName();
        String artistName = currentTrackDetail.getArtist().getName();
        String imageUrl = null;
        if (currentTrackDetail.getAlbum() != null && !currentTrackDetail.getAlbum().getImage().isEmpty()) {
            // Ambil gambar ukuran medium/large jika ada
            imageUrl = currentTrackDetail.getAlbum().getImage().get(Math.min(2, currentTrackDetail.getAlbum().getImage().size() -1)).getUrl();
        }

        Track trackToSave = new Track(0, selectedPlaylist.getId(), trackName, artistName, imageUrl);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Background thread
            dataSource.open();
            long result = dataSource.addTrackToPlaylist(selectedPlaylist.getId(), trackToSave);
            dataSource.close();

            // UI thread
            handler.post(() -> {
                if (result > 0) {
                    Toast.makeText(this, "Saved to '" + selectedPlaylist.getName() + "'", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save track", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}