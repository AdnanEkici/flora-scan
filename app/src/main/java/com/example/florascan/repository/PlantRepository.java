package com.example.florascan.repository;

import android.net.Uri;

import com.example.florascan.service.PlantIdentificationCallback;
import com.example.florascan.service.PlantIdentifier;

/**
 * Provides the application's repository boundary for plant identification.
 *
 * <p>The repository delegates plant identification requests to a
 * provider-independent {@link PlantIdentifier} implementation. This keeps
 * calling layers, such as activities and other presentation components,
 * decoupled from provider-specific identification logic.</p>
 *
 * <p>The repository is intentionally lightweight in the current prototype,
 * but it provides an extension point for future concerns such as caching,
 * persistence, request coordination, provider fallback, or communication
 * with an application backend.</p>
 */
public class PlantRepository
{
    private final PlantIdentifier plantIdentifier;

    /**
     * Creates a plant repository backed by the supplied plant identifier.
     *
     * @param plantIdentifier the plant identification implementation used to
     *                        process identification requests
     */
    public PlantRepository(PlantIdentifier plantIdentifier)
    {
        this.plantIdentifier = plantIdentifier;
    }

    /**
     * Delegates identification of the supplied image to the configured plant
     * identifier.
     *
     * @param imageUri the URI of the image containing the plant to identify
     * @param identificationCallback the callback that receives either the
     *                               identification result or a failure
     */
    public void identifyPlant(
        Uri imageUri,
        PlantIdentificationCallback identificationCallback)
    {
        plantIdentifier.identifyPlant(
                           imageUri,
                           identificationCallback
                       );
    }
}
