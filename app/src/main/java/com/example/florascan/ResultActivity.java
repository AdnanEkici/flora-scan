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

public class ResultActivity extends AppCompatActivity
{
    private static final String EXTRA_IMAGE_URI = "selected_image_uri";
    private static final String EXTRA_COMMON_NAME = "common_name";
    private static final String EXTRA_SCIENTIFIC_NAME = "scientific_name";
    private static final String EXTRA_FAMILY = "family";
    private static final String EXTRA_GENUS = "genus";
    private static final String EXTRA_CONFIDENCE = "confidence";

    private ImageView backButton;
    private ImageView shareButton;
    private ImageView plantImage;

    private TextView confidenceBadgeValue;
    private TextView commonName;
    private TextView scientificName;
    private TextView familyValue;
    private TextView genusValue;
    private TextView confidenceValue;

    private MaterialCardView identifyAnotherButton;

    private Uri selectedImageUri;
    private PlantResult plantResult;

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

        identifyAnotherButton =
            findViewById(R.id.identifyAnotherButton);
    }

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
        confidenceBadgeValue.setText(confidenceBadgePercentage);
    }

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
}
