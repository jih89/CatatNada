package com.imam.catatnada.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imam.catatnada.R;
import com.imam.catatnada.database.PlaylistDataSource;
import com.imam.catatnada.database.Track;
import com.imam.catatnada.ui.adapter.TrackAdapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// FIX #1: Implementasikan interface
public class PlaylistDetailFragment extends Fragment implements TrackAdapter.OnTrackDeleteListener {

    private RecyclerView recyclerView;
    private TrackAdapter adapter;
    private PlaylistDataSource dataSource;
    private long playlistId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playlistId = getArguments().getLong("playlistId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewPlaylistTracks);
        adapter = new TrackAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // FIX #2: Set listener SEBELUM set adapter
        adapter.setOnTrackDeleteListener(this);
        recyclerView.setAdapter(adapter);

        dataSource = PlaylistDataSource.getInstance(requireContext());
        loadTracksFromPlaylist();
    }

    private void loadTracksFromPlaylist() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            dataSource.open();
            ArrayList<Track> tracksFromDb = dataSource.getTracksFromPlaylist(playlistId);
            dataSource.close();

            handler.post(() -> {
                adapter.setTracks(tracksFromDb, "playlistDetail");
            });
        });
    }

    // Metode ini sekarang wajib ada karena implementasi interface
    @Override
    public void onTrackDeleted(long trackId, String trackName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Track")
                .setMessage("Are you sure you want to delete '" + trackName + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTrackFromDb(trackId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTrackFromDb(long trackId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            dataSource.open();
            int result = dataSource.deleteTrackById(trackId);
            dataSource.close();

            handler.post(() -> {
                if (result > 0) {
                    Toast.makeText(getContext(), "Track deleted", Toast.LENGTH_SHORT).show();
                    loadTracksFromPlaylist();
                } else {
                    Toast.makeText(getContext(), "Failed to delete track", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}