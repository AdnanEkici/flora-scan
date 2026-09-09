package com.example.florascan.service;

import com.example.florascan.model.PlantNetIdentificationResult;
import com.example.florascan.model.PlantNetResponse;
import com.example.florascan.model.PlantNetSpecies;
import com.example.florascan.model.PlantNetTaxon;
import com.example.florascan.model.PlantResult;

import java.util.List;

public class PlantNetResultMapper
{
    private static final String UNKNOWN_VALUE = "Unknown";

    public PlantResult map(PlantNetResponse plantNetResponse)
    {
        if (plantNetResponse == null)
        {
            return null;
        }

        List<PlantNetIdentificationResult> identificationResults =
            plantNetResponse.getResults();

        if (identificationResults == null || identificationResults.isEmpty())
        {
            return null;
        }

        PlantNetIdentificationResult identificationResult =
            identificationResults.get(0);

        PlantNetSpecies species = identificationResult.getSpecies();

        if (species == null)
        {
            return null;
        }

        String commonName = getCommonName(species);
        String scientificName = getScientificName(species);
        String family = getTaxonName(species.getFamily());
        String genus = getTaxonName(species.getGenus());
        double confidence = identificationResult.getScore();

        PlantResult plantResult = new PlantResult(
            commonName,
            scientificName,
            family,
            genus,
            confidence
        );

        return plantResult;
    }

    private String getCommonName(PlantNetSpecies species)
    {
        List<String> commonNames = species.getCommonNames();

        if (commonNames == null || commonNames.isEmpty())
        {
            String scientificName = getScientificName(species);

            return scientificName;
        }

        String commonName = commonNames.get(0);

        return commonName;
    }

    private String getScientificName(PlantNetSpecies species)
    {
        String scientificName = species.getScientificNameWithoutAuthor();

        if (scientificName == null || scientificName.isEmpty())
        {
            scientificName = UNKNOWN_VALUE;
        }

        return scientificName;
    }

    private String getTaxonName(PlantNetTaxon taxon)
    {
        if (taxon == null)
        {
            return UNKNOWN_VALUE;
        }

        String taxonName = taxon.getScientificNameWithoutAuthor();

        if (taxonName == null || taxonName.isEmpty())
        {
            taxonName = UNKNOWN_VALUE;
        }

        return taxonName;
    }
}
