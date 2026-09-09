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

/**
 * Identifies plants using the Pl@ntNet plant identification service.
 *
 * <p>The identifier implements the provider-independent
 * {@link PlantIdentifier} contract and encapsulates all Pl@ntNet-specific
 * request preparation, network communication, response handling, and result
 * mapping.</p>
 *
 * <p>Before an identification request is sent, the supplied image is
 * processed by {@link ImagePreprocessor} on a background executor. The
 * resulting temporary JPEG file is converted to multipart form data and
 * submitted through {@link PlantNetApi}. Successful API responses are mapped
 * from the provider-specific response model to the application's
 * {@link PlantResult} domain model using {@link PlantNetResultMapper}.</p>
 *
 * <p>Temporary upload files are deleted after the network request completes,
 * whether the request succeeds or fails. The internal executor should be
 * released through {@link #shutdown()} when the identifier is no longer
 * required.</p>
 */
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
    /**
     * Creates a Pl@ntNet plant identifier with the dependencies required for
     * image preparation, network communication, and response mapping.
     *
     * @param plantNetApi the API client used to communicate with Pl@ntNet
     * @param imagePreprocessor the component used to prepare images for upload
     * @param plantNetResultMapper the mapper used to convert provider responses
     *                             into application plant results
     * @param apiKey the API key used to authenticate Pl@ntNet requests
     */
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
    /**
     * Starts asynchronous identification of the supplied plant image.
     *
     * <p>Image preprocessing is performed on the identifier's background
     * executor before the resulting image is submitted to Pl@ntNet. The supplied
     * callback receives either the mapped identification result or an error
     * describing the failure.</p>
     *
     * @param imageUri the URI of the image to identify
     * @param identificationCallback the callback that receives the identification
     *                               result or failure
     */
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
    /**
     * Shuts down the background executor used for image preprocessing.
     *
     * <p>This method should be called when the identifier is no longer required
     * so that its executor resources can be released.</p>
     */
    public void shutdown()
    {
        executorService.shutdown();
    }
    /**
     * Prepares the selected image for upload and submits it to the Pl@ntNet
     * identification API.
     *
     * <p>If image preprocessing fails, the request is not sent and the failure is
     * reported through the supplied callback.</p>
     *
     * @param imageUri the URI of the image to prepare and identify
     * @param identificationCallback the callback that receives the identification
     *                               result or failure
     */
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
    /**
     * Creates the multipart form-data part used to upload the processed JPEG
     * image to Pl@ntNet.
     *
     * @param imageFile the processed JPEG file to upload
     * @return the multipart image part used by the identification request
     */
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
    /**
     * Processes a completed Pl@ntNet response and reports either a mapped plant
     * result or an appropriate failure through the supplied callback.
     *
     * <p>The temporary upload file is deleted before the response is evaluated.
     * Unsuccessful HTTP responses, empty response bodies, and responses without a
     * usable plant match are treated as identification failures.</p>
     *
     * @param response the HTTP response returned by Pl@ntNet
     * @param imageFile the temporary image file created for the request
     * @param identificationCallback the callback that receives the result or
     *                               failure
     */
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
    /**
     * Deletes the temporary image file created for the identification request
     * when it exists.
     *
     * @param imageFile the temporary image file to delete
     */
    private void deleteTemporaryFile(File imageFile)
    {
        if (imageFile == null || !imageFile.exists())
        {
            return;
        }

        imageFile.delete();
    }
}
