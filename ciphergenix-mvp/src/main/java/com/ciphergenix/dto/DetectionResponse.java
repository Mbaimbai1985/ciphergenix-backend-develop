package com.ciphergenix.dto;

import com.ciphergenix.model.ThreatLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResponse {
    
    private String detectionId;
    private String detectionType;
    private Double threatScore;
    private ThreatLevel threatLevel;
    private List<AnomalySample> anomalousSamples;
    private Map<String, Object> detectionDetails;
    private LocalDateTime timestamp;
    private String recommendation;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalySample {
        private Integer sampleIndex;
        private double[] features;
        private Double anomalyScore;
        private String anomalyType;
        private Map<String, Double> featureContributions;
    }
}