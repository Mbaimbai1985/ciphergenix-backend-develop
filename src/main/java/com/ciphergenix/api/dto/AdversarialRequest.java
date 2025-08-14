package com.ciphergenix.api.dto;

import java.util.List;

public class AdversarialRequest {
    private List<Double> input;

    public AdversarialRequest() {
    }

    public List<Double> getInput() {
        return input;
    }

    public void setInput(List<Double> input) {
        this.input = input;
    }
}