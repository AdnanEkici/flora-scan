package com.example.florascan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.florascan.utils.CameraImageProvider;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;

public class PreviewActivity extends AppCompatActivity
{
    private static final String EXTRA_IMAGE_URI = "selected_image_uri";
    private static final String CAMERA_IMAGE_URI_STATE = "camera_image_uri";

    private ImageView previewImage;
    private ImageView backButton;

    private MaterialCardView retakeButton;
    private MaterialCardView changeButton;
    private MaterialCardView identifyButton;

    private CameraImageProvider cameraImageProvider;

    private Uri selectedImageUri;
    private Uri cameraImageUri;

    private final ActivityResultLauncher<String> galleryImagePickerLauncher =
        registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleGalleryImageResult
        );

    private final ActivityResultLauncher<Uri> cameraImageCaptureLauncher =
        registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            this::handleCameraImageResult
        );

    public static Intent createIntent(Context context, Uri selectedImageUri)
    {
        Intent previewIntent = new Intent(context, PreviewActivity.class);
        previewIntent.putExtra(
                         EXTRA_IMAGE_URI,
                         selectedImageUri.toString()
                     );

        return previewIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview);

        cameraImageProvider = new CameraImageProvider(this);

        restoreCameraImageUri(savedInstanceState);

        selectedImageUri = getSelectedImageUri();

        if (selectedImageUri == null)
        {
            finish();
            return;
        }

        initializeViews();
        initializeListeners();
        displaySelectedImage();
    }

    @Override
    protected void onSaveInstanceState(Bundle outputState)
    {
        super.onSaveInstanceState(outputState);

        if (cameraImageUri != null)
        {
            outputState.putString(
                           CAMERA_IMAGE_URI_STATE,
                           cameraImageUri.toString()
                       );
        }
    }

    private void initializeViews()
    {
        previewImage = findViewById(R.id.previewImage);
        backButton = findViewById(R.id.backButton);
        retakeButton = findViewById(R.id.retakeButton);
        changeButton = findViewById(R.id.changeButton);
        identifyButton = findViewById(R.id.identifyButton);
    }

    private void initializeListeners()
    {
        backButton.setOnClickListener(
                      view -> finish()
                  );

        retakeButton.setOnClickListener(
                        view -> openCamera()
                    );

        changeButton.setOnClickListener(
                        view -> openGallery()
                    );

        identifyButton.setOnClickListener(
                          view -> openLoadingScreen()
                      );
    }

    private void openCamera()
    {
        try
        {
            cameraImageUri = cameraImageProvider.createImageUri();

            cameraImageCaptureLauncher.launch(
                                          cameraImageUri
                                      );
        }
        catch (IOException exception)
        {
            showCameraError();
        }
    }

    private void openGallery()
    {
        galleryImagePickerLauncher.launch("image/*");
    }

    private void handleCameraImageResult(Boolean imageCaptured)
    {
        if (!Boolean.TRUE.equals(imageCaptured))
        {
            cameraImageUri = null;
            return;
        }

        if (cameraImageUri == null)
        {
            showCameraError();
            return;
        }

        Uri capturedImageUri = cameraImageUri;

        cameraImageUri = null;

        updateSelectedImage(capturedImageUri);
    }

    private void handleGalleryImageResult(Uri selectedGalleryImageUri)
    {
        if (selectedGalleryImageUri == null)
        {
            return;
        }

        updateSelectedImage(selectedGalleryImageUri);
    }

    private void updateSelectedImage(Uri newSelectedImageUri)
    {
        selectedImageUri = newSelectedImageUri;

        getIntent().putExtra(
            EXTRA_IMAGE_URI,
            selectedImageUri.toString()
        );

        displaySelectedImage();
    }

    private void displaySelectedImage()
    {
        Glide.with(this)
             .load(selectedImageUri)
             .fitCenter()
             .into(previewImage);
    }

    private void openLoadingScreen()
    {
        Intent loadingIntent =
            LoadingActivity.createIntent(
                this,
                selectedImageUri
            );

        startActivity(loadingIntent);
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

    private void restoreCameraImageUri(Bundle savedInstanceState)
    {
        if (savedInstanceState == null)
        {
            return;
        }

        String cameraImageUriValue =
            savedInstanceState.getString(CAMERA_IMAGE_URI_STATE);

        if (cameraImageUriValue == null
                || cameraImageUriValue.isEmpty())
        {
            return;
        }

        cameraImageUri = Uri.parse(cameraImageUriValue);
    }

    private void showCameraError()
    {
        Toast.makeText(
                 this,
                 "Unable to open the camera. Please try again.",
                 Toast.LENGTH_SHORT
             ).show();
    }
}
