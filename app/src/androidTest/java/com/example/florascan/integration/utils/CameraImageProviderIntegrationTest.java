package com.example.florascan.integration.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.florascan.utils.CameraImageProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class CameraImageProviderIntegrationTest
{
    private Context applicationContext;
    private CameraImageProvider cameraImageProvider;

    @Before
    public void setUp()
    {
        applicationContext = ApplicationProvider.getApplicationContext();
        cameraImageProvider = new CameraImageProvider(applicationContext);
    }

    @After
    public void tearDown()
    {
        File cameraDirectory =
            new File(applicationContext.getCacheDir(), "camera");

        File[] cameraFiles = cameraDirectory.listFiles();

        if (cameraFiles == null)
        {
            return;
        }

        for (File cameraFile : cameraFiles)
        {
            cameraFile.delete();
        }
    }

    /* Verifies that FileProvider creates a content URI for a camera image. */
    @Test
    public void createImageUriReturnsContentUri() throws IOException
    {
        Uri imageUri = cameraImageProvider.createImageUri();

        assertNotNull(imageUri);
        assertEquals("content", imageUri.getScheme());
    }

    /* Verifies that the generated URI uses the application's FileProvider authority. */
    @Test
    public void createImageUriUsesApplicationFileProviderAuthority() throws IOException
    {
        String expectedAuthority =
        applicationContext.getPackageName() + ".fileprovider";

        Uri imageUri = cameraImageProvider.createImageUri();

        assertEquals(expectedAuthority, imageUri.getAuthority());
    }

    /* Verifies that camera image creation uses the configured camera cache directory. */
    @Test
    public void createImageUriCreatesCameraDirectory() throws IOException
    {
        cameraImageProvider.createImageUri();

        File cameraDirectory =
        new File(applicationContext.getCacheDir(), "camera");

        assertTrue(cameraDirectory.exists());
        assertTrue(cameraDirectory.isDirectory());
    }

    /* Verifies that separate camera image requests produce different content URIs. */
    @Test
    public void createImageUriCreatesUniqueUris() throws IOException
    {
        Uri firstImageUri = cameraImageProvider.createImageUri();
        Uri secondImageUri = cameraImageProvider.createImageUri();

        assertNotNull(firstImageUri);
        assertNotNull(secondImageUri);
        assertFalse(firstImageUri.equals(secondImageUri));
    }
}
