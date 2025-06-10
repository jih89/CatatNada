package com.imam.catatnada.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;    // BARU: Import untuk Button
import android.widget.LinearLayout; // BARU: Import untuk LinearLayout
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

    // BARU: Komponen untuk menampilkan error dan retry
    private LinearLayout layoutError;
    private Button buttonRetry;
    private String currentTag = "Global"; // Untuk menyimpan tag terakhir yang dipilih

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewTracks);
        progressBar = view.findViewById(R.id.progressBar);
        ChipGroup chipGroupGenre = view.findViewById(R.id.chipGroupGenre);
        layoutError = view.findViewById(R.id.layoutError);
        buttonRetry = view.findViewById(R.id.buttonRetry);

        trackAdapter = new TrackAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(trackAdapter);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        buttonRetry.setOnClickListener(v -> fetchTopTracksByTag(currentTag));

        chipGroupGenre.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipGlobal) {
                currentTag = "Global";
            } else if (checkedId == R.id.chipRock) {
                currentTag = "rock";
            } else if (checkedId == R.id.chipPop) {
                currentTag = "pop";
            } else if (checkedId == R.id.chipElectronic) {
                currentTag = "electronic";
            }
            fetchTopTracksByTag(currentTag);
        });

        // Muat data awal untuk Global
        if (savedInstanceState == null) {
            fetchTopTracksByTag(currentTag);
        }
    }

    private void fetchTopTracksByTag(String tag) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);

        trackAdapter.setTracks(new ArrayList<>(), "trending");
        Log.d(TAG, "MEMULAI FETCH BARU untuk tag: " + tag);

        Call<LastFmModels.TopTracksResponse> call = tag.equals("Global") ?
                apiService.getTopTracks(API_KEY, 10) :
                apiService.getTagTopTracks(tag, API_KEY, 10);

        call.enqueue(new Callback<LastFmModels.TopTracksResponse>() {
            @Override
            public void onResponse(Call<LastFmModels.TopTracksResponse> call, Response<LastFmModels.TopTracksResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<LastFmModels.TrackSimple> simpleTracks = response.body().getTracks().getTrackList();
                    if (simpleTracks == null || simpleTracks.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        showErrorView();
                        return;
                    }
                    fetchAllDetails(simpleTracks);
                } else {
                    Log.e(TAG, "Gagal mendapatkan Top Tracks. Kode: " + response.code());
                    showErrorView();
                }
            }

            @Override
            public void onFailure(Call<LastFmModels.TopTracksResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Gagal koneksi untuk tag: " + tag, t);
                showErrorView();
            }
        });
    }

    private void fetchAllDetails(List<LastFmModels.TrackSimple> simpleTracks) {
        if (!isAdded()) return;

        List<LastFmModels.TrackDetail> completeTrackList = new ArrayList<>();
        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalRequests = simpleTracks.size();

        for (LastFmModels.TrackSimple track : simpleTracks) {
            Call<LastFmModels.TrackInfoResponse> call = apiService.getTrackInfo(
                API_KEY,
                track.getArtist().getName(),
                track.getName()
            );

            call.enqueue(new Callback<LastFmModels.TrackInfoResponse>() {
                @Override
                public void onResponse(Call<LastFmModels.TrackInfoResponse> call, Response<LastFmModels.TrackInfoResponse> response) {
                    if (!isAdded()) return;

                    if (response.isSuccessful() && response.body() != null && response.body().getTrack() != null) {
                        completeTrackList.add(response.body().getTrack());
                    }

                    if (completedRequests.incrementAndGet() == totalRequests) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (isAdded()) {
                                    updateUiWithNewData(completeTrackList);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<LastFmModels.TrackInfoResponse> call, Throwable t) {
                    if (!isAdded()) return;
                    Log.e(TAG, "Gagal mendapatkan detail untuk: " + track.getName(), t);
                    
                    if (completedRequests.incrementAndGet() == totalRequests) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (isAdded()) {
                                    updateUiWithNewData(completeTrackList);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void updateUiWithNewData(List<LastFmModels.TrackDetail> finalTrackList) {
        if (!isAdded()) return;

        progressBar.setVisibility(View.GONE);
        if (finalTrackList.isEmpty()) {
            showErrorView();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutError.setVisibility(View.GONE);
            trackAdapter.setTracks(finalTrackList, "trending");
        }
    }

    private void showErrorView() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
    }
}