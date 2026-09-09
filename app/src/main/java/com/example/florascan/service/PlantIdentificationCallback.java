package com.example.florascan.service;

import com.example.florascan.model.PlantResult;
/**
 * Defines callbacks for receiving the outcome of an asynchronous plant
 * identification request.
 *
 * <p>Implementations receive either a successfully mapped
 * {@link PlantResult} or an error message describing why identification could
 * not be completed.</p>
 *
 * <p>The callback does not currently guarantee which thread invokes its
 * methods. Callers that update Android UI components should ensure that UI
 * work is executed on the main thread.</p>
 */
public interface PlantIdentificationCallback
{
    /**
     * Called when plant identification completes successfully.
     *
     * @param plantResult the identified plant result
     */
    void onSuccess(PlantResult plantResult);

    /**
     * Called when plant identification cannot be completed successfully.
     *
     * @param errorMessage a user-readable description of the failure
     */
    void onFailure(String errorMessage);
}
