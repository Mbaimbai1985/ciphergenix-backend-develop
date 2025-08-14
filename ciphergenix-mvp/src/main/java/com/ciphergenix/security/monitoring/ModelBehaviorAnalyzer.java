package com.ciphergenix.security.monitoring;

import com.ciphergenix.dto.ModelMonitoringRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModelBehaviorAnalyzer {
    
    private static final double CONSISTENCY_THRESHOLD = 0.15;
    private static final double BOUNDARY_CHANGE_THRESHOLD = 0.2;
    
    public ModelIntegrityMonitor.ConsistencyResult analyzePredictionConsistency(
            List<ModelMonitoringRequest.PredictionRecord> predictions) {
        
        Map<String, Object> details = new HashMap<>();
        boolean hasAnomaly = false;
        double anomalyScore = 0.0;
        
        // Analyze confidence distribution
        DescriptiveStatistics confidenceStats = new DescriptiveStatistics();
        for (ModelMonitoringRequest.PredictionRecord record : predictions) {
            if (record.getConfidence() != null) {
                confidenceStats.addValue(record.getConfidence());
            }
        }
        
        double confidenceMean = confidenceStats.getMean();
        double confidenceStd = confidenceStats.getStandardDeviation();
        details.put("confidenceMean", confidenceMean);
        details.put("confidenceStd", confidenceStd);
        
        // Check for sudden confidence drops
        int lowConfidenceCount = 0;
        double confidenceThreshold = confidenceMean - 2 * confidenceStd;
        
        for (ModelMonitoringRequest.PredictionRecord record : predictions) {
            if (record.getConfidence() != null && record.getConfidence() < confidenceThreshold) {
                lowConfidenceCount++;
            }
        }
        
        double lowConfidenceRate = (double) lowConfidenceCount / predictions.size();
        details.put("lowConfidenceRate", lowConfidenceRate);
        
        if (lowConfidenceRate > CONSISTENCY_THRESHOLD) {
            hasAnomaly = true;
            anomalyScore = Math.max(anomalyScore, lowConfidenceRate / CONSISTENCY_THRESHOLD);
        }
        
        // Analyze prediction stability
        Map<String, Integer> labelCounts = new HashMap<>();
        int mismatchCount = 0;
        
        for (ModelMonitoringRequest.PredictionRecord record : predictions) {
            if (record.getPredictedLabel() != null) {
                labelCounts.merge(record.getPredictedLabel(), 1, Integer::sum);
                
                if (record.getActualLabel() != null && 
                    !record.getPredictedLabel().equals(record.getActualLabel())) {
                    mismatchCount++;
                }
            }
        }
        
        // Calculate prediction entropy
        double entropy = calculateEntropy(labelCounts, predictions.size());
        details.put("predictionEntropy", entropy);
        
        // High entropy might indicate unstable predictions
        if (entropy > 2.0) { // threshold for entropy
            hasAnomaly = true;
            anomalyScore = Math.max(anomalyScore, entropy / 3.0);
        }
        
        // Check error rate
        double errorRate = (double) mismatchCount / predictions.size();
        details.put("errorRate", errorRate);
        
        if (errorRate > 0.3) { // 30% error threshold
            hasAnomaly = true;
            anomalyScore = Math.max(anomalyScore, errorRate);
        }
        
        return new ModelIntegrityMonitor.ConsistencyResult(hasAnomaly, anomalyScore, details);
    }
    
    public ModelIntegrityMonitor.BoundaryChangeResult analyzeDecisionBoundaries(
            ModelMonitoringRequest.ModelSnapshot current,
            ModelMonitoringRequest.ModelSnapshot baseline) {
        
        Map<String, Object> details = new HashMap<>();
        boolean hasSignificantChange = false;
        double changeScore = 0.0;
        
        // Compare output distributions
        if (current.getOutputDistribution() != null && baseline.getOutputDistribution() != null) {
            double klDivergence = calculateKLDivergence(
                current.getOutputDistribution(), 
                baseline.getOutputDistribution()
            );
            details.put("outputDistributionKL", klDivergence);
            
            if (klDivergence > BOUNDARY_CHANGE_THRESHOLD) {
                hasSignificantChange = true;
                changeScore = Math.max(changeScore, klDivergence);
            }
        }
        
        // Compare layer weights if available
        if (current.getLayerWeights() != null && baseline.getLayerWeights() != null) {
            Map<String, Double> layerChanges = new HashMap<>();
            
            for (String layerName : current.getLayerWeights().keySet()) {
                if (baseline.getLayerWeights().containsKey(layerName)) {
                    double[] currentWeights = current.getLayerWeights().get(layerName);
                    double[] baselineWeights = baseline.getLayerWeights().get(layerName);
                    
                    double weightChange = calculateWeightChange(currentWeights, baselineWeights);
                    layerChanges.put(layerName, weightChange);
                    
                    if (weightChange > BOUNDARY_CHANGE_THRESHOLD) {
                        hasSignificantChange = true;
                        changeScore = Math.max(changeScore, weightChange);
                    }
                }
            }
            
            details.put("layerWeightChanges", layerChanges);
        }
        
        // Compare performance metrics
        if (current.getAccuracy() != null && baseline.getAccuracy() != null) {
            double accuracyChange = Math.abs(current.getAccuracy() - baseline.getAccuracy());
            details.put("accuracyChange", accuracyChange);
            
            if (accuracyChange > 0.1) { // 10% accuracy change threshold
                hasSignificantChange = true;
                changeScore = Math.max(changeScore, accuracyChange * 2);
            }
        }
        
        return new ModelIntegrityMonitor.BoundaryChangeResult(hasSignificantChange, changeScore, details);
    }
    
    private double calculateEntropy(Map<String, Integer> labelCounts, int total) {
        double entropy = 0.0;
        
        for (int count : labelCounts.values()) {
            if (count > 0) {
                double probability = (double) count / total;
                entropy -= probability * Math.log(probability) / Math.log(2);
            }
        }
        
        return entropy;
    }
    
    private double calculateKLDivergence(Map<String, Double> p, Map<String, Double> q) {
        double klDiv = 0.0;
        
        for (Map.Entry<String, Double> entry : p.entrySet()) {
            String key = entry.getKey();
            double pValue = entry.getValue();
            double qValue = q.getOrDefault(key, 1e-10);
            
            if (pValue > 0) {
                klDiv += pValue * Math.log(pValue / qValue);
            }
        }
        
        return klDiv;
    }
    
    private double calculateWeightChange(double[] current, double[] baseline) {
        if (current.length != baseline.length) {
            return 1.0; // Maximum change if dimensions don't match
        }
        
        double totalChange = 0.0;
        double totalMagnitude = 0.0;
        
        for (int i = 0; i < current.length; i++) {
            totalChange += Math.abs(current[i] - baseline[i]);
            totalMagnitude += Math.abs(baseline[i]);
        }
        
        return totalMagnitude > 0 ? totalChange / totalMagnitude : totalChange;
    }
}