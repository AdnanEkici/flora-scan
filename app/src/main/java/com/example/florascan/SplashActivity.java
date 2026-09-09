package com.example.florascan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays the FloraScan splash screen before navigating to the main screen.
 */
public class SplashActivity extends AppCompatActivity
{
    private static final long SPLASH_DELAY_MILLISECONDS = 1800;

    /**
     * Initializes the splash screen and schedules navigation to
     * {@link MainActivity} after the configured splash delay.
     *
     * @param savedInstanceState the previously saved activity state, or
     *                           {@code null} when the activity is created
     *                           for the first time
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
        {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MILLISECONDS);
    }
}
