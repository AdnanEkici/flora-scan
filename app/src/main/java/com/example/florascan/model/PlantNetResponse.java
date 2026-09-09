package com.example.florascan.model;

import java.util.List;

public class PlantNetResponse
{
    private String bestMatch;
    private List<PlantNetIdentificationResult> results;
    private int remainingIdentificationRequests;

    public String getBestMatch()
    {
        return bestMatch;
    }

    public List<PlantNetIdentificationResult> getResults()
    {
        return results;
    }

    public int getRemainingIdentificationRequests()
    {
        return remainingIdentificationRequests;
    }
}
