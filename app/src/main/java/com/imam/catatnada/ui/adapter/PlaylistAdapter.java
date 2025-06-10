package com.imam.catatnada.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.imam.catatnada.R;
import com.imam.catatnada.database.Playlist; // Pastikan import ini benar
import com.imam.catatnada.database.PlaylistDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final List<Playlist> playlistList = new ArrayList<>();

    public void setPlaylists(List<Playlist> playlists) {
        this.playlistList.clear();
        this.playlistList.addAll(playlists);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlists, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        holder.bind(playlistList.get(position));
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewPlaylistName;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPlaylistName = itemView.findViewById(R.id.textViewPlaylistName);
        }

        public void bind(Playlist playlist) {
            textViewPlaylistName.setText(playlist.getName());

            // 🔽 TAMBAHKAN ONCLICKLISTENER DI SINI 🔽
            itemView.setOnClickListener(v -> {
                // Buat bundle untuk mengirim argumen
                Bundle bundle = new Bundle();
                bundle.putLong("playlistId", playlist.getId());
                bundle.putString("playlistName", playlist.getName());

                // Gunakan NavController untuk berpindah fragment
                Navigation.findNavController(v).navigate(R.id.action_playlistsFragment_to_playlistDetailFragment, bundle);
            });

            itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Delete Playlist")
                        .setMessage("Delete '" + playlist.getName() + "'? All tracks inside will be lost.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Di sini kita akan memanggil metode delete
                            deletePlaylist(playlist.getId(), itemView.getContext());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true; // Kembalikan true untuk menandakan event sudah ditangani
            });
        }

        private void deletePlaylist(long playlistId, Context context) {
            PlaylistDataSource dataSource = PlaylistDataSource.getInstance(context);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                dataSource.open();
                dataSource.deletePlaylistById(playlistId);
                dataSource.close();

                handler.post(() -> {
                    Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show();
                    // Bagaimana cara me-refresh list di fragment? Kita butuh listener!
                    // Untuk sementara, kita tidak bisa refresh otomatis dari adapter.
                });
            });
        }
    }


}