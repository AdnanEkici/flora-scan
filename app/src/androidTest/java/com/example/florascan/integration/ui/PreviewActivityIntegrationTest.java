package com.example.florascan.integration.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.florascan.LoadingActivity;
import com.example.florascan.PreviewActivity;
import com.example.florascan.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class PreviewActivityIntegrationTest
{
    private static final String CAMERA_DIRECTORY_NAME = "camera";
    private static final String TEST_IMAGE_PREFIX = "preview_test_";
    private static final String TEST_IMAGE_SUFFIX = ".jpg";
    private static final String FILE_PROVIDER_SUFFIX = ".fileprovider";
    private static final int TEST_IMAGE_WIDTH = 100;
    private static final int TEST_IMAGE_HEIGHT = 100;
    private static final int JPEG_QUALITY = 100;

    private Context applicationContext;
    private File testImageFile;
    private Uri testImageUri;

    @Before
    public void setUp() throws IOException
    {
        applicationContext = ApplicationProvider.getApplicationContext();

        testImageFile = createTestImage();
        testImageUri = createTestImageUri(testImageFile);

        Intents.init();
    }

    @After
    public void tearDown()
    {
        Intents.release();

        deleteTestImage();
    }

    /* Verifies that PreviewActivity displays the selected plant image view. */
    @Test
    public void selectedImageIsDisplayed()
    {
        ActivityScenario<PreviewActivity> activityScenario =
            ActivityScenario.launch(
                PreviewActivity.createIntent(
                    applicationContext,
                    testImageUri
                )
            );

        try
        {
            onView(withId(R.id.previewImage))
            .check(matches(isDisplayed()));
        }
        finally
        {
            activityScenario.close();
        }
    }

    /* Verifies that pressing Identify Plant navigates from PreviewActivity to LoadingActivity. */
    @Test
    public void identifyButtonOpensLoadingActivity()
    {
        Instrumentation.ActivityResult activityResult =
            new Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            null
        );

        intending(
            hasComponent(LoadingActivity.class.getName())
        ).respondWith(activityResult);

        ActivityScenario<PreviewActivity> activityScenario =
            ActivityScenario.launch(
                PreviewActivity.createIntent(
                    applicationContext,
                    testImageUri
                )
            );

        try
        {
            onView(withId(R.id.identifyButton))
            .perform(click());

            intended(
                hasComponent(LoadingActivity.class.getName())
            );
        }
        finally
        {
            activityScenario.close();
        }
    }

    /* Verifies that pressing the back control closes PreviewActivity. */
    @Test
    public void backButtonFinishesPreviewActivity()
    {
        ActivityScenario<PreviewActivity> activityScenario =
            ActivityScenario.launch(
                PreviewActivity.createIntent(
                    applicationContext,
                    testImageUri
                )
            );

        try
        {
            onView(withId(R.id.backButton))
            .perform(click());

            Lifecycle.State activityState =
                activityScenario.getState();

            assertEquals(
                Lifecycle.State.DESTROYED,
                activityState
            );
        }
        finally
        {
            activityScenario.close();
        }
    }

    private File createTestImage() throws IOException
    {
        File cameraDirectory =
        new File(
            applicationContext.getCacheDir(),
            CAMERA_DIRECTORY_NAME
        );

        ensureCameraDirectoryExists(cameraDirectory);

        File imageFile = File.createTempFile(
                                 TEST_IMAGE_PREFIX,
                                 TEST_IMAGE_SUFFIX,
                                 cameraDirectory
                             );

        Bitmap testBitmap = Bitmap.createBitmap(
                                      TEST_IMAGE_WIDTH,
                                      TEST_IMAGE_HEIGHT,
                                      Bitmap.Config.ARGB_8888
                                  );

        try
        {
            writeTestImage(
                testBitmap,
                imageFile
            );
        }
        finally
        {
            testBitmap.recycle();
        }

        return imageFile;
    }

    private void ensureCameraDirectoryExists(
        File cameraDirectory) throws IOException
    {
        if (cameraDirectory.exists())
        {
            return;
        }

        boolean directoryCreated =
        cameraDirectory.mkdirs();

        if (!directoryCreated)
        {
            throw new IOException(
                "Unable to create camera test directory."
            );
        }
    }

    private void writeTestImage(
        Bitmap testBitmap,
        File imageFile) throws IOException
    {
        try (FileOutputStream outputStream =
                    new FileOutputStream(imageFile))
        {
            boolean compressionSuccessful =
            testBitmap.compress(
                          Bitmap.CompressFormat.JPEG,
                          JPEG_QUALITY,
                          outputStream
                      );

            if (!compressionSuccessful)
            {
                throw new IOException(
                    "Unable to create test image."
                );
            }
        }
    }

    private Uri createTestImageUri(File imageFile)
    {
        String providerAuthority =
            applicationContext.getPackageName()
            + FILE_PROVIDER_SUFFIX;

        Uri imageUri = FileProvider.getUriForFile(
                           applicationContext,
                           providerAuthority,
                           imageFile
                       );

        return imageUri;
    }

    private void deleteTestImage()
    {
        if (testImageFile == null || !testImageFile.exists())
        {
            return;
        }

        testImageFile.delete();
    }
}
