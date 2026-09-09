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
/**
 * Provides the main entry screen for selecting an image to identify.
 *
 * <p>The activity allows the user to either capture a new plant image using
 * the device camera or select an existing image from the system image picker.
 * Successfully selected images are passed to {@link PreviewActivity} using a
 * {@link Uri} rather than transferring bitmap data directly between
 * activities.</p>
 *
 * <p>The activity also preserves the URI associated with an in-progress
 * camera capture across activity recreation so that the capture result can be
 * handled correctly after configuration changes.</p>
 */
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
    /**
     * Initializes the main screen, configures camera image creation, restores any
     * pending camera capture state, and connects the image-selection controls.
     *
     * @param savedInstanceState the previously saved activity state, or
     *                           {@code null} when the activity is created
     *                           for the first time
     */
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
    /**
     * Saves the URI associated with an in-progress camera capture so that it can
     * be restored if the activity is recreated before the capture result returns.
     *
     * @param outputState the bundle in which the activity state is stored
     */
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
    /**
     * Resolves and stores references to the image-selection controls.
     */
    private void initializeViews()
    {
        takePhotoButton = findViewById(R.id.takePhotoButton);
        galleryButton = findViewById(R.id.galleryButton);
    }
    /**
     * Configures the camera and gallery selection interactions.
     */
    private void initializeListeners()
    {
        takePhotoButton.setOnClickListener(view -> openCamera());
        galleryButton.setOnClickListener(view -> openGallery());
    }
    /**
     * Opens the system image picker and restricts selection to image content.
     */
    private void openGallery()
    {
        galleryImagePickerLauncher.launch("image/*");
    }
    /**
     * Creates a temporary image URI and launches the device camera to capture a
     * plant image.
     *
     * <p>If the temporary image URI cannot be created, the camera flow is
     * cancelled and an error message is shown to the user.</p>
     */
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
    /**
     * Handles the result returned by the gallery image picker.
     *
     * <p>If an image was selected, the preview screen is opened with its URI.
     * Cancelled selections are ignored.</p>
     *
     * @param selectedImageUri the URI of the selected image, or {@code null} when
     *                         the selection was cancelled
     */
    private void handleGalleryImageResult(Uri selectedImageUri)
    {
        if (selectedImageUri == null)
        {
            return;
        }

        openPreviewScreen(selectedImageUri);
    }
    /**
     * Handles the result of a camera capture operation.
     *
     * <p>Successful captures are forwarded to the preview screen using the
     * previously created camera image URI. Cancelled captures clear the pending
     * URI and do not trigger navigation.</p>
     *
     * @param imageCaptured {@code true} when the camera successfully stored the
     *                      captured image; otherwise {@code false}
     */
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
    /**
     * Opens {@link PreviewActivity} with the selected image.
     *
     * @param selectedImageUri the URI of the image to preview
     */
    private void openPreviewScreen(Uri selectedImageUri)
    {
        startActivity(
            PreviewActivity.createIntent(
                this,
                selectedImageUri
            )
        );
    }
    /**
     * Restores the URI associated with an in-progress camera capture from saved
     * activity state.
     *
     * @param savedInstanceState the previously saved activity state, or
     *                           {@code null} when no state is available
     */
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
    /**
     * Displays a short message informing the user that the camera could not be
     * prepared or opened.
     */
    private void showCameraError()
    {
        Toast.makeText(
                 this,
                 "Unable to open the camera. Please try again.",
                 Toast.LENGTH_SHORT
             ).show();
    }
}
