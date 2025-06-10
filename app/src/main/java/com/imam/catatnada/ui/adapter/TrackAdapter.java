package com.imam.catatnada.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.imam.catatnada.R;
import com.imam.catatnada.api.LastFmModels;
import com.imam.catatnada.database.Track;
import com.imam.catatnada.ui.activity.TrackDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    // --- Bagian Listener untuk Delete ---
    public interface OnTrackDeleteListener {
        void onTrackDeleted(long trackId, String trackName);
    }
    private OnTrackDeleteListener deleteListener;

    public void setOnTrackDeleteListener(OnTrackDeleteListener listener) {
        this.deleteListener = listener;
    }
    // ------------------------------------

    private final List<Object> itemList = new ArrayList<>();
    private String screenType = "trending";

    public void setTracks(List<?> items, String screenType) {
        this.itemList.clear();
        this.itemList.addAll(items);
        this.screenType = screenType;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        // Kirim objek dan listener ke ViewHolder
        holder.bind(itemList.get(position), position + 1, screenType, deleteListener);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // --- ViewHolder Class ---
    static class TrackViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton buttonDeleteTrack; // Tombol hapus
        private final TextView textViewTrackNumber;
        private final ImageView imageViewAlbumArt;
        private final TextView textViewTrackName;
        private final TextView textViewArtistName;
        private final TextView textViewInfoLine;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inisialisasi semua view, termasuk tombol hapus
            buttonDeleteTrack = itemView.findViewById(R.id.buttonDeleteTrack);
            textViewTrackNumber = itemView.findViewById(R.id.textViewTrackNumber);
            imageViewAlbumArt = itemView.findViewById(R.id.imageViewAlbumArt);
            textViewTrackName = itemView.findViewById(R.id.textViewTrackName);
            textViewArtistName = itemView.findViewById(R.id.textViewArtistName);
            textViewInfoLine = itemView.findViewById(R.id.textViewInfoLine);
        }

        // Metode bind sekarang menerima listener sebagai parameter
        public void bind(Object item, int position, String screenType, OnTrackDeleteListener listener) {
            String trackName = "";
            String artistName = "";
            String imageUrl = null;
            String infoLine = "";

            // --- Logika Ekstraksi Data ---
            if (item instanceof LastFmModels.TrackDetail) {
                LastFmModels.TrackDetail apiTrack = (LastFmModels.TrackDetail) item;
                trackName = apiTrack.getName();
                artistName = apiTrack.getArtist().getName();
                if ("trending".equalsIgnoreCase(screenType)) {
                    infoLine = "Listeners: " + apiTrack.getListeners();
                } else {
                    infoLine = (apiTrack.getAlbum() != null && apiTrack.getAlbum().getTitle() != null)
                            ? "Album: " + apiTrack.getAlbum().getTitle() : "Album: N/A";
                }
                if (apiTrack.getAlbum() != null && !apiTrack.getAlbum().getImage().isEmpty()) {
                    int imageIndex = Math.min(2, apiTrack.getAlbum().getImage().size() - 1);
                    imageUrl = apiTrack.getAlbum().getImage().get(imageIndex).getUrl();
                }
            } else if (item instanceof Track) {
                Track dbTrack = (Track) item;
                trackName = dbTrack.getTrackName();
                artistName = dbTrack.getArtistName();
                imageUrl = dbTrack.getAlbumArtUrl();
                infoLine = ""; // Kosongkan info line untuk item dari playlist
            }

            // --- Tampilkan Data ke UI ---
            textViewTrackName.setText(trackName);
            textViewArtistName.setText(artistName);
            textViewInfoLine.setText(infoLine);
            textViewInfoLine.setVisibility(infoLine.isEmpty() ? View.GONE : View.VISIBLE);

            // ... Logika nomor dan gambar ...
            if ("trending".equalsIgnoreCase(screenType)) {
                textViewTrackNumber.setVisibility(View.VISIBLE);
                textViewTrackNumber.setText(String.valueOf(position));
            } else {
                textViewTrackNumber.setVisibility(View.GONE);
            }
            Glide.with(itemView.getContext()).load(imageUrl).placeholder(R.drawable.ic_launcher_foreground).error(R.drawable.ic_launcher_foreground).into(imageViewAlbumArt);

            // --- Logika untuk Tombol Hapus ---
            if ("playlistDetail".equalsIgnoreCase(screenType) && item instanceof Track) {
                buttonDeleteTrack.setVisibility(View.VISIBLE);
                final Track trackToDelete = (Track) item;
                buttonDeleteTrack.setOnClickListener(v -> {
                    if (listener != null) {
                        // Kirim ID dan Nama track yang akan dihapus ke Fragment
                        listener.onTrackDeleted(trackToDelete.getId(), trackToDelete.getTrackName());
                    }
                });
            } else {
                buttonDeleteTrack.setVisibility(View.GONE);
            }

            // --- Logika OnClickListener Universal untuk membuka detail ---
            final String finalTrackName = trackName;
            final String finalArtistName = artistName;
            itemView.setOnClickListener(v -> {
                if (finalTrackName != null && !finalTrackName.isEmpty() && finalArtistName != null && !finalArtistName.isEmpty()) {
                    Context context = itemView.getContext();
                    Intent intent = new Intent(context, TrackDetailActivity.class);
                    intent.putExtra(TrackDetailActivity.TRACK_NAME, finalTrackName);
                    intent.putExtra(TrackDetailActivity.ARTIST_NAME, finalArtistName);
                    context.startActivity(intent);
                }
            });
        }
    }
}