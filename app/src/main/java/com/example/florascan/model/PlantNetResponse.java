package com.example.florascan.model;

import java.util.List;

/**
 * Represents the top-level response returned by the Pl@ntNet identification
 * API.
 *
 * <p>The response contains the provider's best-match label, the ranked list of
 * identification candidates, and the number of identification requests
 * remaining for the configured API account. Instances are populated from the
 * Pl@ntNet JSON response by Gson during Retrofit deserialization.</p>
 */
public class PlantNetResponse
{
    private String bestMatch;
    private List<PlantNetIdentificationResult> results;
    private int remainingIdentificationRequests;

    /**
     * Returns the best-match label reported by Pl@ntNet.
     *
     * @return the best-match plant name reported by the provider
     */
    public String getBestMatch()
    {
        return bestMatch;
    }

    /**
     * Returns the identification candidates returned by Pl@ntNet.
     *
     * @return the list of plant identification results
     */
    public List<PlantNetIdentificationResult> getResults()
    {
        return results;
    }

    /**
     * Returns the number of identification requests remaining for the current
     * Pl@ntNet API account or quota period.
     *
     * @return the remaining identification request count
     */
    public int getRemainingIdentificationRequests()
    {
        return remainingIdentificationRequests;
    }
}
