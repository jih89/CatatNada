package com.imam.catatnada.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.imam.catatnada.R;
import com.imam.catatnada.api.ApiService;
import com.imam.catatnada.api.LastFmModels;
import com.imam.catatnada.api.RetrofitClient;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);

        // Inisialisasi Views
        initViews();

        // Ambil data dari Intent
        String trackName = getIntent().getStringExtra(TRACK_NAME);
        String artistName = getIntent().getStringExtra(ARTIST_NAME);

        // Inisialisasi API service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Ambil data detail jika trackName dan artistName ada
        if (trackName != null && artistName != null) {
            fetchTrackDetails(artistName, trackName);
        } else {
            Toast.makeText(this, "Error: Track data not found.", Toast.LENGTH_SHORT).show();
            finish(); // Tutup activity jika tidak ada data
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
        // Set data teks
        textViewTrackName.setText(track.getName());
        textViewArtistName.setText(track.getArtist().getName());

        // Cek jika ada info album
        if (track.getAlbum() != null) {
            textViewAlbumName.setText(track.getAlbum().getTitle());

            // Ambil gambar sampul
            if (track.getAlbum().getImage() != null && !track.getAlbum().getImage().isEmpty()) {
                String imageUrl = track.getAlbum().getImage().get(track.getAlbum().getImage().size() - 1).getUrl();
                Glide.with(this).load(imageUrl).into(imageViewDetailArt);
            }
        } else {
            textViewAlbumName.setText("N/A");
        }

        // Tampilkan ringkasan/wiki
        if (track.getWiki() != null && track.getWiki().getSummary() != null) {
            // 1. Ambil teks ringkasan asli dari API
            String originalSummary = track.getWiki().getSummary();

            // 2. Cari posisi di mana tag <a> dimulai
            int linkPosition = originalSummary.indexOf("<a href=");

            String cleanSummary;

            // 3. Cek apakah tag <a> ditemukan
            if (linkPosition != -1) {
                // Jika ditemukan, potong string dari awal sampai sebelum tag <a>
                cleanSummary = originalSummary.substring(0, linkPosition).trim();
            } else {
                // Jika tidak ada tag, gunakan teks aslinya
                cleanSummary = originalSummary.trim();
            }

            // 4. Tampilkan teks yang sudah bersih
            textViewSummary.setText(cleanSummary);

        } else {
            // Jika tidak ada deskripsi, sembunyikan TextView-nya
            textViewSummary.setVisibility(View.GONE);
        }

        // Tampilkan tags/genre sebagai Chip
        if (track.getToptags() != null && !track.getToptags().getTag().isEmpty()) {
            chipGroupTags.removeAllViews(); // Bersihkan chip lama jika ada
            for (LastFmModels.Tag tag : track.getToptags().getTag()) {
                Chip chip = new Chip(this);
                chip.setText(tag.getName());
                chipGroupTags.addView(chip);
            }
        }

        // Set listener untuk tombol "View on Last.fm"
        buttonViewOnLastFm.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(track.getUrl()));
            startActivity(browserIntent);
        });
    }
}