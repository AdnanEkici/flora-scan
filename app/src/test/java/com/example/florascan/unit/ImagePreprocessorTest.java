package com.example.florascan.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.example.florascan.utils.ImagePreprocessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(RobolectricTestRunner.class)
public class ImagePreprocessorTest
{
    private Context applicationContext;
    private ImagePreprocessor imagePreprocessor;
    private File sourceImageFile;
    private File processedImageFile;

    @Before
    public void setUp()
    {
        applicationContext = ApplicationProvider.getApplicationContext();
        imagePreprocessor = new ImagePreprocessor(applicationContext);
    }

    @After
    public void tearDown()
    {
        deleteFile(sourceImageFile);
        deleteFile(processedImageFile);
    }

    /* Verifies that image preprocessing creates a valid JPEG upload file. */
    @Test
    public void prepareForUploadCreatesJpegFile() throws IOException
    {
        sourceImageFile = createTestImage(800, 600);

        Uri sourceImageUri = Uri.fromFile(sourceImageFile);

        processedImageFile = imagePreprocessor.prepareForUpload(sourceImageUri);

        assertNotNull(processedImageFile);
        assertTrue(processedImageFile.exists());
        assertTrue(processedImageFile.length() > 0);
        assertTrue(processedImageFile.getName().endsWith(".jpg"));
    }

    /* Verifies that images already within the size limit keep their original dimensions. */
    @Test
    public void prepareForUploadKeepsSmallImageDimensions() throws IOException
    {
        int expectedWidth = 800;
        int expectedHeight = 600;

        sourceImageFile = createTestImage(
            expectedWidth,
            expectedHeight
        );

        Uri sourceImageUri = Uri.fromFile(sourceImageFile);

        processedImageFile = imagePreprocessor.prepareForUpload(sourceImageUri);

        Bitmap processedBitmap = BitmapFactory.decodeFile(
                processedImageFile.getAbsolutePath()
                                              );

        assertNotNull(processedBitmap);
        assertEquals(expectedWidth, processedBitmap.getWidth());
        assertEquals(expectedHeight, processedBitmap.getHeight());

        processedBitmap.recycle();
    }

    /* Verifies that oversized images are resized to the configured maximum dimension. */
    @Test
    public void prepareForUploadResizesLargeImage() throws IOException
    {
        sourceImageFile = createTestImage(3200, 2400);

        Uri sourceImageUri = Uri.fromFile(sourceImageFile);

        processedImageFile = imagePreprocessor.prepareForUpload(sourceImageUri);

        Bitmap processedBitmap = BitmapFactory.decodeFile(
                processedImageFile.getAbsolutePath()
                                              );

        assertNotNull(processedBitmap);

        int largestDimension = Math.max(
                                       processedBitmap.getWidth(),
                                       processedBitmap.getHeight()
                                   );

        assertEquals(1600, largestDimension);

        processedBitmap.recycle();
    }

    /* Verifies that resizing preserves the original image aspect ratio. */
    @Test
    public void prepareForUploadPreservesAspectRatio() throws IOException
    {
        sourceImageFile = createTestImage(3200, 1600);

        Uri sourceImageUri = Uri.fromFile(sourceImageFile);

        processedImageFile = imagePreprocessor.prepareForUpload(sourceImageUri);

        Bitmap processedBitmap = BitmapFactory.decodeFile(
                processedImageFile.getAbsolutePath()
                                              );

        assertNotNull(processedBitmap);
        assertEquals(1600, processedBitmap.getWidth());
        assertEquals(800, processedBitmap.getHeight());

        processedBitmap.recycle();
    }

    private File createTestImage(int imageWidth, int imageHeight) throws IOException
    {
        Bitmap testBitmap = Bitmap.createBitmap(
                                      imageWidth,
                                      imageHeight,
                                      Bitmap.Config.ARGB_8888
                                  );

        File testImageFile = File.createTempFile(
                                     "florascan_test_",
                                     ".jpg",
                                     applicationContext.getCacheDir()
                                 );

        try (FileOutputStream outputStream = new FileOutputStream(testImageFile))
        {
            testBitmap.compress(
                          Bitmap.CompressFormat.JPEG,
                          100,
                          outputStream
                      );
        }

        testBitmap.recycle();

        return testImageFile;
    }

    private void deleteFile(File file)
    {
        if (file == null || !file.exists())
        {
            return;
        }

        file.delete();
    }
}
