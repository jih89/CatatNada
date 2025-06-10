package com.imam.catatnada.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    // Endpoint untuk mengambil Top Tracks
    @GET("?method=chart.gettoptracks&format=json")
    Call<LastFmModels.TopTracksResponse> getTopTracks(
            @Query("api_key") String apiKey,
            @Query("limit") int limit // Parameter untuk membatasi jumlah hasil
    );

    // Endpoint untuk mengambil detil dari lagu spesifik
    @GET("?method=track.getInfo&format=json")
    Call<LastFmModels.TrackInfoResponse> getTrackInfo(
            @Query("api_key") String apiKey,
            @Query("artist") String artist,
            @Query("track") String track
    );
}
