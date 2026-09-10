package com.example.florascan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.florascan.model.PlantResult;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * Displays the result of a plant identification request.
 *
 * <p>The activity receives the selected plant image and identification data
 * through intent extras, reconstructs a {@link PlantResult}, and presents the
 * plant's common name, scientific name, taxonomy, and identification
 * confidence. It also allows the user to share the identification result or
 * return to the main screen to identify another plant.</p>
 *
 * <p>Instances of this activity should be launched through
 * {@link #createIntent(Context, Uri, PlantResult)} so that all required result
 * data is supplied consistently.</p>
 */
public class ResultActivity extends AppCompatActivity
{

    private static final double HIGH_CONFIDENCE_THRESHOLD = 0.80;
    private static final double MODERATE_CONFIDENCE_THRESHOLD = 0.50;

    private static final String HIGH_CONFIDENCE_COLOR = "#2E7D32";
    private static final String HIGH_CONFIDENCE_VALUE_COLOR = "#1B5E20";

    private static final String MODERATE_CONFIDENCE_COLOR = "#F59E0B";
    private static final String MODERATE_CONFIDENCE_VALUE_COLOR = "#D97706";

    private static final String LOW_CONFIDENCE_COLOR = "#D32F2F";
    private static final String LOW_CONFIDENCE_VALUE_COLOR = "#B71C1C";

    private static final String EXTRA_IMAGE_URI = "selected_image_uri";
    private static final String EXTRA_COMMON_NAME = "common_name";
    private static final String EXTRA_SCIENTIFIC_NAME = "scientific_name";
    private static final String EXTRA_FAMILY = "family";
    private static final String EXTRA_GENUS = "genus";
    private static final String EXTRA_CONFIDENCE = "confidence";
    private static final String EXTRA_DESCRIPTION = "description";
    private ImageView backButton;
    private ImageView shareButton;
    private ImageView plantImage;

    private TextView confidenceBadgeValue;
    private TextView commonName;
    private TextView scientificName;
    private TextView familyValue;
    private TextView genusValue;
    private TextView confidenceValue;
    private TextView confidenceLabel;

    private MaterialCardView confidenceBadge;
    private MaterialCardView identifyAnotherButton;

    private Uri selectedImageUri;
    private PlantResult plantResult;

    /**
     * Creates an intent containing the data required to display a plant
     * identification result.
     *
     * @param context the context used to create the intent
     * @param selectedImageUri the URI of the image used for identification
     * @param plantResult the identified plant data to display
     * @return an intent configured to launch {@link ResultActivity}
     */
    public static Intent createIntent(
        Context context,
        Uri selectedImageUri,
        PlantResult plantResult)
    {
        Intent resultIntent = new Intent(context, ResultActivity.class);

        resultIntent.putExtra(
                        EXTRA_IMAGE_URI,
                        selectedImageUri.toString()
                    );

        resultIntent.putExtra(
                        EXTRA_COMMON_NAME,
                        plantResult.getCommonName()
                    );

        resultIntent.putExtra(
                        EXTRA_SCIENTIFIC_NAME,
                        plantResult.getScientificName()
                    );

        resultIntent.putExtra(
                        EXTRA_FAMILY,
                        plantResult.getFamily()
                    );

        resultIntent.putExtra(
                        EXTRA_GENUS,
                        plantResult.getGenus()
                    );

        resultIntent.putExtra(
                        EXTRA_CONFIDENCE,
                        plantResult.getConfidence()
                    );

        return resultIntent;
    }

    /**
     * Initializes the result screen, loads identification data from the launching
     * intent, configures user interactions, and displays the plant result.
     *
     * <p>If the required image URI is unavailable, the activity is closed because
     * the result screen cannot be displayed correctly.</p>
     *
     * @param savedInstanceState the previously saved activity state, or
     *                           {@code null} when the activity is created
     *                           for the first time
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result);

        initializeViews();

        if (!loadResultData())
        {
            finish();
            return;
        }

        initializeListeners();
        displayResult();
    }

    /**
     * Resolves and stores references to the views used by the result screen.
     */
    private void initializeViews()
    {
        backButton = findViewById(R.id.backButton);
        shareButton = findViewById(R.id.shareButton);
        plantImage = findViewById(R.id.plantImage);

        confidenceBadgeValue = findViewById(R.id.confidenceBadgeValue);
        commonName = findViewById(R.id.commonName);
        scientificName = findViewById(R.id.scientificName);
        familyValue = findViewById(R.id.familyValue);
        genusValue = findViewById(R.id.genusValue);
        confidenceValue = findViewById(R.id.confidenceValue);
        confidenceBadge = findViewById(R.id.confidenceBadge);
        confidenceLabel = findViewById(R.id.confidenceLabel);
        identifyAnotherButton =
            findViewById(R.id.identifyAnotherButton);
    }

    /**
     * Configures the navigation, sharing, and identify-another user interactions.
     */
    private void initializeListeners()
    {
        backButton.setOnClickListener(view -> finish());

        shareButton.setOnClickListener(
                       view -> sharePlantResult()
                   );

        identifyAnotherButton.setOnClickListener(
                                 view -> returnToMainScreen()
                             );
    }

    /**
     * Reads the selected image URI and plant identification data from the
     * launching intent and reconstructs the {@link PlantResult} used by the
     * screen.
     *
     * @return {@code true} when the required result data can be loaded;
     *         {@code false} when the image URI is missing
     */
    private boolean loadResultData()
    {
        String imageUriValue =
            getIntent().getStringExtra(EXTRA_IMAGE_URI);

        if (imageUriValue == null || imageUriValue.isEmpty())
        {
            return false;
        }

        selectedImageUri = Uri.parse(imageUriValue);

        String plantCommonName =
            getIntent().getStringExtra(EXTRA_COMMON_NAME);

        String plantScientificName =
            getIntent().getStringExtra(EXTRA_SCIENTIFIC_NAME);

        String plantFamily =
            getIntent().getStringExtra(EXTRA_FAMILY);

        String plantGenus =
            getIntent().getStringExtra(EXTRA_GENUS);

        double plantConfidence =
            getIntent().getDoubleExtra(
                EXTRA_CONFIDENCE,
                0.0
            );

        plantResult = new PlantResult(
            plantCommonName,
            plantScientificName,
            plantFamily,
            plantGenus,
            plantConfidence
        );

        return true;
    }

    /**
     * Displays the selected plant image, identification details, taxonomy, and
     * formatted confidence values on the result screen.
     */
    private void displayResult()
    {
        Glide.with(this)
             .load(selectedImageUri)
             .fitCenter()
             .into(plantImage);

        commonName.setText(plantResult.getCommonName());
        scientificName.setText(plantResult.getScientificName());
        familyValue.setText(plantResult.getFamily());
        genusValue.setText(plantResult.getGenus());

        String confidencePercentage =
            formatConfidencePercentage(
                plantResult.getConfidence()
            );

        String confidenceBadgePercentage =
            formatConfidenceBadgePercentage(
                plantResult.getConfidence()
            );

        confidenceValue.setText(confidencePercentage);

        updateConfidenceState(plantResult.getConfidence());
        confidenceBadgeValue.setText(confidenceBadgePercentage);
    }

    /**
     * Converts a normalized confidence score into a percentage with one decimal
     * place.
     *
     * @param confidence the normalized confidence score, where {@code 1.0}
     *                   represents 100 percent confidence
     * @return the formatted confidence percentage, such as {@code "92.0%"}
     */
    private String formatConfidencePercentage(double confidence)
    {
        double confidencePercentage = confidence * 100.0;

        String formattedConfidence =
            String.format(
                Locale.US,
                "%.1f%%",
                confidencePercentage
            );

        return formattedConfidence;
    }

    /**
     * Converts a normalized confidence score into a whole-number percentage for
     * display in the confidence badge.
     *
     * @param confidence the normalized confidence score, where {@code 1.0}
     *                   represents 100 percent confidence
     * @return the formatted confidence percentage, such as {@code "92%"}
     */
    private String formatConfidenceBadgePercentage(double confidence)
    {
        double confidencePercentage = confidence * 100.0;

        String formattedConfidence =
            String.format(
                Locale.US,
                "%.0f%%",
                confidencePercentage
            );

        return formattedConfidence;
    }

    /**
     * Opens the Android share chooser with a text summary containing the plant's
     * common name, scientific name, and identification confidence.
     */
    private void sharePlantResult()
    {
        String shareMessage =
            plantResult.getCommonName()
            + "\n"
            + plantResult.getScientificName()
            + "\nConfidence: "
            + formatConfidencePercentage(
                plantResult.getConfidence()
            );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(
                       Intent.EXTRA_TEXT,
                       shareMessage
                   );

        Intent chooserIntent =
            Intent.createChooser(
                shareIntent,
                "Share plant"
            );

        startActivity(chooserIntent);
    }

    /**
     * Returns to {@link MainActivity} and clears intermediate activities from the
     * navigation stack so the user can begin another plant identification.
     */
    private void returnToMainScreen()
    {
        Intent mainIntent =
            new Intent(this, MainActivity.class);

        mainIntent.addFlags(
                      Intent.FLAG_ACTIVITY_CLEAR_TOP
                      | Intent.FLAG_ACTIVITY_SINGLE_TOP
                  );

        startActivity(mainIntent);
        finish();
    }

    /**
     * Updates the confidence label and badge colors based on the given
     * identification confidence score.
     *
     * <p>The confidence value is expected to be normalized between {@code 0.0}
     * and {@code 1.0}. Both the confidence container and percentage badge are
     * styled according to the configured confidence thresholds.</p>
     *
     * @param confidence the normalized identification confidence score
     */
    private void updateConfidenceState(double confidence)
    {
        if (confidence >= HIGH_CONFIDENCE_THRESHOLD)
        {
            confidenceLabel.setText("High Confidence");

            confidenceBadge.setCardBackgroundColor(
                               android.graphics.Color.parseColor(HIGH_CONFIDENCE_COLOR)
                           );

            confidenceBadgeValue.setBackgroundColor(
                                    android.graphics.Color.parseColor(HIGH_CONFIDENCE_VALUE_COLOR)
                                );
        }
        else if (confidence >= MODERATE_CONFIDENCE_THRESHOLD)
        {
            confidenceLabel.setText("Moderate Confidence");

            confidenceBadge.setCardBackgroundColor(
                               android.graphics.Color.parseColor(MODERATE_CONFIDENCE_COLOR)
                           );

            confidenceBadgeValue.setBackgroundColor(
                                    android.graphics.Color.parseColor(MODERATE_CONFIDENCE_VALUE_COLOR)
                                );
        }
        else
        {
            confidenceLabel.setText("Low Confidence");

            confidenceBadge.setCardBackgroundColor(
                               android.graphics.Color.parseColor(LOW_CONFIDENCE_COLOR)
                           );

            confidenceBadgeValue.setBackgroundColor(
                                    android.graphics.Color.parseColor(LOW_CONFIDENCE_VALUE_COLOR)
                                );
        }
    }

}
