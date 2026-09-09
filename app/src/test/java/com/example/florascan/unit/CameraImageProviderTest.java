package com.example.florascan.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.example.florascan.utils.CameraImageProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;

public class CameraImageProviderTest
{
    private static final String PACKAGE_NAME = "com.example.florascan";
    private static final String EXPECTED_AUTHORITY =
        PACKAGE_NAME + ".fileprovider";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Context context;
    private Context applicationContext;
    private CameraImageProvider cameraImageProvider;

    @Before
    public void setUp()
    {
        context = mock(Context.class);
        applicationContext = mock(Context.class);

        when(context.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getCacheDir()).thenReturn(temporaryFolder.getRoot());
        when(applicationContext.getPackageName()).thenReturn(PACKAGE_NAME);

        cameraImageProvider = new CameraImageProvider(context);
    }

    /* Verifies that the URI returned by FileProvider is returned to the caller. */
    @Test
    public void createImageUriReturnsFileProviderUri() throws IOException
    {
        Uri expectedImageUri = mock(Uri.class);

        try (MockedStatic<FileProvider> fileProviderMock = mockStatic(FileProvider.class))
        {
            fileProviderMock.when(
                                () -> FileProvider.getUriForFile(
                                    eq(applicationContext),
                                    eq(EXPECTED_AUTHORITY),
                                    any(File.class)
                                )
                            ).thenReturn(expectedImageUri);

            Uri actualImageUri = cameraImageProvider.createImageUri();

            assertNotNull(actualImageUri);
            assertEquals(expectedImageUri, actualImageUri);
        }
    }

    /* Verifies that the application package name is used to build the FileProvider authority. */
    @Test
    public void createImageUriUsesApplicationFileProviderAuthority() throws IOException
    {
        Uri expectedImageUri = mock(Uri.class);

        try (MockedStatic<FileProvider> fileProviderMock = mockStatic(FileProvider.class))
        {
            fileProviderMock.when(
                                () -> FileProvider.getUriForFile(
                                    eq(applicationContext),
                                    eq(EXPECTED_AUTHORITY),
                                    any(File.class)
                                )
                            ).thenReturn(expectedImageUri);

            cameraImageProvider.createImageUri();

            fileProviderMock.verify(
                                () -> FileProvider.getUriForFile(
                                    eq(applicationContext),
                                    eq(EXPECTED_AUTHORITY),
                                    any(File.class)
                                )
                            );
        }
    }

    /* Verifies that camera images are created inside the camera cache directory. */
    @Test
    public void createImageUriCreatesImageInsideCameraDirectory() throws IOException
    {
        Uri expectedImageUri = mock(Uri.class);
        ArgumentCaptor<File> imageFileCaptor =
        ArgumentCaptor.forClass(File.class);

        try (MockedStatic<FileProvider> fileProviderMock = mockStatic(FileProvider.class))
        {
            fileProviderMock.when(
                                () -> FileProvider.getUriForFile(
                                    eq(applicationContext),
                                    eq(EXPECTED_AUTHORITY),
                                    any(File.class)
                                )
                            ).thenReturn(expectedImageUri);

            cameraImageProvider.createImageUri();

            fileProviderMock.verify(
                                () -> FileProvider.getUriForFile(
                                    eq(applicationContext),
                                    eq(EXPECTED_AUTHORITY),
                                    imageFileCaptor.capture()
                                )
                            );

            File imageFile = imageFileCaptor.getValue();
            File cameraDirectory = imageFile.getParentFile();

            assertNotNull(cameraDirectory);
            assertEquals("camera", cameraDirectory.getName());
            assertTrue(cameraDirectory.exists());
            assertTrue(cameraDirectory.isDirectory());
            assertTrue(imageFile.exists());
        }
    }

    /* Verifies that generated camera image files use the expected JPEG naming convention. */
    @Test
    public void createImageUriCreatesJpegImageFile() throws IOException
    {
        Uri expectedImageUri = mock(Uri.class);
        ArgumentCaptor<File> imageFileCaptor =
        ArgumentCaptor.forClass(File.class);

        try (MockedStatic<FileProvider> fileProviderMock = mockStatic(FileProvider.class))
        {
            fileProviderMock.when(
                                () -> FileProvider.getUriForFile(
                                    eq(applicationContext),
                                    eq(EXPECTED_AUTHORITY),
                                    any(File.class)
                                )
                            ).thenReturn(expectedImageUri);

            cameraImageProvider.createImageUri();

            fileProviderMock.verify(
                                () -> FileProvider.getUriForFile(
                                    eq(applicationContext),
                                    eq(EXPECTED_AUTHORITY),
                                    imageFileCaptor.capture()
                                )
                            );

            File imageFile = imageFileCaptor.getValue();
            String imageFileName = imageFile.getName();

            assertTrue(imageFileName.startsWith("florascan_camera_"));
            assertTrue(imageFileName.endsWith(".jpg"));
        }
    }
}
