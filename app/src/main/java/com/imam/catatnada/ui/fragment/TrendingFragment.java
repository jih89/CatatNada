package com.imam.catatnada.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imam.catatnada.R;
import com.imam.catatnada.api.ApiService;
import com.imam.catatnada.api.LastFmModels;
import com.imam.catatnada.api.RetrofitClient;
import com.imam.catatnada.ui.adapter.TrackAdapter;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrendingFragment extends Fragment {

    private static final String TAG = "TrendingFragment";
    private final String API_KEY = "a604b5b421465fe9e7be6f7f96edf595"; // ❗ GANTI INI

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TrackAdapter trackAdapter;
    private ApiService apiService;

    private final List<LastFmModels.TrackDetail> completeTrackList = new ArrayList<>();
    private int totalTracksToFetch = 0;
    private int tracksFetched = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi Views
        recyclerView = view.findViewById(R.id.recyclerViewTracks);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView
        trackAdapter = new TrackAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(trackAdapter);

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Mulai ambil data
        fetchTopTracks();
    }

    private void fetchTopTracks() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        Call<LastFmModels.TopTracksResponse> call = apiService.getTopTracks(API_KEY, 20); // Ambil 10 lagu

        call.enqueue(new Callback<LastFmModels.TopTracksResponse>() {
            @Override
            public void onResponse(Call<LastFmModels.TopTracksResponse> call, Response<LastFmModels.TopTracksResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LastFmModels.TrackSimple> simpleTracks = response.body().getTracks().getTrackList();
                    totalTracksToFetch = simpleTracks.size();
                    tracksFetched = 0;
                    completeTrackList.clear();

                    if (totalTracksToFetch == 0) {
                        progressBar.setVisibility(View.GONE);
                        // Tampilkan pesan "Tidak ada data" jika perlu
                    }

                    for (LastFmModels.TrackSimple track : simpleTracks) {
                        fetchTrackDetails(track.getArtist().getName(), track.getName());
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Gagal mendapatkan Top Tracks. Kode: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LastFmModels.TopTracksResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Gagal koneksi (Top Tracks): " + t.getMessage());
            }
        });
    }

    private void fetchTrackDetails(String artistName, String trackName) {
        Call<LastFmModels.TrackInfoResponse> call = apiService.getTrackInfo(API_KEY, artistName, trackName);
        call.enqueue(new Callback<LastFmModels.TrackInfoResponse>() {
            @Override
            public void onResponse(Call<LastFmModels.TrackInfoResponse> call, Response<LastFmModels.TrackInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getTrack() != null) {
                    completeTrackList.add(response.body().getTrack());
                }

                // Cek apakah semua data detail sudah terkumpul
                checkIfAllDataFetched();
            }

            @Override
            public void onFailure(Call<LastFmModels.TrackInfoResponse> call, Throwable t) {
                // Tetap panggil check, agar progress bar tidak stuck jika ada yg gagal
                checkIfAllDataFetched();
                Log.e(TAG, "Gagal mendapatkan detail untuk: " + trackName);
            }
        });
    }

    private synchronized void checkIfAllDataFetched() {
        tracksFetched++;
        if (tracksFetched == totalTracksToFetch) {
            // Semua data sudah terkumpul, sekarang tampilkan!
            getActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                trackAdapter.setTracks(completeTrackList);
                Log.d(TAG, "Semua data berhasil dimuat dan ditampilkan.");
            });
        }
    }
}