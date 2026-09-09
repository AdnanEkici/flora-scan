package com.example.florascan.network;

import com.example.florascan.model.PlantNetResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface PlantNetApi
{
    @Multipart
    @POST("v2/identify/all")
    Call<PlantNetResponse> identifyPlant(
        @Query("api-key") String apiKey,
        @Query("lang") String language,
        @Query("nb-results") int numberOfResults,
        @Part MultipartBody.Part image
    );
}
