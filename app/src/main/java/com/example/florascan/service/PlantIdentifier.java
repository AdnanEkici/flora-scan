package com.example.florascan.service;

import android.net.Uri;

/**
 * Defines the provider-independent contract for plant identification.
 *
 * <p>Implementations accept an image through a {@link Uri} and report the
 * outcome through a {@link PlantIdentificationCallback}. This abstraction
 * keeps the application independent from a specific plant-identification
 * provider, allowing the underlying implementation to be replaced without
 * changing callers such as repositories or UI flows.</p>
 *
 * <p>Implementations may perform image preparation, network communication,
 * local inference, or other provider-specific work before returning a
 * result.</p>
 */
public interface PlantIdentifier
{
    /**
     * Identifies the plant represented by the supplied image.
     *
     * <p>The result is delivered through the supplied callback rather than being
     * returned directly, allowing implementations to perform identification
     * asynchronously.</p>
     *
     * @param imageUri the URI of the image containing the plant to identify
     * @param identificationCallback the callback that receives either a
     *                               successful identification result or a failure
     */
    void identifyPlant(
        Uri imageUri,
        PlantIdentificationCallback identificationCallback
    );
}
