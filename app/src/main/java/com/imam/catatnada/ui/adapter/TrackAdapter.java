package com.imam.catatnada.ui.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.imam.catatnada.ui.activity.TrackDetailActivity;

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
        // 1. Dapatkan objek track yang benar untuk posisi ini
        LastFmModels.TrackDetail track = trackList.get(position);

        // 2. Panggil metode bind untuk menampilkan data ke view
        holder.bind(track, position + 1, screenType);

        // 3. 🔽 SET LISTENER DI SINI, DI TEMPAT YANG BENAR 🔽
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, TrackDetailActivity.class);

            // Gunakan objek 'track' yang sudah kita dapatkan di atas
            intent.putExtra(TrackDetailActivity.TRACK_NAME, track.getName());
            intent.putExtra(TrackDetailActivity.ARTIST_NAME, track.getArtist().getName());

            context.startActivity(intent);
        });
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
            // --- Bagian 1: Set data yang selalu sama ---
            textViewTrackName.setText(track.getName());
            textViewArtistName.setText(track.getArtist().getName());

            // --- Bagian 2: Logika untuk penomoran dan baris info berdasarkan screenType ---
            if ("trending".equalsIgnoreCase(screenType)) {
                // Tampilkan nomor urut & listeners untuk layar TRENDING
                textViewTrackNumber.setVisibility(View.VISIBLE);
                textViewTrackNumber.setText(String.valueOf(position));

                if (track.getListeners() != null) {
                    textViewInfoLine.setText("Listeners: " + track.getListeners());
                } else {
                    textViewInfoLine.setText("Listeners: -");
                }
            } else { // Asumsikan ini untuk layar SEARCH atau lainnya
                // Sembunyikan nomor urut & tampilkan nama album
                textViewTrackNumber.setVisibility(View.GONE);

                if (track.getAlbum() != null && track.getAlbum().getTitle() != null) {
                    // Kita ubah agar lebih jelas, misal: "Album: [Nama Album]"
                    textViewInfoLine.setText("Album: " + track.getAlbum().getTitle());
                } else {
                    textViewInfoLine.setText("Album: N/A");
                }
            }

            // --- Bagian 3: Logika untuk memuat gambar (berjalan untuk semua screenType) ---
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
                    .placeholder(R.drawable.ic_launcher_foreground) // Gunakan placeholder netral
                    .error(R.drawable.ic_launcher_foreground)      // Gunakan placeholder netral
                    .into(imageViewAlbumArt);
        }
    }
}