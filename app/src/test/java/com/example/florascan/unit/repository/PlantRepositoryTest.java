package com.example.florascan.unit.repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.net.Uri;

import com.example.florascan.repository.PlantRepository;
import com.example.florascan.service.PlantIdentificationCallback;
import com.example.florascan.service.PlantIdentifier;

import org.junit.Before;
import org.junit.Test;

public class PlantRepositoryTest
{
    private PlantIdentifier plantIdentifier;
    private PlantRepository plantRepository;

    @Before
    public void setUp()
    {
        plantIdentifier = mock(PlantIdentifier.class);
        plantRepository = new PlantRepository(plantIdentifier);
    }

    /* Verifies that plant identification requests are delegated to the configured identifier. */
    @Test
    public void identifyPlantDelegatesRequestToPlantIdentifier()
    {
        Uri imageUri = mock(Uri.class);
        PlantIdentificationCallback identificationCallback =
            mock(PlantIdentificationCallback.class);

        plantRepository.identifyPlant(
                           imageUri,
                           identificationCallback
                       );

        verify(plantIdentifier).identifyPlant(
            imageUri,
            identificationCallback
        );
    }
}
