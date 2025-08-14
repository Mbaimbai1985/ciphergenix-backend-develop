package com.ciphergenix.api.dto;

import java.util.List;

public class DataPoisoningRequest {
    private List<List<Double>> dataset;

    public DataPoisoningRequest() {
    }

    public List<List<Double>> getDataset() {
        return dataset;
    }

    public void setDataset(List<List<Double>> dataset) {
        this.dataset = dataset;
    }
}