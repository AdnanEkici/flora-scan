package com.example.florascan.service;

import com.example.florascan.model.PlantResult;

public interface PlantIdentificationCallback
{
    void onSuccess(PlantResult plantResult);

    void onFailure(String errorMessage);
}
