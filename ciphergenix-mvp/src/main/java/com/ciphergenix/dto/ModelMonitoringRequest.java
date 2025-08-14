package com.ciphergenix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelMonitoringRequest {
    
    @NotNull(message = "Model ID is required")
    private String modelId;
    
    private String modelType;
    
    private ModelSnapshot currentSnapshot;
    
    private ModelSnapshot baselineSnapshot;
    
    private List<PredictionRecord> recentPredictions;
    
    private Map<String, Double> performanceMetrics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelSnapshot {
        private LocalDateTime timestamp;
        private Map<String, double[]> layerWeights;
        private Map<String, Double> outputDistribution;
        private Double accuracy;
        private Double loss;
        private String checksum;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionRecord {
        private LocalDateTime timestamp;
        private double[] input;
        private double[] output;
        private Double confidence;
        private String actualLabel;
        private String predictedLabel;
    }
}