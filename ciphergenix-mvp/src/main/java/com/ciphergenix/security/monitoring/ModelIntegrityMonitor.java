package com.ciphergenix.security.monitoring;

import com.ciphergenix.dto.DetectionResponse;
import com.ciphergenix.dto.ModelMonitoringRequest;
import com.ciphergenix.model.ThreatLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ModelIntegrityMonitor {
    
    @Value("${ai.detection.monitoring.drift-threshold}")
    private double driftThreshold;
    
    @Value("${ai.detection.monitoring.performance-degradation-threshold}")
    private double performanceDegradationThreshold;
    
    private final ModelBehaviorAnalyzer behaviorAnalyzer;
    private final DriftDetector driftDetector;
    private final ModelFingerprinter fingerprinter;
    
    public ModelIntegrityMonitor() {
        this.behaviorAnalyzer = new ModelBehaviorAnalyzer();
        this.driftDetector = new DriftDetector();
        this.fingerprinter = new ModelFingerprinter();
    }
    
    public DetectionResponse monitorModelIntegrity(ModelMonitoringRequest request) {
        log.info("Starting model integrity monitoring for model: {}", request.getModelId());
        
        Map<String, Object> integrityResults = new HashMap<>();
        List<String> violations = new ArrayList<>();
        double overallThreatScore = 0.0;
        
        // 1. Prediction Consistency Monitoring
        if (request.getRecentPredictions() != null && !request.getRecentPredictions().isEmpty()) {
            ConsistencyResult consistencyResult = behaviorAnalyzer.analyzePredictionConsistency(
                request.getRecentPredictions()
            );
            integrityResults.put("consistencyAnalysis", consistencyResult);
            
            if (consistencyResult.hasAnomaly()) {
                violations.add("Prediction consistency anomaly detected");
                overallThreatScore = Math.max(overallThreatScore, consistencyResult.getAnomalyScore());
            }
        }
        
        // 2. Decision Boundary Analysis
        if (request.getCurrentSnapshot() != null && request.getBaselineSnapshot() != null) {
            BoundaryChangeResult boundaryResult = behaviorAnalyzer.analyzeDecisionBoundaries(
                request.getCurrentSnapshot(), request.getBaselineSnapshot()
            );
            integrityResults.put("boundaryAnalysis", boundaryResult);
            
            if (boundaryResult.hasSignificantChange()) {
                violations.add("Significant decision boundary changes detected");
                overallThreatScore = Math.max(overallThreatScore, boundaryResult.getChangeScore());
            }
        }
        
        // 3. Performance Degradation Detection
        if (request.getPerformanceMetrics() != null) {
            DegradationResult degradationResult = detectPerformanceDegradation(
                request.getPerformanceMetrics()
            );
            integrityResults.put("performanceAnalysis", degradationResult);
            
            if (degradationResult.hasDegradation()) {
                violations.add("Performance degradation detected");
                overallThreatScore = Math.max(overallThreatScore, degradationResult.getDegradationScore());
            }
        }
        
        // 4. Model Fingerprinting
        if (request.getCurrentSnapshot() != null) {
            FingerprintResult fingerprintResult = fingerprinter.generateFingerprint(
                request.getCurrentSnapshot()
            );
            integrityResults.put("fingerprint", fingerprintResult);
            
            // Check for tampering
            if (request.getBaselineSnapshot() != null && 
                request.getBaselineSnapshot().getChecksum() != null &&
                !fingerprintResult.getChecksum().equals(request.getBaselineSnapshot().getChecksum())) {
                violations.add("Model tampering detected - checksum mismatch");
                overallThreatScore = Math.max(overallThreatScore, 0.9);
            }
        }
        
        // 5. Distribution Drift Detection
        if (request.getCurrentSnapshot() != null && request.getBaselineSnapshot() != null) {
            DriftResult driftResult = driftDetector.detectDrift(
                request.getCurrentSnapshot(), request.getBaselineSnapshot()
            );
            integrityResults.put("driftAnalysis", driftResult);
            
            if (driftResult.hasDrift()) {
                violations.add("Model drift detected");
                overallThreatScore = Math.max(overallThreatScore, driftResult.getDriftScore());
            }
        }
        
        ThreatLevel threatLevel = determineThreatLevel(overallThreatScore, violations);
        
        return buildMonitoringResponse(
            integrityResults, violations, overallThreatScore, threatLevel, request
        );
    }
    
    private DegradationResult detectPerformanceDegradation(Map<String, Double> metrics) {
        boolean hasDegradation = false;
        double degradationScore = 0.0;
        Map<String, Double> degradationDetails = new HashMap<>();
        
        // Check accuracy degradation
        if (metrics.containsKey("accuracy") && metrics.containsKey("baseline_accuracy")) {
            double currentAccuracy = metrics.get("accuracy");
            double baselineAccuracy = metrics.get("baseline_accuracy");
            double degradation = (baselineAccuracy - currentAccuracy) / baselineAccuracy;
            
            if (degradation > performanceDegradationThreshold) {
                hasDegradation = true;
                degradationScore = Math.max(degradationScore, degradation);
                degradationDetails.put("accuracy_degradation", degradation);
            }
        }
        
        // Check loss increase
        if (metrics.containsKey("loss") && metrics.containsKey("baseline_loss")) {
            double currentLoss = metrics.get("loss");
            double baselineLoss = metrics.get("baseline_loss");
            double lossIncrease = (currentLoss - baselineLoss) / (baselineLoss + 1e-10);
            
            if (lossIncrease > performanceDegradationThreshold) {
                hasDegradation = true;
                degradationScore = Math.max(degradationScore, lossIncrease);
                degradationDetails.put("loss_increase", lossIncrease);
            }
        }
        
        // Check F1 score degradation
        if (metrics.containsKey("f1_score") && metrics.containsKey("baseline_f1_score")) {
            double currentF1 = metrics.get("f1_score");
            double baselineF1 = metrics.get("baseline_f1_score");
            double f1Degradation = (baselineF1 - currentF1) / baselineF1;
            
            if (f1Degradation > performanceDegradationThreshold) {
                hasDegradation = true;
                degradationScore = Math.max(degradationScore, f1Degradation);
                degradationDetails.put("f1_degradation", f1Degradation);
            }
        }
        
        return new DegradationResult(hasDegradation, degradationScore, degradationDetails);
    }
    
    private ThreatLevel determineThreatLevel(double threatScore, List<String> violations) {
        if (violations.isEmpty()) {
            return ThreatLevel.LOW;
        }
        
        if (threatScore >= 0.8 || violations.size() >= 4) {
            return ThreatLevel.CRITICAL;
        } else if (threatScore >= 0.6 || violations.size() >= 3) {
            return ThreatLevel.HIGH;
        } else if (threatScore >= 0.4 || violations.size() >= 2) {
            return ThreatLevel.MEDIUM;
        } else {
            return ThreatLevel.LOW;
        }
    }
    
    private DetectionResponse buildMonitoringResponse(
            Map<String, Object> results,
            List<String> violations,
            double threatScore,
            ThreatLevel threatLevel,
            ModelMonitoringRequest request) {
        
        Map<String, Object> detectionDetails = new HashMap<>();
        detectionDetails.putAll(results);
        detectionDetails.put("violations", violations);
        detectionDetails.put("modelId", request.getModelId());
        detectionDetails.put("monitoringTimestamp", LocalDateTime.now());
        
        List<DetectionResponse.AnomalySample> anomalies = new ArrayList<>();
        if (!violations.isEmpty()) {
            // Create a summary anomaly entry
            DetectionResponse.AnomalySample anomaly = DetectionResponse.AnomalySample.builder()
                .sampleIndex(0)
                .features(new double[]{threatScore})
                .anomalyScore(threatScore)
                .anomalyType("MODEL_INTEGRITY")
                .featureContributions(analyzeViolationContributions(results))
                .build();
            anomalies.add(anomaly);
        }
        
        String recommendation = generateRecommendation(threatLevel, violations, results);
        
        return DetectionResponse.builder()
            .detectionId(UUID.randomUUID().toString())
            .detectionType("MODEL_INTEGRITY_MONITORING")
            .threatScore(threatScore)
            .threatLevel(threatLevel)
            .anomalousSamples(anomalies)
            .detectionDetails(detectionDetails)
            .timestamp(LocalDateTime.now())
            .recommendation(recommendation)
            .build();
    }
    
    private Map<String, Double> analyzeViolationContributions(Map<String, Object> results) {
        Map<String, Double> contributions = new HashMap<>();
        
        if (results.containsKey("consistencyAnalysis")) {
            ConsistencyResult cr = (ConsistencyResult) results.get("consistencyAnalysis");
            contributions.put("consistency", cr.hasAnomaly() ? cr.getAnomalyScore() : 0.0);
        }
        
        if (results.containsKey("boundaryAnalysis")) {
            BoundaryChangeResult bcr = (BoundaryChangeResult) results.get("boundaryAnalysis");
            contributions.put("boundary_change", bcr.hasSignificantChange() ? bcr.getChangeScore() : 0.0);
        }
        
        if (results.containsKey("performanceAnalysis")) {
            DegradationResult dr = (DegradationResult) results.get("performanceAnalysis");
            contributions.put("performance", dr.hasDegradation() ? dr.getDegradationScore() : 0.0);
        }
        
        if (results.containsKey("driftAnalysis")) {
            DriftResult dr = (DriftResult) results.get("driftAnalysis");
            contributions.put("drift", dr.hasDrift() ? dr.getDriftScore() : 0.0);
        }
        
        return contributions;
    }
    
    private String generateRecommendation(ThreatLevel level, List<String> violations, Map<String, Object> results) {
        if (violations.isEmpty()) {
            return "Model integrity verified. No anomalies detected.";
        }
        
        String violationSummary = String.join(", ", violations);
        
        switch (level) {
            case CRITICAL:
                return String.format(
                    "CRITICAL: Multiple integrity violations detected [%s]. " +
                    "Immediately isolate the model and investigate potential compromise. " +
                    "Consider rolling back to a known good version.",
                    violationSummary
                );
            case HIGH:
                return String.format(
                    "HIGH: Significant integrity concerns detected [%s]. " +
                    "Suspend model deployment and perform thorough validation. " +
                    "Compare with baseline model behavior.",
                    violationSummary
                );
            case MEDIUM:
                return String.format(
                    "MEDIUM: Integrity anomalies detected [%s]. " +
                    "Monitor closely and validate model outputs. " +
                    "Consider retraining or recalibration.",
                    violationSummary
                );
            case LOW:
            default:
                return String.format(
                    "LOW: Minor integrity concerns [%s]. " +
                    "Continue monitoring and log for analysis.",
                    violationSummary
                );
        }
    }
    
    // Inner classes for results
    private static class ConsistencyResult {
        private final boolean hasAnomaly;
        private final double anomalyScore;
        private final Map<String, Object> details;
        
        public ConsistencyResult(boolean hasAnomaly, double anomalyScore, Map<String, Object> details) {
            this.hasAnomaly = hasAnomaly;
            this.anomalyScore = anomalyScore;
            this.details = details;
        }
        
        public boolean hasAnomaly() { return hasAnomaly; }
        public double getAnomalyScore() { return anomalyScore; }
        public Map<String, Object> getDetails() { return details; }
    }
    
    private static class BoundaryChangeResult {
        private final boolean hasSignificantChange;
        private final double changeScore;
        private final Map<String, Object> details;
        
        public BoundaryChangeResult(boolean hasSignificantChange, double changeScore, Map<String, Object> details) {
            this.hasSignificantChange = hasSignificantChange;
            this.changeScore = changeScore;
            this.details = details;
        }
        
        public boolean hasSignificantChange() { return hasSignificantChange; }
        public double getChangeScore() { return changeScore; }
        public Map<String, Object> getDetails() { return details; }
    }
    
    private static class DegradationResult {
        private final boolean hasDegradation;
        private final double degradationScore;
        private final Map<String, Double> details;
        
        public DegradationResult(boolean hasDegradation, double degradationScore, Map<String, Double> details) {
            this.hasDegradation = hasDegradation;
            this.degradationScore = degradationScore;
            this.details = details;
        }
        
        public boolean hasDegradation() { return hasDegradation; }
        public double getDegradationScore() { return degradationScore; }
        public Map<String, Double> getDetails() { return details; }
    }
    
    private static class FingerprintResult {
        private final String checksum;
        private final Map<String, String> layerChecksums;
        
        public FingerprintResult(String checksum, Map<String, String> layerChecksums) {
            this.checksum = checksum;
            this.layerChecksums = layerChecksums;
        }
        
        public String getChecksum() { return checksum; }
        public Map<String, String> getLayerChecksums() { return layerChecksums; }
    }
    
    private static class DriftResult {
        private final boolean hasDrift;
        private final double driftScore;
        private final Map<String, Double> layerDrifts;
        
        public DriftResult(boolean hasDrift, double driftScore, Map<String, Double> layerDrifts) {
            this.hasDrift = hasDrift;
            this.driftScore = driftScore;
            this.layerDrifts = layerDrifts;
        }
        
        public boolean hasDrift() { return hasDrift; }
        public double getDriftScore() { return driftScore; }
        public Map<String, Double> getLayerDrifts() { return layerDrifts; }
    }
}