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

// BARU: Implementasikan interface dari adapter
public class PlaylistsFragment extends Fragment implements PlaylistAdapter.OnPlaylistActionClickListener {

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

        dataSource = PlaylistDataSource.getInstance(requireContext());

        adapter = new PlaylistAdapter();
        // BARU: Daftarkan fragment ini sebagai listener untuk aksi update/delete
        adapter.setOnPlaylistActionClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> showAddPlaylistDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlaylistsAsync();
    }

    private void loadPlaylistsAsync() {
        // Metode ini sudah benar, tidak perlu diubah
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            dataSource.open();
            ArrayList<Playlist> playlists = dataSource.getAllPlaylists();
            dataSource.close();
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

    private void showAddPlaylistDialog() {
        // Metode ini sudah benar, tidak perlu diubah
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_playlist, null);
        final EditText editTextPlaylistName = dialogView.findViewById(R.id.editTextPlaylistName);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setTitle("Create New Playlist") // Judul disesuaikan
                .setPositiveButton("Save", (dialog, id) -> {
                    String playlistName = editTextPlaylistName.getText().toString().trim();
                    if (!playlistName.isEmpty()) {
                        saveNewPlaylist(playlistName);
                    } else {
                        Toast.makeText(getContext(), "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    private void saveNewPlaylist(String name) {
        // Metode ini sudah benar, tidak perlu diubah
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            dataSource.open();
            long result = dataSource.createPlaylist(name);
            dataSource.close();

            handler.post(() -> {
                if (result > 0) {
                    Toast.makeText(getContext(), "Playlist '" + name + "' created!", Toast.LENGTH_SHORT).show();
                    loadPlaylistsAsync();
                } else {
                    Toast.makeText(getContext(), "Failed to create playlist", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // --- BARU: Implementasi metode dari interface OnPlaylistActionClickListener ---

    @Override
    public void onUpdateClicked(Playlist playlist) {
        // Metode ini dipanggil dari adapter saat pengguna memilih "Edit Nama"
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_playlist, null);
        final EditText editTextPlaylistName = dialogView.findViewById(R.id.editTextPlaylistName);
        editTextPlaylistName.setText(playlist.getName()); // Isi dengan nama lama

        new AlertDialog.Builder(requireContext())
                .setTitle("Update Playlist Name")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, id) -> {
                    String newName = editTextPlaylistName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        updatePlaylistNameInDb(playlist.getId(), newName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create().show();
    }

    @Override
    public void onDeleteClicked(Playlist playlist) {
        // Metode ini dipanggil dari adapter saat pengguna memilih "Hapus Playlist"
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Playlist")
                .setMessage("Are you sure you want to delete '" + playlist.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePlaylistFromDb(playlist.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- BARU: Metode helper untuk menjalankan operasi update/delete di background ---

    private void updatePlaylistNameInDb(long playlistId, String newName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            dataSource.open();
            dataSource.updatePlaylist(playlistId, newName);
            dataSource.close();
            handler.post(() -> {
                Toast.makeText(getContext(), "Playlist updated", Toast.LENGTH_SHORT).show();
                loadPlaylistsAsync(); // Refresh daftar playlist
            });
        });
    }

    private void deletePlaylistFromDb(long playlistId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            dataSource.open();
            dataSource.deletePlaylistById(playlistId);
            dataSource.close();
            handler.post(() -> {
                Toast.makeText(getContext(), "Playlist deleted", Toast.LENGTH_SHORT).show();
                loadPlaylistsAsync(); // Refresh daftar playlist
            });
        });
    }
}