package com.example.florascan.model;

/**
 * Represents a single plant identification candidate returned by the Pl@ntNet
 * API.
 *
 * <p>Each result contains the provider-assigned confidence score and the
 * corresponding {@link PlantNetSpecies} information. Instances are populated
 * from the Pl@ntNet response by Gson during Retrofit response deserialization.</p>
 */
public class PlantNetIdentificationResult
{
    private double score;
    private PlantNetSpecies species;

    /**
     * Returns the confidence score assigned to this identification result.
     *
     * @return the identification confidence score
     */
    public double getScore()
    {
        return score;
    }

    /**
     * Returns the species information associated with this identification result.
     *
     * @return the identified species information
     */
    public PlantNetSpecies getSpecies()
    {
        return species;
    }
}
