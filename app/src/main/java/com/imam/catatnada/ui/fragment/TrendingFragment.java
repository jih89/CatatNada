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

import com.google.android.material.chip.ChipGroup;
import com.imam.catatnada.R;
import com.imam.catatnada.api.ApiService;
import com.imam.catatnada.api.LastFmModels;
import com.imam.catatnada.api.RetrofitClient;
import com.imam.catatnada.ui.adapter.TrackAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrendingFragment extends Fragment {

    private static final String TAG = "TrendingFragment";
    private final String API_KEY = "a604b5b421465fe9e7be6f7f96edf595";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TrackAdapter trackAdapter;
    private ApiService apiService;

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
        ChipGroup chipGroupGenre = view.findViewById(R.id.chipGroupGenre);

        // Setup RecyclerView & Adapter
        trackAdapter = new TrackAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(trackAdapter);

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Setup listener untuk ChipGroup
        chipGroupGenre.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipGlobal) {
                fetchTopTracksByTag("Global");
            } else if (checkedId == R.id.chipRock) {
                fetchTopTracksByTag("rock");
            } else if (checkedId == R.id.chipPop) {
                fetchTopTracksByTag("pop");
            } else if (checkedId == R.id.chipElectronic) {
                fetchTopTracksByTag("electronic");
            }
        });

        // Muat data awal untuk "Global" saat fragment pertama kali dibuat
        // Pastikan chip "Global" ter-checklist secara default di XML
        if (savedInstanceState == null) { // Hanya panggil saat pertama kali fragment dibuat
            fetchTopTracksByTag("Global");
        }
    }

    private void fetchTopTracksByTag(String tag) {
        // Tampilkan UI loading
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        // Kosongkan tampilan lama
        trackAdapter.setTracks(new ArrayList<>(), "trending");

        Log.d(TAG, "MEMULAI FETCH BARU untuk tag: " + tag);

        Call<LastFmModels.TopTracksResponse> call = tag.equals("Global") ?
                apiService.getTopTracks(API_KEY, 10) :
                apiService.getTagTopTracks(tag, API_KEY, 10);

        call.enqueue(new Callback<LastFmModels.TopTracksResponse>() {
            @Override
            public void onResponse(Call<LastFmModels.TopTracksResponse> call, Response<LastFmModels.TopTracksResponse> response) {
                if (!isAdded()) return; // Pastikan fragment masih terpasang

                if (response.isSuccessful() && response.body() != null) {
                    List<LastFmModels.TrackSimple> simpleTracks = response.body().getTracks().getTrackList();
                    if (simpleTracks == null || simpleTracks.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    // Proses pengambilan detail dengan "keranjang" lokalnya sendiri
                    fetchAllDetails(simpleTracks);

                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<LastFmModels.TopTracksResponse> call, Throwable t) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void fetchAllDetails(List<LastFmModels.TrackSimple> simpleTracks) {
        // 1. Buat "keranjang" lokal baru untuk setiap operasi fetch
        final List<LastFmModels.TrackDetail> newTrackList = Collections.synchronizedList(new ArrayList<>());
        // 2. Buat counter thread-safe baru
        final AtomicInteger counter = new AtomicInteger(0);
        final int totalTracks = simpleTracks.size();

        for (LastFmModels.TrackSimple track : simpleTracks) {
            Call<LastFmModels.TrackInfoResponse> detailCall = apiService.getTrackInfo(API_KEY, track.getArtist().getName(), track.getName());

            detailCall.enqueue(new Callback<LastFmModels.TrackInfoResponse>() {
                @Override
                public void onResponse(Call<LastFmModels.TrackInfoResponse> call, Response<LastFmModels.TrackInfoResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getTrack() != null) {
                        // 3. Masukkan hasil ke "keranjang" lokal, bukan ke variabel global
                        newTrackList.add(response.body().getTrack());
                    }
                    // 4. Cek apakah semua sudah terkumpul
                    if (counter.incrementAndGet() == totalTracks) {
                        updateUiWithNewData(newTrackList);
                    }
                }

                @Override
                public void onFailure(Call<LastFmModels.TrackInfoResponse> call, Throwable t) {
                    // Tetap hitung agar proses tidak stuck
                    if (counter.incrementAndGet() == totalTracks) {
                        updateUiWithNewData(newTrackList);
                    }
                }
            });
        }
    }

    private void updateUiWithNewData(List<LastFmModels.TrackDetail> finalTrackList) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (!isAdded()) return; // Cek lagi sebelum update UI
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                // 5. Ganti data lama di adapter dengan data dari "keranjang" baru yang sudah penuh
                trackAdapter.setTracks(finalTrackList, "trending");
                Log.d(TAG, "UI diperbarui dengan " + finalTrackList.size() + " lagu.");
            });
        }
    }
}