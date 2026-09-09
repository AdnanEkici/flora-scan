package com.example.florascan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class PreviewActivity extends AppCompatActivity
{
    private static final String EXTRA_IMAGE_URI = "selected_image_uri";

    private ImageView previewImage;

    public static Intent createIntent(Context context, Uri selectedImageUri)
    {
        Intent previewIntent = new Intent(context, PreviewActivity.class);
        previewIntent.putExtra(EXTRA_IMAGE_URI, selectedImageUri.toString());

        return previewIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview);

        initializeViews();
        displaySelectedImage();
    }

    private void initializeViews()
    {
        previewImage = findViewById(R.id.previewImage);
    }

    private void displaySelectedImage()
    {
        Uri selectedImageUri = getSelectedImageUri();

        if (selectedImageUri == null)
        {
            finish();
            return;
        }

        Glide.with(this)
             .load(selectedImageUri)
             .fitCenter()
             .into(previewImage);
    }

    private Uri getSelectedImageUri()
    {
        String selectedImageUriValue = getIntent().getStringExtra(EXTRA_IMAGE_URI);

        if (selectedImageUriValue == null || selectedImageUriValue.isEmpty())
        {
            return null;
        }

        return Uri.parse(selectedImageUriValue);
    }
}
