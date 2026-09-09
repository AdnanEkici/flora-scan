package com.example.florascan.unit.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.florascan.model.PlantNetIdentificationResult;
import com.example.florascan.model.PlantNetResponse;
import com.example.florascan.model.PlantNetSpecies;
import com.example.florascan.model.PlantNetTaxon;
import com.example.florascan.model.PlantResult;
import com.example.florascan.service.PlantNetResultMapper;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class PlantNetResultMapperTest
{
    private PlantNetResultMapper plantNetResultMapper;

    @Before
    public void setUp()
    {
        plantNetResultMapper = new PlantNetResultMapper();
    }

    /* Verifies that a valid PlantNet response is mapped to the application plant result. */
    @Test
    public void mapCreatesPlantResultFromValidResponse()
    {
        PlantNetResponse plantNetResponse = mock(PlantNetResponse.class);
        PlantNetIdentificationResult identificationResult =
            mock(PlantNetIdentificationResult.class);
        PlantNetSpecies species = mock(PlantNetSpecies.class);
        PlantNetTaxon family = mock(PlantNetTaxon.class);
        PlantNetTaxon genus = mock(PlantNetTaxon.class);

        when(plantNetResponse.getResults())
        .thenReturn(Collections.singletonList(identificationResult));

        when(identificationResult.getSpecies())
        .thenReturn(species);

        when(identificationResult.getScore())
        .thenReturn(0.92);

        when(species.getCommonNames())
        .thenReturn(Collections.singletonList("Golden Pothos"));

        when(species.getScientificNameWithoutAuthor())
        .thenReturn("Epipremnum aureum");

        when(species.getFamily())
        .thenReturn(family);

        when(species.getGenus())
        .thenReturn(genus);

        when(family.getScientificNameWithoutAuthor())
        .thenReturn("Araceae");

        when(genus.getScientificNameWithoutAuthor())
        .thenReturn("Epipremnum");

        PlantResult plantResult =
            plantNetResultMapper.map(plantNetResponse);

        assertEquals("Golden Pothos", plantResult.getCommonName());
        assertEquals("Epipremnum aureum", plantResult.getScientificName());
        assertEquals("Araceae", plantResult.getFamily());
        assertEquals("Epipremnum", plantResult.getGenus());
        assertEquals(0.92, plantResult.getConfidence(), 0.001);
    }

    /* Verifies that a null PlantNet response does not produce a plant result. */
    @Test
    public void mapReturnsNullForNullResponse()
    {
        PlantResult plantResult =
            plantNetResultMapper.map(null);

        assertNull(plantResult);
    }

    /* Verifies that a response without identification results does not produce a plant result. */
    @Test
    public void mapReturnsNullWhenResultsAreEmpty()
    {
        PlantNetResponse plantNetResponse = mock(PlantNetResponse.class);

        when(plantNetResponse.getResults())
        .thenReturn(Collections.emptyList());

        PlantResult plantResult =
            plantNetResultMapper.map(plantNetResponse);

        assertNull(plantResult);
    }

    /* Verifies that an identification result without species information is rejected. */
    @Test
    public void mapReturnsNullWhenSpeciesIsMissing()
    {
        PlantNetResponse plantNetResponse = mock(PlantNetResponse.class);
        PlantNetIdentificationResult identificationResult =
            mock(PlantNetIdentificationResult.class);

        when(plantNetResponse.getResults())
        .thenReturn(Collections.singletonList(identificationResult));

        when(identificationResult.getSpecies())
        .thenReturn(null);

        PlantResult plantResult =
            plantNetResultMapper.map(plantNetResponse);

        assertNull(plantResult);
    }

    /* Verifies that the scientific name is used when no common plant name is available. */
    @Test
    public void mapUsesScientificNameWhenCommonNameIsMissing()
    {
        PlantNetResponse plantNetResponse = mock(PlantNetResponse.class);
        PlantNetIdentificationResult identificationResult =
            mock(PlantNetIdentificationResult.class);
        PlantNetSpecies species = mock(PlantNetSpecies.class);

        when(plantNetResponse.getResults())
        .thenReturn(Collections.singletonList(identificationResult));

        when(identificationResult.getSpecies())
        .thenReturn(species);

        when(species.getCommonNames())
        .thenReturn(Collections.emptyList());

        when(species.getScientificNameWithoutAuthor())
        .thenReturn("Monstera deliciosa");

        PlantResult plantResult =
            plantNetResultMapper.map(plantNetResponse);

        assertEquals(
            "Monstera deliciosa",
            plantResult.getCommonName()
        );
    }

    /* Verifies that missing taxonomy information is represented as unknown. */
    @Test
    public void mapUsesUnknownForMissingTaxonomy()
    {
        PlantNetResponse plantNetResponse = mock(PlantNetResponse.class);
        PlantNetIdentificationResult identificationResult =
            mock(PlantNetIdentificationResult.class);
        PlantNetSpecies species = mock(PlantNetSpecies.class);

        when(plantNetResponse.getResults())
        .thenReturn(Collections.singletonList(identificationResult));

        when(identificationResult.getSpecies())
        .thenReturn(species);

        when(species.getCommonNames())
        .thenReturn(List.of("Plant"));

        when(species.getScientificNameWithoutAuthor())
        .thenReturn("Plant species");

        when(species.getFamily())
        .thenReturn(null);

        when(species.getGenus())
        .thenReturn(null);

        PlantResult plantResult =
            plantNetResultMapper.map(plantNetResponse);

        assertEquals("Unknown", plantResult.getFamily());
        assertEquals("Unknown", plantResult.getGenus());
    }
}
