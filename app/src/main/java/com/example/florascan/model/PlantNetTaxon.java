package com.example.florascan.model;
/**
 * Represents a taxonomy entry returned by the Pl@ntNet API.
 *
 * <p>The model currently stores the scientific taxon name without author
 * information. It is used for higher-level taxonomy values such as genus and
 * family within {@link PlantNetSpecies}.</p>
 */
public class PlantNetTaxon
{
    private String scientificNameWithoutAuthor;

    /**
     * Returns the scientific taxon name without author information.
     *
     * @return the scientific taxon name
     */
    public String getScientificNameWithoutAuthor()
    {
        return scientificNameWithoutAuthor;
    }
}
