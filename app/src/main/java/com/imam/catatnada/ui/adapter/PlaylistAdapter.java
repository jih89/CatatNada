package com.imam.catatnada.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Import AlertDialog
import androidx.recyclerview.widget.RecyclerView;
import com.imam.catatnada.R;
import com.imam.catatnada.database.Playlist;
import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    // --- Interface untuk berkomunikasi dengan Fragment ---
    public interface OnPlaylistActionClickListener {
        void onUpdateClicked(Playlist playlist);
        void onDeleteClicked(Playlist playlist);
    }
    private OnPlaylistActionClickListener actionListener;

    public void setOnPlaylistActionClickListener(OnPlaylistActionClickListener listener) {
        this.actionListener = listener;
    }
    // ----------------------------------------------------

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
        // Kirim listener ke ViewHolder
        holder.bind(playlistList.get(position), actionListener);
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

        // Metode bind sekarang menerima listener
        public void bind(Playlist playlist, OnPlaylistActionClickListener listener) {
            textViewPlaylistName.setText(playlist.getName());

            // TODO: Nanti tambahkan listener klik biasa untuk membuka detail
            // itemView.setOnClickListener(v -> { ... });

            // Listener tekan lama untuk memunculkan pilihan Update/Delete
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    CharSequence[] options = {"Edit Nama", "Hapus Playlist"};
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Aksi untuk '" + playlist.getName() + "'")
                            .setItems(options, (dialog, which) -> {
                                if (which == 0) { // Opsi "Edit Nama"
                                    listener.onUpdateClicked(playlist);
                                } else { // Opsi "Hapus Playlist"
                                    listener.onDeleteClicked(playlist);
                                }
                            })
                            .show();
                }
                return true; // Event sudah ditangani
            });
        }
    }
}