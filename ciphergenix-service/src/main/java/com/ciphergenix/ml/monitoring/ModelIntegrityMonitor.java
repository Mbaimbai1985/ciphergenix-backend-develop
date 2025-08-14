package com.ciphergenix.ml.monitoring;

import com.ciphergenix.domain.AIModel;
import com.ciphergenix.domain.ThreatDetection;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ModelIntegrityMonitor {
    
    @Value("${ciphergenix.monitoring.alert.response.automated:true}")
    private boolean automatedResponse;
    
    @Value("${ciphergenix.monitoring.alert.response.escalation-delay:300}")
    private int escalationDelaySeconds;
    
    // In-memory storage for model monitoring data
    private final Map<String, ModelMonitoringData> modelMonitoringData = new ConcurrentHashMap<>();
    private final Map<String, ModelFingerprint> modelFingerprints = new ConcurrentHashMap<>();
    private final Map<String, QueryPattern> queryPatterns = new ConcurrentHashMap<>();
    
    /**
     * Monitor model behavior and detect integrity issues
     */
    public MonitoringResult monitorModelIntegrity(String modelId, double[] input, double[] output, 
                                                 Map<String, Object> metadata) {
        MonitoringResult result = new MonitoringResult();
        
        // 1. Prediction Consistency Monitoring
        boolean predictionConsistent = monitorPredictionConsistency(modelId, input, output);
        
        // 2. Decision Boundary Analysis
        boolean boundaryStable = analyzeDecisionBoundary(modelId, input, output);
        
        // 3. Model Fingerprinting
        boolean fingerprintValid = validateModelFingerprint(modelId, input, output);
        
        // 4. Query Pattern Analysis
        boolean patternNormal = analyzeQueryPattern(modelId, metadata);
        
        // 5. Performance Degradation Detection
        boolean performanceStable = detectPerformanceDegradation(modelId, output);
        
        // Overall integrity assessment
        boolean modelIntegrity = predictionConsistent && boundaryStable && 
                                fingerprintValid && patternNormal && performanceStable;
        
        result.setModelIntegrity(modelIntegrity);
        result.setIntegrityScore(calculateIntegrityScore(predictionConsistent, boundaryStable, 
                                                       fingerprintValid, patternNormal, performanceStable));
        result.setIssuesDetected(identifyIssues(predictionConsistent, boundaryStable, 
                                              fingerprintValid, patternNormal, performanceStable));
        
        // Update monitoring data
        updateMonitoringData(modelId, input, output, metadata, result);
        
        return result;
    }
    
    /**
     * Monitor prediction consistency over time
     */
    private boolean monitorPredictionConsistency(String modelId, double[] input, double[] output) {
        ModelMonitoringData data = modelMonitoringData.get(modelId);
        if (data == null) {
            data = new ModelMonitoringData();
            modelMonitoringData.put(modelId, data);
        }
        
        // Store prediction data
        data.addPrediction(input, output, LocalDateTime.now());
        
        // Check for consistency drift
        if (data.getPredictionCount() > 10) {
            double consistencyScore = calculatePredictionConsistency(data);
            return consistencyScore > 0.8; // Threshold for consistency
        }
        
        return true; // Not enough data yet
    }
    
    /**
     * Analyze decision boundary stability
     */
    private boolean analyzeDecisionBoundary(String modelId, double[] input, double[] output) {
        ModelMonitoringData data = modelMonitoringData.get(modelId);
        if (data == null) return true;
        
        // Check for sudden changes in decision boundaries
        if (data.getPredictionCount() > 5) {
            double boundaryStability = calculateBoundaryStability(data);
            return boundaryStability > 0.7; // Threshold for stability
        }
        
        return true;
    }
    
    /**
     * Validate model fingerprint
     */
    private boolean validateModelFingerprint(String modelId, double[] input, double[] output) {
        ModelFingerprint fingerprint = modelFingerprints.get(modelId);
        if (fingerprint == null) {
            // Create new fingerprint
            fingerprint = createModelFingerprint(modelId, input, output);
            modelFingerprints.put(modelId, fingerprint);
            return true;
        }
        
        // Validate against existing fingerprint
        double similarity = calculateFingerprintSimilarity(fingerprint, input, output);
        return similarity > 0.9; // Threshold for fingerprint validation
    }
    
    /**
     * Analyze query patterns for model theft detection
     */
    private boolean analyzeQueryPattern(String modelId, Map<String, Object> metadata) {
        QueryPattern pattern = queryPatterns.get(modelId);
        if (pattern == null) {
            pattern = new QueryPattern();
            queryPatterns.put(modelId, pattern);
        }
        
        // Update query pattern
        pattern.addQuery(metadata);
        
        // Check for suspicious patterns
        if (pattern.getQueryCount() > 20) {
            return !detectSuspiciousPattern(pattern);
        }
        
        return true;
    }
    
    /**
     * Detect performance degradation
     */
    private boolean detectPerformanceDegradation(String modelId, double[] output) {
        ModelMonitoringData data = modelMonitoringData.get(modelId);
        if (data == null) return true;
        
        // Check for performance drift
        if (data.getPredictionCount() > 10) {
            double performanceScore = calculatePerformanceScore(data, output);
            return performanceScore > 0.75; // Threshold for performance
        }
        
        return true;
    }
    
    /**
     * Calculate overall integrity score
     */
    private double calculateIntegrityScore(boolean predictionConsistent, boolean boundaryStable,
                                         boolean fingerprintValid, boolean patternNormal, boolean performanceStable) {
        int totalChecks = 5;
        int passedChecks = 0;
        
        if (predictionConsistent) passedChecks++;
        if (boundaryStable) passedChecks++;
        if (fingerprintValid) passedChecks++;
        if (patternNormal) passedChecks++;
        if (performanceStable) passedChecks++;
        
        return (double) passedChecks / totalChecks;
    }
    
    /**
     * Identify specific issues
     */
    private List<String> identifyIssues(boolean predictionConsistent, boolean boundaryStable,
                                      boolean fingerprintValid, boolean patternNormal, boolean performanceStable) {
        List<String> issues = new ArrayList<>();
        
        if (!predictionConsistent) issues.add("Prediction consistency drift detected");
        if (!boundaryStable) issues.add("Decision boundary instability detected");
        if (!fingerprintValid) issues.add("Model fingerprint mismatch detected");
        if (!patternNormal) issues.add("Suspicious query pattern detected");
        if (!performanceStable) issues.add("Performance degradation detected");
        
        return issues;
    }
    
    // Helper methods for monitoring calculations
    private double calculatePredictionConsistency(ModelMonitoringData data) {
        List<PredictionRecord> predictions = data.getRecentPredictions(10);
        if (predictions.size() < 2) return 1.0;
        
        double totalSimilarity = 0.0;
        int comparisons = 0;
        
        for (int i = 0; i < predictions.size() - 1; i++) {
            for (int j = i + 1; j < predictions.size(); j++) {
                double similarity = calculateOutputSimilarity(predictions.get(i).getOutput(), 
                                                           predictions.get(j).getOutput());
                totalSimilarity += similarity;
                comparisons++;
            }
        }
        
        return comparisons > 0 ? totalSimilarity / comparisons : 1.0;
    }
    
    private double calculateBoundaryStability(ModelMonitoringData data) {
        List<PredictionRecord> predictions = data.getRecentPredictions(5);
        if (predictions.size() < 3) return 1.0;
        
        // Calculate variance in decision boundaries
        double[] boundaryVariances = new double[predictions.get(0).getOutput().length];
        
        for (int feature = 0; feature < boundaryVariances.length; feature++) {
            double[] featureValues = predictions.stream()
                    .mapToDouble(p -> p.getInput()[feature])
                    .toArray();
            
            boundaryVariances[feature] = calculateVariance(featureValues);
        }
        
        // Return stability score (lower variance = higher stability)
        double averageVariance = Arrays.stream(boundaryVariances).average().orElse(0.0);
        return Math.exp(-averageVariance);
    }
    
    private ModelFingerprint createModelFingerprint(String modelId, double[] input, double[] output) {
        ModelFingerprint fingerprint = new ModelFingerprint();
        fingerprint.setModelId(modelId);
        fingerprint.setCreatedAt(LocalDateTime.now());
        
        // Create fingerprint based on input-output mapping characteristics
        fingerprint.setInputSignature(createInputSignature(input));
        fingerprint.setOutputSignature(createOutputSignature(output));
        
        return fingerprint;
    }
    
    private double calculateFingerprintSimilarity(ModelFingerprint fingerprint, double[] input, double[] output) {
        double inputSimilarity = calculateSignatureSimilarity(fingerprint.getInputSignature(), 
                                                           createInputSignature(input));
        double outputSimilarity = calculateSignatureSimilarity(fingerprint.getOutputSignature(), 
                                                            createOutputSignature(output));
        
        return (inputSimilarity + outputSimilarity) / 2.0;
    }
    
    private boolean detectSuspiciousPattern(QueryPattern pattern) {
        // Check for systematic probing patterns
        double systematicScore = pattern.calculateSystematicScore();
        double rateLimitScore = pattern.calculateRateLimitScore();
        
        return systematicScore > 0.8 || rateLimitScore > 0.9;
    }
    
    private double calculatePerformanceScore(ModelMonitoringData data, double[] currentOutput) {
        List<PredictionRecord> predictions = data.getRecentPredictions(10);
        if (predictions.isEmpty()) return 1.0;
        
        // Calculate performance based on output confidence and consistency
        double averageConfidence = predictions.stream()
                .mapToDouble(p -> Arrays.stream(p.getOutput()).max().orElse(0.0))
                .average()
                .orElse(1.0);
        
        double currentConfidence = Arrays.stream(currentOutput).max().orElse(0.0);
        
        return Math.min(currentConfidence / averageConfidence, 1.0);
    }
    
    // Utility methods
    private double calculateOutputSimilarity(double[] output1, double[] output2) {
        if (output1.length != output2.length) return 0.0;
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < output1.length; i++) {
            dotProduct += output1[i] * output2[i];
            norm1 += output1[i] * output1[i];
            norm2 += output2[i] * output2[i];
        }
        
        if (norm1 == 0 || norm2 == 0) return 0.0;
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    private double calculateVariance(double[] values) {
        double mean = Arrays.stream(values).average().orElse(0.0);
        return Arrays.stream(values)
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
    }
    
    private String createInputSignature(double[] input) {
        // Create a simple hash-based signature
        return Arrays.toString(input);
    }
    
    private String createOutputSignature(double[] output) {
        // Create a simple hash-based signature
        return Arrays.toString(output);
    }
    
    private double calculateSignatureSimilarity(String signature1, String signature2) {
        // Simple string similarity (in real implementation, use proper similarity metrics)
        return signature1.equals(signature2) ? 1.0 : 0.0;
    }
    
    private void updateMonitoringData(String modelId, double[] input, double[] output, 
                                    Map<String, Object> metadata, MonitoringResult result) {
        ModelMonitoringData data = modelMonitoringData.get(modelId);
        if (data != null) {
            data.addPrediction(input, output, LocalDateTime.now());
        }
    }
    
    /**
     * Scheduled task to clean up old monitoring data
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupOldData() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        modelMonitoringData.values().removeIf(data -> 
            data.getLastUpdated().isBefore(cutoff));
    }
    
    // Inner classes for data structures
    public static class MonitoringResult {
        private boolean modelIntegrity;
        private double integrityScore;
        private List<String> issuesDetected;
        private LocalDateTime timestamp;
        
        public MonitoringResult() {
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters and Setters
        public boolean isModelIntegrity() { return modelIntegrity; }
        public void setModelIntegrity(boolean modelIntegrity) { this.modelIntegrity = modelIntegrity; }
        
        public double getIntegrityScore() { return integrityScore; }
        public void setIntegrityScore(double integrityScore) { this.integrityScore = integrityScore; }
        
        public List<String> getIssuesDetected() { return issuesDetected; }
        public void setIssuesDetected(List<String> issuesDetected) { this.issuesDetected = issuesDetected; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class ModelMonitoringData {
        private final List<PredictionRecord> predictions = new ArrayList<>();
        private LocalDateTime lastUpdated;
        
        public void addPrediction(double[] input, double[] output, LocalDateTime timestamp) {
            predictions.add(new PredictionRecord(input, output, timestamp));
            lastUpdated = timestamp;
            
            // Keep only recent predictions
            if (predictions.size() > 100) {
                predictions.remove(0);
            }
        }
        
        public List<PredictionRecord> getRecentPredictions(int count) {
            int startIndex = Math.max(0, predictions.size() - count);
            return predictions.subList(startIndex, predictions.size());
        }
        
        public int getPredictionCount() { return predictions.size(); }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
    }
    
    public static class PredictionRecord {
        private final double[] input;
        private final double[] output;
        private final LocalDateTime timestamp;
        
        public PredictionRecord(double[] input, double[] output, LocalDateTime timestamp) {
            this.input = input.clone();
            this.output = output.clone();
            this.timestamp = timestamp;
        }
        
        public double[] getInput() { return input; }
        public double[] getOutput() { return output; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    public static class ModelFingerprint {
        private String modelId;
        private String inputSignature;
        private String outputSignature;
        private LocalDateTime createdAt;
        
        // Getters and Setters
        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
        
        public String getInputSignature() { return inputSignature; }
        public void setInputSignature(String inputSignature) { this.inputSignature = inputSignature; }
        
        public String getOutputSignature() { return outputSignature; }
        public void setOutputSignature(String outputSignature) { this.outputSignature = outputSignature; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    public static class QueryPattern {
        private final List<Map<String, Object>> queries = new ArrayList<>();
        private final Map<String, Integer> featureAccessCount = new HashMap<>();
        
        public void addQuery(Map<String, Object> metadata) {
            queries.add(metadata);
            updateFeatureAccessCount(metadata);
            
            // Keep only recent queries
            if (queries.size() > 1000) {
                queries.remove(0);
            }
        }
        
        private void updateFeatureAccessCount(Map<String, Object> metadata) {
            // Count feature access patterns
            metadata.forEach((key, value) -> {
                if (key.startsWith("feature_")) {
                    featureAccessCount.merge(key, 1, Integer::sum);
                }
            });
        }
        
        public int getQueryCount() { return queries.size(); }
        
        public double calculateSystematicScore() {
            // Calculate how systematic the queries are
            if (queries.size() < 10) return 0.0;
            
            double systematicPatterns = 0.0;
            for (int i = 0; i < queries.size() - 1; i++) {
                if (isSystematicQuery(queries.get(i), queries.get(i + 1))) {
                    systematicPatterns++;
                }
            }
            
            return systematicPatterns / (queries.size() - 1);
        }
        
        public double calculateRateLimitScore() {
            // Calculate rate limiting violations
            if (queries.size() < 2) return 0.0;
            
            long recentQueries = queries.stream()
                    .filter(q -> q.containsKey("timestamp"))
                    .mapToLong(q -> {
                        Object timestamp = q.get("timestamp");
                        if (timestamp instanceof LocalDateTime) {
                            return ((LocalDateTime) timestamp).toEpochSecond(java.time.ZoneOffset.UTC);
                        }
                        return 0L;
                    })
                    .filter(t -> t > System.currentTimeMillis() / 1000 - 3600) // Last hour
                    .count();
            
            return Math.min(recentQueries / 100.0, 1.0); // Normalize to [0, 1]
        }
        
        private boolean isSystematicQuery(Map<String, Object> query1, Map<String, Object> query2) {
            // Check if queries follow a systematic pattern
            Set<String> keys1 = query1.keySet();
            Set<String> keys2 = query2.keySet();
            
            return keys1.equals(keys2) && keys1.size() > 3; // Systematic if same structure and complex
        }
    }
}