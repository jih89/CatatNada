package com.imam.catatnada.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.imam.catatnada.R;
import com.imam.catatnada.database.Playlist;
import com.imam.catatnada.database.PlaylistDataSource;
import com.imam.catatnada.ui.adapter.PlaylistAdapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;
    private TextView textViewNoPlaylists;
    private PlaylistDataSource dataSource;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewPlaylists);
        textViewNoPlaylists = view.findViewById(R.id.textViewNoPlaylists);
        FloatingActionButton fab = view.findViewById(R.id.fabAddPlaylist);

        // Inisialisasi DataSource
        dataSource = PlaylistDataSource.getInstance(requireContext());

        // Setup RecyclerView
        adapter = new PlaylistAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddPlaylistDialog());
    }
    @Override
    public void onResume() {
        super.onResume();
        // Muat data setiap kali fragment ini ditampilkan agar selalu update
        loadPlaylistsAsync();
    }

    private void loadPlaylistsAsync() {
        // Ini adalah implementasi Background Thread yang disyaratkan
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Background thread
            dataSource.open();
            ArrayList<Playlist> playlists = dataSource.getAllPlaylists();
            dataSource.close();

            // UI thread
            handler.post(() -> {
                if (playlists.isEmpty()) {
                    textViewNoPlaylists.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    textViewNoPlaylists.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setPlaylists(playlists);
                }
            });
        });
    }

    /**
     * Menampilkan dialog pop-up untuk membuat playlist baru.
     */
    private void showAddPlaylistDialog() {
        // Buat layout untuk dialog dari file XML
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_playlist, null);
        final EditText editTextPlaylistName = dialogView.findViewById(R.id.editTextPlaylistName);

        // Bangun AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, id) -> {
                    String playlistName = editTextPlaylistName.getText().toString().trim();
                    if (!playlistName.isEmpty()) {
                        // Panggil metode untuk menyimpan ke database
                        saveNewPlaylist(playlistName);
                    } else {
                        Toast.makeText(getContext(), "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    /**
     * Menyimpan playlist baru ke database menggunakan background thread.
     */
    private void saveNewPlaylist(String name) {
        // Implementasi Background Thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Background thread
            dataSource.open();
            long result = dataSource.createPlaylist(name);
            dataSource.close();

            // UI thread
            handler.post(() -> {
                if (result > 0) {
                    Toast.makeText(getContext(), "Playlist '" + name + "' created!", Toast.LENGTH_SHORT).show();
                    // Muat ulang daftar playlist agar yang baru muncul
                    loadPlaylistsAsync();
                } else {
                    Toast.makeText(getContext(), "Failed to create playlist", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}