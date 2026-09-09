package com.example.florascan.service;

import com.example.florascan.model.PlantNetIdentificationResult;
import com.example.florascan.model.PlantNetResponse;
import com.example.florascan.model.PlantNetSpecies;
import com.example.florascan.model.PlantNetTaxon;
import com.example.florascan.model.PlantResult;

import java.util.List;
/**
 * Maps Pl@ntNet-specific identification responses to the application's
 * provider-independent {@link PlantResult} model.
 *
 * <p>The mapper selects the highest-ranked identification result returned by
 * Pl@ntNet, extracts the common name, scientific name, family, genus, and
 * confidence score, and converts them into the simplified domain model used by
 * the rest of the application.</p>
 *
 * <p>Missing taxonomy values are replaced with a readable fallback value.
 * Responses that do not contain a usable identification result return
 * {@code null}, allowing callers to treat them as no-match results.</p>
 */
public class PlantNetResultMapper
{
    private static final String UNKNOWN_VALUE = "Unknown";
    /**
     * Converts a Pl@ntNet response into the application's plant result model.
     *
     * <p>The first identification result is treated as the highest-ranked match.
     * If the response is missing, contains no results, or does not include species
     * information, no plant result can be produced.</p>
     *
     * @param plantNetResponse the response returned by the Pl@ntNet API
     * @return the mapped plant result, or {@code null} when no usable
     *         identification result is available
     */
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
    /**
     * Retrieves the preferred common name for the supplied species.
     *
     * <p>If no common name is available, the scientific name is used as a
     * fallback.</p>
     *
     * @param species the species containing the available naming information
     * @return the preferred common name or scientific-name fallback
     */
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
    /**
     * Retrieves the scientific name for the supplied species.
     *
     * @param species the species containing scientific naming information
     * @return the scientific name, or {@code "Unknown"} when it is unavailable
     */
    private String getScientificName(PlantNetSpecies species)
    {
        String scientificName = species.getScientificNameWithoutAuthor();

        if (scientificName == null || scientificName.isEmpty())
        {
            scientificName = UNKNOWN_VALUE;
        }

        return scientificName;
    }
    /**
     * Retrieves the scientific name of a taxonomy entry.
     *
     * @param taxon the taxonomy entry to inspect
     * @return the taxonomy name, or {@code "Unknown"} when the taxon or its name
     *         is unavailable
     */
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
