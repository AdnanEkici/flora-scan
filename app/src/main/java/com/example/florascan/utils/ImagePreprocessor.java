package com.example.florascan.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImagePreprocessor
{
    private static final int MAXIMUM_IMAGE_DIMENSION = 1600;
    private static final int JPEG_QUALITY = 85;

    private final Context applicationContext;

    public ImagePreprocessor(Context context)
    {
        applicationContext = context.getApplicationContext();
    }

    public File prepareForUpload(Uri imageUri) throws IOException
    {
        Bitmap decodedBitmap = decodeImage(imageUri);
        Bitmap orientedBitmap = correctOrientation(decodedBitmap, imageUri);
        Bitmap resizedBitmap = resizeImage(orientedBitmap);
        File outputFile = createOutputFile();

        writeImage(resizedBitmap, outputFile);

        if (decodedBitmap != resizedBitmap && !decodedBitmap.isRecycled())
        {
            decodedBitmap.recycle();
        }

        if (orientedBitmap != decodedBitmap
                && orientedBitmap != resizedBitmap
                && !orientedBitmap.isRecycled())
        {
            orientedBitmap.recycle();
        }

        if (!resizedBitmap.isRecycled())
        {
            resizedBitmap.recycle();
        }

        return outputFile;
    }

    private Bitmap decodeImage(Uri imageUri) throws IOException
    {
        ContentResolver contentResolver = applicationContext.getContentResolver();

        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;

        try (InputStream inputStream = contentResolver.openInputStream(imageUri))
        {
            if (inputStream == null)
            {
                throw new IOException("Unable to open selected image.");
            }

            BitmapFactory.decodeStream(inputStream, null, boundsOptions);
        }

        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0)
        {
            throw new IOException("Unable to determine image dimensions.");
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = calculateSampleSize(
                                         boundsOptions.outWidth,
                                         boundsOptions.outHeight
                                     );

        Bitmap decodedBitmap;

        try (InputStream inputStream = contentResolver.openInputStream(imageUri))
        {
            if (inputStream == null)
            {
                throw new IOException("Unable to open selected image.");
            }

            decodedBitmap = BitmapFactory.decodeStream(
                                inputStream,
                                null,
                                decodeOptions
                            );

            if (decodedBitmap == null)
            {
                throw new IOException("Unable to decode selected image.");
            }
        }

        return decodedBitmap;
    }

    private int calculateSampleSize(int imageWidth, int imageHeight)
    {
        int sampleSize = 1;

        while ((imageWidth / sampleSize) > MAXIMUM_IMAGE_DIMENSION * 2
                || (imageHeight / sampleSize) > MAXIMUM_IMAGE_DIMENSION * 2)
        {
            sampleSize *= 2;
        }

        return sampleSize;
    }

    private Bitmap correctOrientation(Bitmap bitmap, Uri imageUri) throws IOException
    {
        ContentResolver contentResolver = applicationContext.getContentResolver();
        Bitmap orientedBitmap = bitmap;

        try (InputStream inputStream = contentResolver.openInputStream(imageUri))
        {
            if (inputStream == null)
            {
                return orientedBitmap;
            }

            ExifInterface exifInterface = new ExifInterface(inputStream);

            int orientation = exifInterface.getAttributeInt(
                                  ExifInterface.TAG_ORIENTATION,
                                  ExifInterface.ORIENTATION_NORMAL
                              );

            Matrix transformationMatrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
            {
                transformationMatrix.postRotate(90);
            }
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
            {
                transformationMatrix.postRotate(180);
            }
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
            {
                transformationMatrix.postRotate(270);
            }
            else
            {
                return orientedBitmap;
            }

            orientedBitmap = Bitmap.createBitmap(
                                 bitmap,
                                 0,
                                 0,
                                 bitmap.getWidth(),
                                 bitmap.getHeight(),
                                 transformationMatrix,
                                 true
                             );
        }

        return orientedBitmap;
    }

    private Bitmap resizeImage(Bitmap bitmap)
    {
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        int largestDimension = Math.max(imageWidth, imageHeight);
        Bitmap resizedBitmap = bitmap;

        if (largestDimension > MAXIMUM_IMAGE_DIMENSION)
        {
            float scale = (float) MAXIMUM_IMAGE_DIMENSION / largestDimension;

            int resizedWidth = Math.round(imageWidth * scale);
            int resizedHeight = Math.round(imageHeight * scale);

            resizedBitmap = Bitmap.createScaledBitmap(
                                bitmap,
                                resizedWidth,
                                resizedHeight,
                                true
                            );
        }

        return resizedBitmap;
    }

    private File createOutputFile() throws IOException
    {
        File outputFile = File.createTempFile(
                                  "florascan_upload_",
                                  ".jpg",
                                  applicationContext.getCacheDir()
                              );

        return outputFile;
    }

    private void writeImage(Bitmap bitmap, File outputFile) throws IOException
    {
        try (FileOutputStream outputStream = new FileOutputStream(outputFile))
        {
            boolean compressionSuccessful = bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    JPEG_QUALITY,
                    outputStream
                                                  );

            if (!compressionSuccessful)
            {
                throw new IOException("Unable to compress selected image.");
            }
        }
    }
}
