package com.imam.catatnada.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private final String API_KEY = "a604b5b421465fe9e7be6f7f96edf595";

    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textViewInfo;
    private TrackAdapter trackAdapter;
    private ApiService apiService;
    private LinearLayout layoutError;
    private Button buttonRetry;
    private String lastSearchQuery = "";

    private final List<LastFmModels.TrackDetail> completeTrackList = new ArrayList<>();
    private int totalTracksToFetch = 0;
    private int tracksFetched = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi Views
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerViewSearch);
        progressBar = view.findViewById(R.id.progressBarSearch);
        textViewInfo = view.findViewById(R.id.textViewInfo);
        layoutError = view.findViewById(R.id.layoutError);
        buttonRetry = view.findViewById(R.id.buttonRetry);

        // Setup RecyclerView & Adapter
        trackAdapter = new TrackAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(trackAdapter);

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Setup listener untuk tombol Retry
        buttonRetry.setOnClickListener(v -> {
            if (!lastSearchQuery.isEmpty()) {
                performSearch(lastSearchQuery);
            }
        });

        // Setup Listener untuk SearchView
        setupSearchView();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Saat pengguna menekan tombol cari
                if (!query.trim().isEmpty()) {
                    performSearch(query.trim());
                }
                return true; // Menandakan bahwa kita sudah menangani event ini
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Bisa diabaikan untuk sekarang
                return false;
            }
        });
    }

    private void performSearch(String query) {
        // Simpan query terakhir untuk retry
        lastSearchQuery = query;

        // Tampilkan loading, sembunyikan yang lain
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textViewInfo.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        completeTrackList.clear();
        trackAdapter.setTracks(new ArrayList<>(), "search");

        Log.d(TAG, "Mencari lagu: " + query);

        Call<LastFmModels.SearchResultsResponse> call = apiService.searchTracks(query, API_KEY, 20);

        call.enqueue(new Callback<LastFmModels.SearchResultsResponse>() {
            @Override
            public void onResponse(Call<LastFmModels.SearchResultsResponse> call, Response<LastFmModels.SearchResultsResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<LastFmModels.TrackSimpleSearch> searchResults = response.body().getResults().getTrackMatches().getTrackList();
                    totalTracksToFetch = searchResults.size();
                    tracksFetched = 0;

                    if (totalTracksToFetch == 0) {
                        progressBar.setVisibility(View.GONE);
                        textViewInfo.setText("No results found for \"" + query + "\"");
                        textViewInfo.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Ambil detail untuk setiap hasil pencarian
                    for (LastFmModels.TrackSimpleSearch track : searchResults) {
                        fetchTrackDetails(track.getArtist(), track.getName());
                    }
                } else {
                    Log.e(TAG, "Search failed. Code: " + response.code());
                    showErrorView();
                }
            }

            @Override
            public void onFailure(Call<LastFmModels.SearchResultsResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Connection failure: " + t.getMessage());
                showErrorView();
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
                checkIfAllDataFetched();
            }
            @Override
            public void onFailure(Call<LastFmModels.TrackInfoResponse> call, Throwable t) {
                checkIfAllDataFetched();
                Log.e(TAG, "Failed to get details for: " + trackName);
            }
        });
    }

    private void showErrorView() {
        if (!isAdded()) return;
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        textViewInfo.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
    }

    private void checkIfAllDataFetched() {
        tracksFetched++;
        if (tracksFetched >= totalTracksToFetch) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    if (completeTrackList.isEmpty()) {
                        showErrorView();
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        layoutError.setVisibility(View.GONE);
                        trackAdapter.setTracks(completeTrackList, "search");
                        Log.d(TAG, "All search results loaded and displayed.");
                    }
                });
            }
        }
    }
}