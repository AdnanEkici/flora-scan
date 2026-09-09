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
/**
 * Prepares user-selected images for plant identification uploads.
 *
 * <p>The preprocessor decodes an image from its {@link Uri}, applies EXIF
 * orientation correction, reduces oversized images while preserving their
 * aspect ratio, and writes the processed image as a compressed JPEG file in
 * the application's cache directory.</p>
 *
 * <p>Image bounds are inspected before full decoding so that very large
 * images can be downsampled during decoding. The final image is constrained
 * to a maximum dimension of 1600 pixels and compressed at a JPEG quality of
 * 85, reducing memory usage, upload size, bandwidth consumption, and API
 * request latency.</p>
 *
 * <p>Intermediate bitmap instances are recycled when they are no longer
 * required. The returned file is temporary and is expected to be deleted
 * after the identification request has completed.</p>
 */
public class ImagePreprocessor
{
    private static final int MAXIMUM_IMAGE_DIMENSION = 1600;
    private static final int JPEG_QUALITY = 85;

    private final Context applicationContext;
    /**
     * Creates an image preprocessor using the application context derived from
     * the supplied context.
     *
     * @param context the context used to access image content and application
     *                cache storage
     */
    public ImagePreprocessor(Context context)
    {
        applicationContext = context.getApplicationContext();
    }
    /**
     * Prepares the supplied image for upload by decoding, correcting its
     * orientation, resizing it when necessary, and compressing it as JPEG.
     *
     * @param imageUri the URI of the source image to preprocess
     * @return a temporary JPEG file containing the processed image
     * @throws IOException if the source image cannot be opened, decoded,
     *                     processed, or written to temporary storage
     */
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
    /**
     * Decodes an image from the supplied URI using an appropriate sampling factor
     * to reduce memory usage for large source images.
     *
     * <p>The image bounds are read first without allocating the full bitmap. A
     * sampling factor is then calculated before the actual image is decoded.</p>
     *
     * @param imageUri the URI of the image to decode
     * @return the decoded bitmap
     * @throws IOException if the image cannot be opened, its dimensions cannot
     *                     be determined, or decoding fails
     */
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
    /**
     * Calculates a power-of-two sampling factor for decoding a large image while
     * avoiding unnecessarily large bitmap allocations.
     *
     * @param imageWidth the original image width in pixels
     * @param imageHeight the original image height in pixels
     * @return the sampling factor to use during bitmap decoding
     */
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
    /**
     * Corrects the bitmap orientation using EXIF metadata from the source image.
     *
     * <p>Images marked as rotated by 90, 180, or 270 degrees are transformed to
     * their expected display orientation. Images without a supported rotation
     * value are returned unchanged.</p>
     *
     * @param bitmap the decoded bitmap to orient
     * @param imageUri the URI used to read the source image EXIF metadata
     * @return the correctly oriented bitmap, or the original bitmap when no
     *         rotation is required
     * @throws IOException if the image metadata cannot be read
     */
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
    /**
     * Resizes the supplied bitmap when its largest dimension exceeds the
     * configured maximum image dimension.
     *
     * <p>The original aspect ratio is preserved during scaling. Images already
     * within the allowed dimensions are returned unchanged.</p>
     *
     * @param bitmap the bitmap to resize
     * @return the resized bitmap, or the original bitmap when resizing is not
     *         required
     */
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
    /**
     * Creates a temporary JPEG file in the application's cache directory for the
     * processed upload image.
     *
     * @return the temporary file used to store the processed image
     * @throws IOException if the temporary file cannot be created
     */
    private File createOutputFile() throws IOException
    {
        File outputFile = File.createTempFile(
                                  "florascan_upload_",
                                  ".jpg",
                                  applicationContext.getCacheDir()
                              );

        return outputFile;
    }
    /**
     * Compresses the supplied bitmap as JPEG and writes it to the specified
     * output file.
     *
     * @param bitmap the processed bitmap to write
     * @param outputFile the destination file
     * @throws IOException if the output file cannot be written or JPEG
     *                     compression fails
     */
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
