package com.example.florascan.utils;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class CameraImageProvider
{
    private static final String CAMERA_DIRECTORY_NAME = "camera";
    private static final String IMAGE_FILE_PREFIX = "florascan_camera_";
    private static final String IMAGE_FILE_SUFFIX = ".jpg";
    private static final String FILE_PROVIDER_SUFFIX = ".fileprovider";

    private final Context applicationContext;

    public CameraImageProvider(Context context)
    {
        applicationContext = context.getApplicationContext();
    }

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
