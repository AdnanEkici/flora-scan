package com.example.florascan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.florascan.model.PlantResult;
import com.example.florascan.network.PlantNetApi;
import com.example.florascan.network.RetrofitClient;
import com.example.florascan.repository.PlantRepository;
import com.example.florascan.service.PlantIdentificationCallback;
import com.example.florascan.service.PlantNetIdentifier;
import com.example.florascan.service.PlantNetResultMapper;
import com.example.florascan.utils.ImagePreprocessor;

public class LoadingActivity extends AppCompatActivity
{
    private static final String EXTRA_IMAGE_URI = "selected_image_uri";
    private static final long MINIMUM_LOADING_DURATION_MILLISECONDS = 1800L;

    private final Handler mainThreadHandler =
        new Handler(Looper.getMainLooper());

    private PlantRepository plantRepository;
    private PlantNetIdentifier plantNetIdentifier;
    private Uri selectedImageUri;
    private long loadingStartedAtMilliseconds;

    public static Intent createIntent(Context context, Uri selectedImageUri)
    {
        Intent loadingIntent = new Intent(context, LoadingActivity.class);
        loadingIntent.putExtra(EXTRA_IMAGE_URI, selectedImageUri.toString());

        return loadingIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);

        loadingStartedAtMilliseconds = SystemClock.elapsedRealtime();
        selectedImageUri = getSelectedImageUri();

        if (selectedImageUri == null)
        {
            finish();
            return;
        }

        initializeDependencies();
        identifyPlant();
    }

    @Override
    protected void onDestroy()
    {
        mainThreadHandler.removeCallbacksAndMessages(null);

        if (plantNetIdentifier != null)
        {
            plantNetIdentifier.shutdown();
        }

        super.onDestroy();
    }

    private void initializeDependencies()
    {
        PlantNetApi plantNetApi =
            RetrofitClient.createPlantNetApi();

        ImagePreprocessor imagePreprocessor =
            new ImagePreprocessor(this);

        PlantNetResultMapper plantNetResultMapper =
            new PlantNetResultMapper();

        plantNetIdentifier = new PlantNetIdentifier(
            plantNetApi,
            imagePreprocessor,
            plantNetResultMapper,
            BuildConfig.PLANTNET_API_KEY
        );

        plantRepository = new PlantRepository(
            plantNetIdentifier
        );
    }

    private void identifyPlant()
    {
        plantRepository.identifyPlant(
                           selectedImageUri,
                           new PlantIdentificationCallback()
        {
            @Override
            public void onSuccess(PlantResult plantResult)
            {
                completeAfterMinimumLoadingDuration(
                    () -> openResultScreen(plantResult)
                );
            }

            @Override
            public void onFailure(String errorMessage)
            {
                completeAfterMinimumLoadingDuration(
                    () -> showIdentificationError(errorMessage)
                );
            }
        }
                       );
    }

    private void completeAfterMinimumLoadingDuration(Runnable completionAction)
    {
        long elapsedMilliseconds =
            SystemClock.elapsedRealtime() - loadingStartedAtMilliseconds;

        long remainingMilliseconds =
            MINIMUM_LOADING_DURATION_MILLISECONDS - elapsedMilliseconds;

        if (remainingMilliseconds <= 0)
        {
            completionAction.run();
            return;
        }

        mainThreadHandler.postDelayed(
                             completionAction,
                             remainingMilliseconds
                         );
    }

    private void openResultScreen(PlantResult plantResult)
    {
        Intent resultIntent =
            ResultActivity.createIntent(
                this,
                selectedImageUri,
                plantResult
            );

        startActivity(resultIntent);
        finish();
    }

    private void showIdentificationError(String errorMessage)
    {
        Toast.makeText(
                 this,
                 errorMessage,
                 Toast.LENGTH_LONG
             ).show();

        finish();
    }

    private Uri getSelectedImageUri()
    {
        String selectedImageUriValue =
            getIntent().getStringExtra(EXTRA_IMAGE_URI);

        if (selectedImageUriValue == null
                || selectedImageUriValue.isEmpty())
        {
            return null;
        }

        Uri imageUri = Uri.parse(selectedImageUriValue);

        return imageUri;
    }
}
