package com.example.florascan.utils;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
/**
 * Creates temporary image locations for photos captured by the device camera.
 *
 * <p>The provider stores camera images in the application's cache directory
 * and exposes them as content URIs through Android's {@link FileProvider}.
 * This allows camera applications to write to application-owned files without
 * exposing raw filesystem paths.</p>
 *
 * <p>The generated images are temporary cache files and are intended to be
 * passed between the camera, preview, and identification flows using
 * {@link Uri} references.</p>
 */
public class CameraImageProvider
{
    private static final String CAMERA_DIRECTORY_NAME = "camera";
    private static final String IMAGE_FILE_PREFIX = "florascan_camera_";
    private static final String IMAGE_FILE_SUFFIX = ".jpg";
    private static final String FILE_PROVIDER_SUFFIX = ".fileprovider";

    private final Context applicationContext;
    /**
     * Creates a camera image provider using the application context derived from
     * the supplied context.
     *
     * @param context the context used to access application storage and
     *                FileProvider configuration
     */
    public CameraImageProvider(Context context)
    {
        applicationContext = context.getApplicationContext();
    }
    /**
     * Creates a temporary JPEG file for a camera capture and returns its
     * FileProvider content URI.
     *
     * @return a content URI referencing the newly created temporary image file
     * @throws IOException if the camera directory or temporary image file cannot
     *                     be created
     */
    public Uri createImageUri() throws IOException
    {
        File cameraDirectory = createCameraDirectory();

        File imageFile = File.createTempFile(
                                 IMAGE_FILE_PREFIX,
                                 IMAGE_FILE_SUFFIX,
                                 cameraDirectory
                             );

        String providerAuthority =
        applicationContext.getPackageName() + FILE_PROVIDER_SUFFIX;

        return FileProvider.getUriForFile(
                               applicationContext,
                               providerAuthority,
                               imageFile
                           );
    }
    /**
     * Retrieves the camera cache directory, creating it when necessary.
     *
     * @return the directory used to store temporary camera images
     * @throws IOException if the directory does not exist and cannot be created
     */
    private File createCameraDirectory() throws IOException
    {
        File cameraDirectory =
        new File(applicationContext.getCacheDir(), CAMERA_DIRECTORY_NAME);

        if (!cameraDirectory.exists() && !cameraDirectory.mkdirs())
        {
            throw new IOException("Unable to create camera directory.");
        }

        return cameraDirectory;
    }
}
