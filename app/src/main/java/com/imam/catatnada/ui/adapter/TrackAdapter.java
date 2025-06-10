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

    public void setTracks(List<LastFmModels.TrackDetail> trackList) {
        this.trackList = trackList;
        notifyDataSetChanged(); // Memberi tahu adapter bahwa data telah berubah
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
        holder.bind(track);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    // ViewHolder Class
    static class TrackViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewAlbumArt;
        private TextView textViewTrackName;
        private TextView textViewArtistName;
        private TextView textViewListeners;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAlbumArt = itemView.findViewById(R.id.imageViewAlbumArt);
            textViewTrackName = itemView.findViewById(R.id.textViewTrackName);
            textViewArtistName = itemView.findViewById(R.id.textViewArtistName);
            textViewListeners = itemView.findViewById(R.id.textViewListeners);
        }

        public void bind(LastFmModels.TrackDetail track) {
            textViewTrackName.setText(track.getName());
            textViewArtistName.setText(track.getArtist().getName());
            if (track.getListeners() != null) {
                textViewListeners.setText("Listeners: " + track.getListeners());
            } else {
                textViewListeners.setText("Listeners: -");
            }

            String imageUrl = null;

            // Fungsi untuk mendapatkan URL gambar album dengan mengecek semua ukuran gambar
            if (track.getAlbum() != null && track.getAlbum().getImage() != null && !track.getAlbum().getImage().isEmpty()) {

                int lastImageIndex = track.getAlbum().getImage().size() - 1;
                imageUrl = track.getAlbum().getImage().get(lastImageIndex).getUrl();

                if (imageUrl != null && imageUrl.isEmpty()) {
                    imageUrl = null;
                }
            }

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_launcher) // Gambar sementara saat memuat
                    .error(R.mipmap.ic_launcher_round) // Gambar jika gagal memuat
                    .into(imageViewAlbumArt);
        }
    }
}