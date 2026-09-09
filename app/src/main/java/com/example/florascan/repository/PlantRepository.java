package com.example.florascan.repository;

import android.net.Uri;

import com.example.florascan.service.PlantIdentificationCallback;
import com.example.florascan.service.PlantIdentifier;

public class PlantRepository
{
    private final PlantIdentifier plantIdentifier;

    public PlantRepository(PlantIdentifier plantIdentifier)
    {
        this.plantIdentifier = plantIdentifier;
    }

    public void identifyPlant(
        Uri imageUri,
        PlantIdentificationCallback identificationCallback)
    {
        plantIdentifier.identifyPlant(
                           imageUri,
                           identificationCallback
                       );
    }
}
