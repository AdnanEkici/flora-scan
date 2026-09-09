package com.example.florascan.service;

import android.net.Uri;

public interface PlantIdentifier
{
    void identifyPlant(
        Uri imageUri,
        PlantIdentificationCallback identificationCallback
    );
}
