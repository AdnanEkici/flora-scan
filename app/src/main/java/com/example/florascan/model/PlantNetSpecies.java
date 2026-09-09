package com.example.florascan.model;

import java.util.List;
/**
 * Represents species-level information returned by the Pl@ntNet API.
 *
 * <p>The model contains the scientific name, scientific authorship, available
 * common names, and higher-level taxonomy information such as genus and
 * family. Instances are populated from the Pl@ntNet JSON response by Gson
 * during Retrofit deserialization.</p>
 */
public class PlantNetSpecies
{
    private String scientificNameWithoutAuthor;
    private String scientificNameAuthorship;
    private List<String> commonNames;
    private PlantNetTaxon genus;
    private PlantNetTaxon family;

    /**
     * Returns the scientific species name without the author citation.
     *
     * @return the scientific name without authorship information
     */
    public String getScientificNameWithoutAuthor()
    {
        return scientificNameWithoutAuthor;
    }
    /**
     * Returns the scientific authorship associated with the species name.
     *
     * @return the scientific name authorship
     */
    public String getScientificNameAuthorship()
    {
        return scientificNameAuthorship;
    }

    /**
     * Returns the common names associated with the species.
     *
     * @return the list of available common names
     */
    public List<String> getCommonNames()
    {
        return commonNames;
    }

    /**
     * Returns the genus taxonomy information for the species.
     *
     * @return the genus taxon
     */

    public PlantNetTaxon getGenus()
    {
        return genus;
    }
    /**
     * Returns the family taxonomy information for the species.
     *
     * @return the family taxon
     */
    public PlantNetTaxon getFamily()
    {
        return family;
    }
}
