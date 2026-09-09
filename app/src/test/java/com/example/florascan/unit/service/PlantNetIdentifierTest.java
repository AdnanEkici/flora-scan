package com.example.florascan.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import com.example.florascan.model.PlantNetResponse;
import com.example.florascan.model.PlantResult;
import com.example.florascan.network.PlantNetApi;
import com.example.florascan.service.PlantIdentificationCallback;
import com.example.florascan.service.PlantNetIdentifier;
import com.example.florascan.service.PlantNetResultMapper;
import com.example.florascan.utils.ImagePreprocessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlantNetIdentifierTest
{
    private static final String API_KEY = "test-api-key";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private PlantNetApi plantNetApi;
    private ImagePreprocessor imagePreprocessor;
    private PlantNetResultMapper plantNetResultMapper;
    private PlantNetIdentifier plantNetIdentifier;

    @Before
    public void setUp()
    {
        plantNetApi = mock(PlantNetApi.class);
        imagePreprocessor = mock(ImagePreprocessor.class);
        plantNetResultMapper = mock(PlantNetResultMapper.class);

        plantNetIdentifier = new PlantNetIdentifier(
            plantNetApi,
            imagePreprocessor,
            plantNetResultMapper,
            API_KEY
        );
    }

    @After
    public void tearDown()
    {
        plantNetIdentifier.shutdown();
    }

    /* Verifies that preprocessing failures are reported through the failure callback. */
    @Test
    public void identifyPlantReportsImagePreprocessingFailure() throws Exception
    {
        Uri imageUri = mock(Uri.class);
        PlantIdentificationCallback identificationCallback =
        mock(PlantIdentificationCallback.class);

        CountDownLatch completionLatch = new CountDownLatch(1);

        when(imagePreprocessor.prepareForUpload(imageUri))
        .thenThrow(new IOException());

        org.mockito.Mockito.doAnswer(invocation ->
        {
            completionLatch.countDown();
            return null;
        }).when(identificationCallback)
        .onFailure(any(String.class));

        plantNetIdentifier.identifyPlant(
                              imageUri,
                              identificationCallback
                          );

        completionLatch.await(2, TimeUnit.SECONDS);

        verify(identificationCallback).onFailure(
            "Unable to prepare the selected image."
        );
    }

    /* Verifies that a successful PlantNet response is mapped and returned to the caller. */
    @Test
    public void identifyPlantReturnsMappedPlantResult() throws Exception
    {
        Uri imageUri = mock(Uri.class);
        PlantIdentificationCallback identificationCallback =
        mock(PlantIdentificationCallback.class);

        File imageFile =
        temporaryFolder.newFile("plant.jpg");

        PlantNetResponse plantNetResponse =
        mock(PlantNetResponse.class);

        PlantResult plantResult =
        mock(PlantResult.class);

        @SuppressWarnings("unchecked")
        Call<PlantNetResponse> identificationCall =
        mock(Call.class);

        when(imagePreprocessor.prepareForUpload(imageUri))
        .thenReturn(imageFile);

        when(plantNetApi.identifyPlant(
                 eq(API_KEY),
                 eq("en"),
                 eq(1),
                 any(MultipartBody.Part.class)
             )).thenReturn(identificationCall);

        when(plantNetResultMapper.map(plantNetResponse))
        .thenReturn(plantResult);

        CountDownLatch completionLatch = new CountDownLatch(1);

        org.mockito.Mockito.doAnswer(invocation ->
        {
            Callback<PlantNetResponse> callback =
            invocation.getArgument(0);

            callback.onResponse(
                        identificationCall,
                        Response.success(plantNetResponse)
                    );

            return null;
        }).when(identificationCall)
        .enqueue(any());

        org.mockito.Mockito.doAnswer(invocation ->
        {
            completionLatch.countDown();
            return null;
        }).when(identificationCallback)
        .onSuccess(plantResult);

        plantNetIdentifier.identifyPlant(
                              imageUri,
                              identificationCallback
                          );

        completionLatch.await(2, TimeUnit.SECONDS);

        verify(identificationCallback)
        .onSuccess(plantResult);
    }

    /* Verifies that unsuccessful PlantNet HTTP responses are reported as identification failures. */
    @Test
    public void identifyPlantReportsUnsuccessfulApiResponse() throws Exception
    {
        Uri imageUri = mock(Uri.class);
        PlantIdentificationCallback identificationCallback =
        mock(PlantIdentificationCallback.class);

        File imageFile =
        temporaryFolder.newFile("plant.jpg");

        @SuppressWarnings("unchecked")
        Call<PlantNetResponse> identificationCall =
        mock(Call.class);

        when(imagePreprocessor.prepareForUpload(imageUri))
        .thenReturn(imageFile);

        when(plantNetApi.identifyPlant(
                 eq(API_KEY),
                 eq("en"),
                 eq(1),
                 any(MultipartBody.Part.class)
             )).thenReturn(identificationCall);

        CountDownLatch completionLatch = new CountDownLatch(1);

        org.mockito.Mockito.doAnswer(invocation ->
        {
            Callback<PlantNetResponse> callback =
            invocation.getArgument(0);

            callback.onResponse(
                        identificationCall,
                        Response.error(
                            500,
                            okhttp3.ResponseBody.create(
                                null,
                                ""
                            )
                        )
                    );

            return null;
        }).when(identificationCall)
        .enqueue(any());

        org.mockito.Mockito.doAnswer(invocation ->
        {
            completionLatch.countDown();
            return null;
        }).when(identificationCallback)
        .onFailure(any(String.class));

        plantNetIdentifier.identifyPlant(
                              imageUri,
                              identificationCallback
                          );

        completionLatch.await(2, TimeUnit.SECONDS);

        verify(identificationCallback).onFailure(
            "Plant identification request failed."
        );
    }

    /* Verifies that network failures are reported through the identification failure callback. */
    @Test
    public void identifyPlantReportsNetworkFailure() throws Exception
    {
        Uri imageUri = mock(Uri.class);
        PlantIdentificationCallback identificationCallback =
        mock(PlantIdentificationCallback.class);

        File imageFile =
        temporaryFolder.newFile("plant.jpg");

        @SuppressWarnings("unchecked")
        Call<PlantNetResponse> identificationCall =
        mock(Call.class);

        when(imagePreprocessor.prepareForUpload(imageUri))
        .thenReturn(imageFile);

        when(plantNetApi.identifyPlant(
                 eq(API_KEY),
                 eq("en"),
                 eq(1),
                 any(MultipartBody.Part.class)
             )).thenReturn(identificationCall);

        CountDownLatch completionLatch = new CountDownLatch(1);

        org.mockito.Mockito.doAnswer(invocation ->
        {
            Callback<PlantNetResponse> callback =
            invocation.getArgument(0);

            callback.onFailure(
                        identificationCall,
                        new IOException("Network unavailable")
                    );

            return null;
        }).when(identificationCall)
        .enqueue(any());

        org.mockito.Mockito.doAnswer(invocation ->
        {
            completionLatch.countDown();
            return null;
        }).when(identificationCallback)
        .onFailure(any(String.class));

        plantNetIdentifier.identifyPlant(
                              imageUri,
                              identificationCallback
                          );

        completionLatch.await(2, TimeUnit.SECONDS);

        verify(identificationCallback).onFailure(
            "Unable to identify the plant. Please try again."
        );
    }

    /* Verifies that a response without a mapped plant match is reported as a failure. */
    @Test
    public void identifyPlantReportsMissingPlantMatch() throws Exception
    {
        Uri imageUri = mock(Uri.class);
        PlantIdentificationCallback identificationCallback =
        mock(PlantIdentificationCallback.class);

        File imageFile =
        temporaryFolder.newFile("plant.jpg");

        PlantNetResponse plantNetResponse =
        mock(PlantNetResponse.class);

        @SuppressWarnings("unchecked")
        Call<PlantNetResponse> identificationCall =
        mock(Call.class);

        when(imagePreprocessor.prepareForUpload(imageUri))
        .thenReturn(imageFile);

        when(plantNetApi.identifyPlant(
                 eq(API_KEY),
                 eq("en"),
                 eq(1),
                 any(MultipartBody.Part.class)
             )).thenReturn(identificationCall);

        when(plantNetResultMapper.map(plantNetResponse))
        .thenReturn(null);

        CountDownLatch completionLatch = new CountDownLatch(1);

        org.mockito.Mockito.doAnswer(invocation ->
        {
            Callback<PlantNetResponse> callback =
            invocation.getArgument(0);

            callback.onResponse(
                        identificationCall,
                        Response.success(plantNetResponse)
                    );

            return null;
        }).when(identificationCall)
        .enqueue(any());

        org.mockito.Mockito.doAnswer(invocation ->
        {
            completionLatch.countDown();
            return null;
        }).when(identificationCallback)
        .onFailure(any(String.class));

        plantNetIdentifier.identifyPlant(
                              imageUri,
                              identificationCallback
                          );

        completionLatch.await(2, TimeUnit.SECONDS);

        verify(identificationCallback).onFailure(
            "No plant match was found."
        );
    }
}
