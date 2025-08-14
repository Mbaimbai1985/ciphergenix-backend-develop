package com.ciphergenix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdversarialDetectionRequest {
    
    @NotNull(message = "Model ID is required")
    private String modelId;
    
    @NotNull(message = "Input data is required")
    private double[] inputData;
    
    private double[] originalPrediction;
    
    private String modelType; // e.g., "neural_network", "random_forest", etc.
    
    private ModelMetadata modelMetadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelMetadata {
        private Integer inputDimension;
        private Integer outputDimension;
        private String[] classLabels;
        private Double confidenceThreshold;
    }
}