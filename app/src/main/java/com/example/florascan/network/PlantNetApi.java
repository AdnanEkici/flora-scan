package com.example.florascan.network;

import com.example.florascan.model.PlantNetResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Defines the Retrofit contract for communicating with the Pl@ntNet plant
 * identification API.
 *
 * <p>The interface declares the multipart HTTP request used to submit a plant
 * image together with the API key, response language, and requested result
 * count. Retrofit generates the concrete implementation at runtime.</p>
 *
 * <p>The current endpoint targets Pl@ntNet's general identification project
 * and returns the provider-specific response model used by the service layer.</p>
 */
public interface PlantNetApi
{
    /**
     * Submits an image to the Pl@ntNet identification endpoint.
     *
     * @param apiKey the API key used to authenticate the request
     * @param language the language requested for localized response data
     * @param numberOfResults the maximum number of identification results to return
     * @param image the multipart image payload to identify
     * @return a Retrofit call containing the Pl@ntNet identification response
     */
    @Multipart
    @POST("v2/identify/all")
    Call<PlantNetResponse> identifyPlant(
        @Query("api-key") String apiKey,
        @Query("lang") String language,
        @Query("nb-results") int numberOfResults,
        @Part MultipartBody.Part image
    );
}
