package com.example.florascan.model;

import java.util.List;

public class PlantNetSpecies
{
    private String scientificNameWithoutAuthor;
    private String scientificNameAuthorship;
    private List<String> commonNames;
    private PlantNetTaxon genus;
    private PlantNetTaxon family;

    public String getScientificNameWithoutAuthor()
    {
        return scientificNameWithoutAuthor;
    }

    public String getScientificNameAuthorship()
    {
        return scientificNameAuthorship;
    }

    public List<String> getCommonNames()
    {
        return commonNames;
    }

    public PlantNetTaxon getGenus()
    {
        return genus;
    }

    public PlantNetTaxon getFamily()
    {
        return family;
    }
}
