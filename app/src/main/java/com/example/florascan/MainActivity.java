package com.example.florascan;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.florascan.utils.CameraImageProvider;
import com.google.android.material.card.MaterialCardView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private static final String CAMERA_IMAGE_URI_STATE = "camera_image_uri";

    private MaterialCardView takePhotoButton;
    private MaterialCardView galleryButton;

    private CameraImageProvider cameraImageProvider;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        cameraImageProvider = new CameraImageProvider(this);

        restoreCameraImageUri(savedInstanceState);
        initializeViews();
        initializeListeners();
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
        takePhotoButton = findViewById(R.id.takePhotoButton);
        galleryButton = findViewById(R.id.galleryButton);
    }

    private void initializeListeners()
    {
        takePhotoButton.setOnClickListener(view -> openCamera());
        galleryButton.setOnClickListener(view -> openGallery());
    }

    private void openGallery()
    {
        galleryImagePickerLauncher.launch("image/*");
    }

    private void openCamera()
    {
        try
        {
            cameraImageUri = cameraImageProvider.createImageUri();
            cameraImageCaptureLauncher.launch(cameraImageUri);
        }
        catch (IOException exception)
        {
            showCameraError();
        }
    }

    private void handleGalleryImageResult(Uri selectedImageUri)
    {
        if (selectedImageUri == null)
        {
            return;
        }

        openPreviewScreen(selectedImageUri);
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

        openPreviewScreen(capturedImageUri);
    }

    private void openPreviewScreen(Uri selectedImageUri)
    {
        startActivity(
            PreviewActivity.createIntent(
                this,
                selectedImageUri
            )
        );
    }

    private void restoreCameraImageUri(Bundle savedInstanceState)
    {
        if (savedInstanceState == null)
        {
            return;
        }

        String cameraImageUriValue =
            savedInstanceState.getString(CAMERA_IMAGE_URI_STATE);

        if (cameraImageUriValue == null || cameraImageUriValue.isEmpty())
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
