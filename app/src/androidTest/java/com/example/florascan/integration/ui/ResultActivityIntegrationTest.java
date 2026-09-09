package com.example.florascan.integration.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.florascan.MainActivity;
import com.example.florascan.R;
import com.example.florascan.ResultActivity;
import com.example.florascan.model.PlantResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ResultActivityIntegrationTest
{
    private static final String CAMERA_DIRECTORY_NAME = "camera";
    private static final String TEST_IMAGE_PREFIX = "result_test_";
    private static final String TEST_IMAGE_SUFFIX = ".jpg";
    private static final String FILE_PROVIDER_SUFFIX = ".fileprovider";

    private static final int TEST_IMAGE_WIDTH = 100;
    private static final int TEST_IMAGE_HEIGHT = 100;
    private static final int JPEG_QUALITY = 100;

    private static final String COMMON_NAME = "Golden Pothos";
    private static final String SCIENTIFIC_NAME = "Epipremnum aureum";
    private static final String FAMILY = "Araceae";
    private static final String GENUS = "Epipremnum";
    private static final double CONFIDENCE = 0.92;

    private Context applicationContext;
    private File testImageFile;
    private Uri testImageUri;
    private PlantResult plantResult;

    @Before
    public void setUp() throws IOException
    {
        applicationContext = ApplicationProvider.getApplicationContext();

        testImageFile = createTestImage();
        testImageUri = createTestImageUri(testImageFile);

        plantResult = new PlantResult(
            COMMON_NAME,
            SCIENTIFIC_NAME,
            FAMILY,
            GENUS,
            CONFIDENCE
        );

        Intents.init();
    }

    @After
    public void tearDown()
    {
        Intents.release();

        deleteTestImage();
    }

    /* Verifies that ResultActivity displays the identified plant information. */
    @Test
    public void resultScreenDisplaysPlantInformation()
    {
        ActivityScenario<ResultActivity> activityScenario =
            ActivityScenario.launch(
                ResultActivity.createIntent(
                    applicationContext,
                    testImageUri,
                    plantResult
                )
            );

        try
        {
            onView(withId(R.id.commonName))
            .check(matches(withText(COMMON_NAME)));

            onView(withId(R.id.scientificName))
            .check(matches(withText(SCIENTIFIC_NAME)));

            onView(withId(R.id.familyValue))
            .check(matches(withText(FAMILY)));

            onView(withId(R.id.genusValue))
            .check(matches(withText(GENUS)));

            onView(withId(R.id.confidenceValue))
            .check(matches(withText("92.0%")));

            onView(withId(R.id.confidenceBadgeValue))
            .check(matches(withText("92%")));
        }
        finally
        {
            activityScenario.close();
        }
    }

    /* Verifies that ResultActivity displays the selected plant image view. */
    @Test
    public void resultScreenDisplaysPlantImage()
    {
        ActivityScenario<ResultActivity> activityScenario =
            ActivityScenario.launch(
                ResultActivity.createIntent(
                    applicationContext,
                    testImageUri,
                    plantResult
                )
            );

        try
        {
            onView(withId(R.id.plantImage))
            .check(matches(isDisplayed()));
        }
        finally
        {
            activityScenario.close();
        }
    }

    /* Verifies that Identify Another navigates from ResultActivity back to MainActivity. */
    @Test
    public void identifyAnotherButtonOpensMainActivity()
    {
        Instrumentation.ActivityResult activityResult =
            new Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            null
        );

        intending(
            hasComponent(MainActivity.class.getName())
        ).respondWith(activityResult);

        ActivityScenario<ResultActivity> activityScenario =
            ActivityScenario.launch(
                ResultActivity.createIntent(
                    applicationContext,
                    testImageUri,
                    plantResult
                )
            );

        try
        {
            onView(withId(R.id.identifyAnotherButton))
            .perform(scrollTo(), click());

            intended(
                hasComponent(MainActivity.class.getName())
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
