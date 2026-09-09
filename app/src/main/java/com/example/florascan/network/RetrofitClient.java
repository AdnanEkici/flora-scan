package com.example.florascan.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient
{
    private static final String BASE_URL =
        "https://my-api.plantnet.org/";

    private static final Retrofit RETROFIT =
        new Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build();

    private RetrofitClient()
    {
    }

    public static PlantNetApi createPlantNetApi()
    {
        PlantNetApi plantNetApi =
            RETROFIT.create(PlantNetApi.class);

        return plantNetApi;
    }
}
