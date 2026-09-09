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

/**
 * Coordinates the plant identification process while displaying the loading
 * screen.
 *
 * <p>The activity receives the URI of the image selected for identification,
 * initializes the identification dependencies, and delegates the request to
 * {@link PlantRepository}. The selected image is preprocessed and submitted
 * to the configured plant identification provider through
 * {@link PlantNetIdentifier}.</p>
 *
 * <p>Successful identifications are forwarded to {@link ResultActivity},
 * while failures are presented to the user before the activity is closed.
 * A minimum loading duration is applied so that very fast responses do not
 * cause an abrupt transition between the preview and result screens.</p>
 *
 * <p>The activity also owns the lifecycle of the identifier's background
 * executor and releases pending callbacks and resources when destroyed.</p>
 */
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

    /**
     * Creates an intent configured to start the loading screen with the image
     * selected for plant identification.
     *
     * @param context the context used to create the intent
     * @param selectedImageUri the URI of the image to identify
     * @return an intent configured to launch {@link LoadingActivity}
     */
    public static Intent createIntent(Context context, Uri selectedImageUri)
    {
        Intent loadingIntent = new Intent(context, LoadingActivity.class);
        loadingIntent.putExtra(EXTRA_IMAGE_URI, selectedImageUri.toString());

        return loadingIntent;
    }

    /**
     * Initializes the loading screen, retrieves the selected image URI,
     * constructs the identification dependencies, and starts plant
     * identification.
     *
     * <p>If the required image URI is missing, the activity is closed because an
     * identification request cannot be performed.</p>
     *
     * @param savedInstanceState the previously saved activity state, or
     *                           {@code null} when the activity is created
     *                           for the first time
     */
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

    /**
     * Releases pending main-thread callbacks and shuts down the plant
     * identification executor when the activity is destroyed.
     */
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

    /**
     * Constructs the dependencies required for plant identification.
     *
     * <p>This initializes the Pl@ntNet API client, image preprocessor, response
     * mapper, provider-specific identifier, and repository used by the loading
     * flow.</p>
     */
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

    /**
     * Starts asynchronous identification of the selected image.
     *
     * <p>A successful identification is forwarded to the result screen after the
     * minimum loading duration has elapsed. Failed identification requests are
     * handled through the same timing mechanism before an error is shown.</p>
     */
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

    /**
     * Executes the supplied completion action after ensuring that the loading
     * screen has been visible for at least the configured minimum duration.
     *
     * @param completionAction the action to execute after the loading duration
     *                         requirement has been satisfied
     */
    private void completeAfterMinimumLoadingDuration(Runnable completionAction)
    {
        long elapsedMilliseconds =
            SystemClock.elapsedRealtime() - loadingStartedAtMilliseconds;

        long remainingMilliseconds =
            MINIMUM_LOADING_DURATION_MILLISECONDS - elapsedMilliseconds;

        if (remainingMilliseconds <= 0)
        {
            mainThreadHandler.post(completionAction);
            return;
        }

        mainThreadHandler.postDelayed(
                             completionAction,
                             remainingMilliseconds
                         );
    }

    /**
     * Opens {@link ResultActivity} with the selected image and identified plant
     * data, then closes the loading screen.
     *
     * @param plantResult the successfully identified plant result
     */
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

    /**
     * Displays an identification failure message to the user and closes the
     * loading screen.
     *
     * @param errorMessage the message describing the identification failure
     */
    private void showIdentificationError(String errorMessage)
    {
        Toast.makeText(
                 this,
                 errorMessage,
                 Toast.LENGTH_LONG
             ).show();

        finish();
    }
    /**
     * Reads and parses the selected image URI supplied through the launching
     * intent.
     *
     * @return the selected image URI, or {@code null} when the required intent
     *         extra is missing or empty
     */
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
