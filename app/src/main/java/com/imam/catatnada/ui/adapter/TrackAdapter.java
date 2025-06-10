package com.imam.catatnada.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.imam.catatnada.R;
import com.imam.catatnada.api.LastFmModels;
import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<LastFmModels.TrackDetail> trackList = new ArrayList<>();
    private String screenType = "trending"; // Defaultnya adalah 'trending'

    public void setTracks(List<LastFmModels.TrackDetail> trackList, String screenType) {
        this.trackList = trackList;
        this.screenType = screenType; // Simpan tipe layarnya
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
        LastFmModels.TrackDetail track = trackList.get(position);
        // Kirim posisi ke ViewHolder untuk ditampilkan sebagai nomor
        holder.bind(track, position + 1, screenType);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTrackNumber;
        private ImageView imageViewAlbumArt;
        private TextView textViewTrackName;
        private TextView textViewArtistName;
        private TextView textViewInfoLine;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTrackNumber = itemView.findViewById(R.id.textViewTrackNumber);
            imageViewAlbumArt = itemView.findViewById(R.id.imageViewAlbumArt);
            textViewTrackName = itemView.findViewById(R.id.textViewTrackName);
            textViewArtistName = itemView.findViewById(R.id.textViewArtistName);
            textViewInfoLine = itemView.findViewById(R.id.textViewInfoLine);
        }

        public void bind(LastFmModels.TrackDetail track, int position, String screenType) {
            textViewTrackName.setText(track.getName());
            textViewArtistName.setText(track.getArtist().getName());

            // Logika untuk menampilkan nomor atau tidak
            if ("trending".equalsIgnoreCase(screenType)) {
                textViewTrackNumber.setVisibility(View.VISIBLE);
                textViewTrackNumber.setText(String.valueOf(position));

                // Di layar trending, tampilkan listeners
                if (track.getListeners() != null) {
                    textViewInfoLine.setText("Listeners: " + track.getListeners());
                } else {
                    textViewInfoLine.setText("Listeners: -");
                }
            } else { // Jika dari layar search atau lainnya
                textViewTrackNumber.setVisibility(View.GONE); // Sembunyikan nomor

                // Di layar search, tampilkan nama album (sesuai ide Anda)
                if (track.getAlbum() != null && track.getAlbum().getTitle() != null) {
                    textViewInfoLine.setText(track.getAlbum().getTitle());
                } else {
                    textViewInfoLine.setText("N/A");
                }
            }

            // Logika pengambilan gambar tetap sama
            String imageUrl = null;
            if (track.getAlbum() != null && track.getAlbum().getImage() != null && !track.getAlbum().getImage().isEmpty()) {
                int lastImageIndex = track.getAlbum().getImage().size() - 1;
                imageUrl = track.getAlbum().getImage().get(lastImageIndex).getUrl();
                if (imageUrl != null && imageUrl.isEmpty()) {
                    imageUrl = null;
                }
            }

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageViewAlbumArt);
        }
    }
}