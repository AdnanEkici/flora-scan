package com.example.florascan.model;

public class PlantResult
{
    private final String commonName;
    private final String scientificName;
    private final String family;
    private final String genus;
    private final double confidence;

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

    public String getCommonName()
    {
        return commonName;
    }

    public String getScientificName()
    {
        return scientificName;
    }

    public String getFamily()
    {
        return family;
    }

    public String getGenus()
    {
        return genus;
    }

    public double getConfidence()
    {
        return confidence;
    }
}
