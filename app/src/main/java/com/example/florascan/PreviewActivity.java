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

/**
 * Displays a preview of the image selected for plant identification.
 *
 * <p>The activity allows the user to review the currently selected image
 * before submitting it for identification. From this screen, the user can
 * capture a replacement image with the camera, select another image from the
 * gallery, return to the previous screen, or continue to the plant
 * identification process.</p>
 *
 * <p>The selected image is represented by a {@link Uri} rather than a
 * {@code Bitmap}, avoiding the transfer of large image data between
 * activities. Instances of this activity should be created through
 * {@link #createIntent(Context, Uri)}.</p>
 */
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

    /**
     * Creates an intent configured to display the supplied image in the preview
     * screen.
     *
     * @param context the context used to create the intent
     * @param selectedImageUri the URI of the image to preview
     * @return an intent configured to launch {@link PreviewActivity}
     */
    public static Intent createIntent(Context context, Uri selectedImageUri)
    {
        Intent previewIntent = new Intent(context, PreviewActivity.class);
        previewIntent.putExtra(
                         EXTRA_IMAGE_URI,
                         selectedImageUri.toString()
                     );

        return previewIntent;
    }

    /**
     * Initializes the preview screen, restores any pending camera state, loads the
     * selected image URI, and configures the available user interactions.
     *
     * <p>If no valid selected image URI is available, the activity is closed
     * because there is no image to preview.</p>
     *
     * @param savedInstanceState the previously saved activity state, or
     *                           {@code null} when the activity is created
     *                           for the first time
     */
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

    /**
     * Saves the URI of a pending camera image so that an in-progress camera
     * capture can survive activity recreation.
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
     * Resolves and stores references to the views used by the preview screen.
     */
    private void initializeViews()
    {
        previewImage = findViewById(R.id.previewImage);
        backButton = findViewById(R.id.backButton);
        retakeButton = findViewById(R.id.retakeButton);
        changeButton = findViewById(R.id.changeButton);
        identifyButton = findViewById(R.id.identifyButton);
    }

    /**
     * Configures navigation, camera, gallery, and identification interactions.
     */
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

    /**
     * Creates a temporary image URI and launches the device camera to capture a
     * replacement image.
     *
     * <p>If the temporary image URI cannot be created, an error message is shown
     * and the existing selected image is preserved.</p>
     */
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

    /**
     * Opens the system image picker so the user can select a replacement image
     * from available image sources.
     */
    private void openGallery()
    {
        galleryImagePickerLauncher.launch("image/*");
    }

    /**
     * Handles the result of a camera capture operation.
     *
     * <p>When capture succeeds, the pending camera URI becomes the newly selected
     * image. Cancelled captures are ignored and their pending URI is cleared.</p>
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

        updateSelectedImage(capturedImageUri);
    }

    /**
     * Handles an image returned by the gallery picker and updates the current
     * preview when a selection was made.
     *
     * @param selectedGalleryImageUri the URI of the selected gallery image, or
     *                                {@code null} when selection was cancelled
     */
    private void handleGalleryImageResult(Uri selectedGalleryImageUri)
    {
        if (selectedGalleryImageUri == null)
        {
            return;
        }

        updateSelectedImage(selectedGalleryImageUri);
    }

    /**
     * Replaces the currently selected image, updates the activity intent with the
     * new URI, and refreshes the displayed preview.
     *
     * @param newSelectedImageUri the URI of the replacement image
     */
    private void updateSelectedImage(Uri newSelectedImageUri)
    {
        selectedImageUri = newSelectedImageUri;

        getIntent().putExtra(
            EXTRA_IMAGE_URI,
            selectedImageUri.toString()
        );

        displaySelectedImage();
    }

    /**
     * Loads the currently selected image into the preview view using Glide.
     */
    private void displaySelectedImage()
    {
        Glide.with(this)
             .load(selectedImageUri)
             .fitCenter()
             .into(previewImage);
    }

    /**
     * Opens {@link LoadingActivity} with the selected image to begin the plant
     * identification process.
     */
    private void openLoadingScreen()
    {
        Intent loadingIntent =
            LoadingActivity.createIntent(
                this,
                selectedImageUri
            );

        startActivity(loadingIntent);
    }

    /**
     * Reads and parses the selected image URI stored in the launching intent.
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

        if (cameraImageUriValue == null
                || cameraImageUriValue.isEmpty())
        {
            return;
        }

        cameraImageUri = Uri.parse(cameraImageUriValue);
    }

    /**
     * Displays a short message informing the user that the camera could not be
     * opened or prepared.
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
