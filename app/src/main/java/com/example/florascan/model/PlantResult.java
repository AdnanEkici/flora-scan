package com.example.florascan.model;

/**
 * Represents the provider-independent plant identification result used by the
 * application.
 *
 * <p>The model contains the plant's common name, scientific name, family,
 * genus, and identification confidence. It is intentionally independent from
 * the Pl@ntNet response structure so that UI and repository layers do not
 * depend directly on provider-specific data models.</p>
 *
 * <p>Instances are immutable after construction and are typically created by
 * mapping a provider response through the service layer.</p>
 */
public class PlantResult
{
    private final String commonName;
    private final String scientificName;
    private final String family;
    private final String genus;
    private final double confidence;

    /**
     * Creates a plant identification result.
     *
     * @param commonName the plant's common name
     * @param scientificName the plant's scientific name
     * @param family the plant family
     * @param genus the plant genus
     * @param confidence the normalized identification confidence score, where
     *                   {@code 1.0} represents 100 percent confidence
     */
    public PlantResult(
        String commonName,
        String scientificName,
        String family,
        String genus,
        double confidence)
    {
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.family = family;
        this.genus = genus;
        this.confidence = confidence;
    }
    /**
     * Returns the plant's common name.
     *
     * @return the common name
     */
    public String getCommonName()
    {
        return commonName;
    }
    /**
     * Returns the plant's scientific name.
     *
     * @return the scientific name
     */
    public String getScientificName()
    {
        return scientificName;
    }
    /**
     * Returns the plant family.
     *
     * @return the family name
     */
    public String getFamily()
    {
        return family;
    }
    /**
     * Returns the plant genus.
     *
     * @return the genus name
     */
    public String getGenus()
    {
        return genus;
    }
    /**
     * Returns the normalized confidence score for the identification.
     *
     * @return the confidence score, where {@code 1.0} represents 100 percent
     *         confidence
     */
    public double getConfidence()
    {
        return confidence;
    }
}
