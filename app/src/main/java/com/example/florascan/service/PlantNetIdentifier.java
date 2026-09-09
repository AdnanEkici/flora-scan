package com.example.florascan.service;

import android.net.Uri;

import com.example.florascan.model.PlantNetResponse;
import com.example.florascan.model.PlantResult;
import com.example.florascan.network.PlantNetApi;
import com.example.florascan.utils.ImagePreprocessor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlantNetIdentifier implements PlantIdentifier
{
    private static final String IMAGE_MEDIA_TYPE = "image/jpeg";
    private static final String IMAGE_FORM_NAME = "images";
    private static final String LANGUAGE = "en";
    private static final int NUMBER_OF_RESULTS = 1;

    private final PlantNetApi plantNetApi;
    private final ImagePreprocessor imagePreprocessor;
    private final PlantNetResultMapper plantNetResultMapper;
    private final String apiKey;
    private final ExecutorService executorService;

    public PlantNetIdentifier(
        PlantNetApi plantNetApi,
        ImagePreprocessor imagePreprocessor,
        PlantNetResultMapper plantNetResultMapper,
        String apiKey)
    {
        this.plantNetApi = plantNetApi;
        this.imagePreprocessor = imagePreprocessor;
        this.plantNetResultMapper = plantNetResultMapper;
        this.apiKey = apiKey;
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void identifyPlant(
        Uri imageUri,
        PlantIdentificationCallback identificationCallback)
    {
        executorService.execute(
                           () -> prepareAndIdentifyPlant(
                               imageUri,
                               identificationCallback
                           )
                       );
    }

    public void shutdown()
    {
        executorService.shutdown();
    }

    private void prepareAndIdentifyPlant(
        Uri imageUri,
        PlantIdentificationCallback identificationCallback)
    {
        File imageFile;

        try
        {
            imageFile = imagePreprocessor.prepareForUpload(imageUri);
        }
        catch (IOException exception)
        {
            identificationCallback.onFailure(
                                      "Unable to prepare the selected image."
                                  );

            return;
        }

        MultipartBody.Part imagePart = createImagePart(imageFile);

        Call<PlantNetResponse> identificationCall =
            plantNetApi.identifyPlant(
                apiKey,
                LANGUAGE,
                NUMBER_OF_RESULTS,
                imagePart
            );

        identificationCall.enqueue(
                              new Callback<PlantNetResponse>()
        {
            @Override
            public void onResponse(
                Call<PlantNetResponse> call,
                Response<PlantNetResponse> response)
            {
                handleResponse(
                    response,
                    imageFile,
                    identificationCallback
                );
            }

            @Override
            public void onFailure(
                Call<PlantNetResponse> call,
                Throwable throwable)
            {
                deleteTemporaryFile(imageFile);

                identificationCallback.onFailure(
                                          "Unable to identify the plant. Please try again."
                                      );
            }
        }
                          );
    }

    private MultipartBody.Part createImagePart(File imageFile)
    {
        MediaType imageMediaType = MediaType.parse(IMAGE_MEDIA_TYPE);

        RequestBody imageRequestBody = RequestBody.create(
                                           imageMediaType,
                                           imageFile
                                       );

        MultipartBody.Part imagePart =
            MultipartBody.Part.createFormData(
                IMAGE_FORM_NAME,
                imageFile.getName(),
                imageRequestBody
            );

        return imagePart;
    }

    private void handleResponse(
        Response<PlantNetResponse> response,
        File imageFile,
        PlantIdentificationCallback identificationCallback)
    {
        deleteTemporaryFile(imageFile);

        if (!response.isSuccessful())
        {
            identificationCallback.onFailure(
                                      "Plant identification request failed."
                                  );

            return;
        }

        PlantNetResponse plantNetResponse = response.body();

        if (plantNetResponse == null)
        {
            identificationCallback.onFailure(
                                      "Plant identification returned an empty response."
                                  );

            return;
        }

        PlantResult plantResult =
            plantNetResultMapper.map(plantNetResponse);

        if (plantResult == null)
        {
            identificationCallback.onFailure(
                                      "No plant match was found."
                                  );

            return;
        }

        identificationCallback.onSuccess(plantResult);
    }

    private void deleteTemporaryFile(File imageFile)
    {
        if (imageFile == null || !imageFile.exists())
        {
            return;
        }

        imageFile.delete();
    }
}
