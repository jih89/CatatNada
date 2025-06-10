package com.imam.catatnada.ui.adapter;

import android.os.Bundle; // BARU: Import Bundle
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation; // BARU: Import Navigation
import androidx.recyclerview.widget.RecyclerView;
import com.imam.catatnada.R;
import com.imam.catatnada.database.Playlist;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    public interface OnPlaylistActionClickListener {
        void onUpdateClicked(Playlist playlist);
        void onDeleteClicked(Playlist playlist);
    }
    private OnPlaylistActionClickListener actionListener;

    public void setOnPlaylistActionClickListener(OnPlaylistActionClickListener listener) {
        this.actionListener = listener;
    }

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
        holder.bind(playlistList.get(position), actionListener);
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewPlaylistName;
        private final TextView textViewCreationDate;
        private final SimpleDateFormat dateFormat;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPlaylistName = itemView.findViewById(R.id.textViewPlaylistName);
            textViewCreationDate = itemView.findViewById(R.id.textViewCreationDate);
            dateFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
        }

        public void bind(Playlist playlist, OnPlaylistActionClickListener listener) {
            textViewPlaylistName.setText(playlist.getName());
            
            // Format creation date
            String formattedDate = "Dibuat " + dateFormat.format(new Date(playlist.getCreationDate()));
            textViewCreationDate.setText(formattedDate);

            // Listener klik biasa untuk membuka detail
            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putLong("playlistId", playlist.getId());
                bundle.putString("playlistName", playlist.getName());
                Navigation.findNavController(v).navigate(R.id.action_playlistsFragment_to_playlistDetailFragment, bundle);
            });

            // Listener tekan lama untuk memunculkan pilihan Update/Delete
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    CharSequence[] options = {"Edit Nama", "Hapus Playlist"};
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Aksi untuk '" + playlist.getName() + "'")
                            .setItems(options, (dialog, which) -> {
                                if (which == 0) {
                                    listener.onUpdateClicked(playlist);
                                } else {
                                    listener.onDeleteClicked(playlist);
                                }
                            })
                            .show();
                }
                return true;
            });
        }
    }
}