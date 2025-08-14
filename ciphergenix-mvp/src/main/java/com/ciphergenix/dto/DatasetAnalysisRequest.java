package com.ciphergenix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetAnalysisRequest {
    
    @NotNull(message = "Dataset ID is required")
    private String datasetId;
    
    @NotNull(message = "Model ID is required")
    private String modelId;
    
    @NotNull(message = "Data samples are required")
    @Size(min = 1, message = "At least one data sample is required")
    private List<double[]> dataSamples;
    
    private List<String> featureNames;
    
    private BaselineStatistics baselineStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaselineStatistics {
        private double[] means;
        private double[] standardDeviations;
        private double[][] covarianceMatrix;
        private Integer sampleCount;
    }
}