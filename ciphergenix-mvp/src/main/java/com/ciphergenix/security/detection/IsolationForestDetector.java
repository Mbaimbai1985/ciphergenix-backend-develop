package com.ciphergenix.security.detection;

import lombok.extern.slf4j.Slf4j;
import smile.anomaly.IsolationForest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class IsolationForestDetector {
    
    private static final int DEFAULT_NUM_TREES = 100;
    private static final int DEFAULT_MAX_SAMPLES = 256;
    
    public Map<Integer, Double> detectAnomalies(double[][] data) {
        Map<Integer, Double> anomalyScores = new HashMap<>();
        
        try {
            // Build Isolation Forest model
            IsolationForest forest = IsolationForest.fit(data, DEFAULT_NUM_TREES, DEFAULT_MAX_SAMPLES);
            
            // Calculate anomaly scores for each sample
            for (int i = 0; i < data.length; i++) {
                double score = forest.score(data[i]);
                // Normalize score to [0, 1] range where 1 is most anomalous
                double normalizedScore = 1.0 - score;
                anomalyScores.put(i, normalizedScore);
            }
            
            log.debug("Isolation Forest detection completed for {} samples", data.length);
            
        } catch (Exception e) {
            log.error("Error in Isolation Forest detection", e);
            // Return empty scores on error
            for (int i = 0; i < data.length; i++) {
                anomalyScores.put(i, 0.0);
            }
        }
        
        return anomalyScores;
    }
}