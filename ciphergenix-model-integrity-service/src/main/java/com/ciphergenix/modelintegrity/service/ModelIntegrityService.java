package com.ciphergenix.modelintegrity.service;

import com.ciphergenix.modelintegrity.model.ModelFingerprint;
import com.ciphergenix.modelintegrity.model.ModelPerformanceMetrics;
import com.ciphergenix.modelintegrity.repository.ModelFingerprintRepository;
import com.ciphergenix.modelintegrity.repository.ModelPerformanceMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class ModelIntegrityService {

    private static final Logger logger = LoggerFactory.getLogger(ModelIntegrityService.class);

    @Autowired
    private ModelFingerprintRepository fingerprintRepository;

    @Autowired
    private ModelPerformanceMetricsRepository metricsRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${ciphergenix.model-integrity.fingerprint-verification-interval:3600}")
    private long fingerprintVerificationInterval;

    @Value("${ciphergenix.model-integrity.performance-alert-threshold:0.85}")
    private double performanceAlertThreshold;

    @Value("${ciphergenix.model-integrity.drift-threshold:0.1}")
    private double driftThreshold;

    // Cache for model monitoring sessions
    private final Map<String, ModelMonitoringSession> activeMonitoringSessions = new ConcurrentHashMap<>();

    /**
     * Create or update model fingerprint
     */
    public ModelFingerprint createModelFingerprint(String modelId, String modelName, String modelVersion, 
                                                 Map<String, Double> parameters, Map<String, String> weightsHash) {
        logger.info("Creating fingerprint for model: {} version: {}", modelId, modelVersion);

        try {
            // Generate fingerprint hash
            String fingerprintHash = generateFingerprintHash(modelId, parameters, weightsHash);
            
            // Check if fingerprint already exists
            Optional<ModelFingerprint> existingFingerprint = fingerprintRepository.findByFingerprintHash(fingerprintHash);
            if (existingFingerprint.isPresent()) {
                logger.warn("Fingerprint already exists for model: {}", modelId);
                return existingFingerprint.get();
            }

            // Deactivate previous fingerprints for this model
            List<ModelFingerprint> previousFingerprints = fingerprintRepository.findByModelIdOrderByCreatedAtDesc(modelId);
            previousFingerprints.forEach(fp -> fp.setIsActive(false));
            fingerprintRepository.saveAll(previousFingerprints);

            // Create new fingerprint
            ModelFingerprint fingerprint = new ModelFingerprint(modelId, modelName, modelVersion, fingerprintHash);
            fingerprint.setParameters(parameters);
            fingerprint.setWeightsHash(weightsHash);
            fingerprint.setArchitectureHash(generateArchitectureHash(parameters));
            fingerprint.setIntegrityScore(1.0); // Perfect integrity initially

            ModelFingerprint savedFingerprint = fingerprintRepository.save(fingerprint);
            
            // Send notification
            sendFingerprintCreatedNotification(savedFingerprint);
            
            logger.info("Fingerprint created successfully for model: {} with ID: {}", modelId, savedFingerprint.getId());
            return savedFingerprint;

        } catch (Exception e) {
            logger.error("Error creating fingerprint for model: {}", modelId, e);
            throw new RuntimeException("Failed to create model fingerprint", e);
        }
    }

    /**
     * Verify model integrity against fingerprint
     */
    public double verifyModelIntegrity(String modelId, Map<String, Double> currentParameters, 
                                     Map<String, String> currentWeightsHash) {
        logger.info("Verifying integrity for model: {}", modelId);

        try {
            Optional<ModelFingerprint> fingerprintOpt = fingerprintRepository.findByModelIdAndIsActiveTrue(modelId);
            if (fingerprintOpt.isEmpty()) {
                logger.warn("No active fingerprint found for model: {}", modelId);
                return 0.0;
            }

            ModelFingerprint fingerprint = fingerprintOpt.get();
            
            // Calculate integrity score
            double parameterSimilarity = calculateParameterSimilarity(fingerprint.getParameters(), currentParameters);
            double weightsSimilarity = calculateWeightsSimilarity(fingerprint.getWeightsHash(), currentWeightsHash);
            
            double integrityScore = (parameterSimilarity + weightsSimilarity) / 2.0;
            
            // Update fingerprint with latest verification
            fingerprint.setIntegrityScore(integrityScore);
            fingerprint.setLastVerified(LocalDateTime.now());
            fingerprintRepository.save(fingerprint);

            // Send alert if integrity is compromised
            if (integrityScore < 0.9) {
                sendIntegrityAlertNotification(modelId, integrityScore);
            }

            logger.info("Integrity verification completed for model: {} with score: {}", modelId, integrityScore);
            return integrityScore;

        } catch (Exception e) {
            logger.error("Error verifying integrity for model: {}", modelId, e);
            throw new RuntimeException("Failed to verify model integrity", e);
        }
    }

    /**
     * Start monitoring model performance
     */
    @Async
    public CompletableFuture<Void> startModelMonitoring(String modelId) {
        logger.info("Starting performance monitoring for model: {}", modelId);

        try {
            ModelMonitoringSession session = new ModelMonitoringSession(modelId);
            activeMonitoringSessions.put(modelId, session);

            // Start monitoring loop
            while (session.isActive()) {
                // Collect performance metrics
                ModelPerformanceMetrics metrics = collectPerformanceMetrics(modelId);
                
                if (metrics != null) {
                    // Analyze metrics for anomalies
                    analyzePerformanceMetrics(metrics);
                    
                    // Save metrics
                    metricsRepository.save(metrics);
                    
                    // Send metrics to Kafka
                    sendPerformanceMetricsNotification(metrics);
                }

                // Wait for next measurement interval
                Thread.sleep(60000); // 1 minute interval
            }

            logger.info("Monitoring stopped for model: {}", modelId);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("Error in model monitoring for: {}", modelId, e);
            activeMonitoringSessions.remove(modelId);
            throw new RuntimeException("Model monitoring failed", e);
        }
    }

    /**
     * Stop monitoring for a model
     */
    public void stopModelMonitoring(String modelId) {
        logger.info("Stopping monitoring for model: {}", modelId);
        ModelMonitoringSession session = activeMonitoringSessions.get(modelId);
        if (session != null) {
            session.stop();
            activeMonitoringSessions.remove(modelId);
        }
    }

    /**
     * Detect potential model theft based on query patterns
     */
    public Map<String, Object> analyzeModelTheftPatterns(String modelId, Map<String, Object> queryPatterns) {
        logger.info("Analyzing potential theft patterns for model: {}", modelId);

        try {
            Map<String, Object> analysis = new HashMap<>();
            
            // Extract query pattern metrics
            List<Map<String, Object>> queries = (List<Map<String, Object>>) queryPatterns.get("queries");
            int queryCount = queries.size();
            
            // Analyze query frequency
            double queryFrequency = calculateQueryFrequency(queries);
            
            // Analyze query diversity
            double queryDiversity = calculateQueryDiversity(queries);
            
            // Analyze response correlation
            double responseCorrelation = calculateResponseCorrelation(queries);
            
            // Calculate theft probability
            double theftProbability = calculateTheftProbability(queryFrequency, queryDiversity, responseCorrelation);
            
            // Prepare analysis results
            analysis.put("modelId", modelId);
            analysis.put("queryCount", queryCount);
            analysis.put("queryFrequency", queryFrequency);
            analysis.put("queryDiversity", queryDiversity);
            analysis.put("responseCorrelation", responseCorrelation);
            analysis.put("theftProbability", theftProbability);
            analysis.put("riskLevel", determineRiskLevel(theftProbability));
            analysis.put("analyzedAt", LocalDateTime.now());

            // Send alert if high theft probability
            if (theftProbability > 0.7) {
                sendTheftAlertNotification(modelId, analysis);
            }

            logger.info("Theft analysis completed for model: {} with probability: {}", modelId, theftProbability);
            return analysis;

        } catch (Exception e) {
            logger.error("Error analyzing theft patterns for model: {}", modelId, e);
            throw new RuntimeException("Failed to analyze model theft patterns", e);
        }
    }

    /**
     * Get model performance trends
     */
    public Map<String, Object> getPerformanceTrends(String modelId, int days) {
        logger.info("Getting performance trends for model: {} over {} days", modelId, days);

        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            List<ModelPerformanceMetrics> metrics = metricsRepository.findByModelIdAndDateRange(
                modelId, since, LocalDateTime.now());

            Map<String, Object> trends = new HashMap<>();
            
            if (!metrics.isEmpty()) {
                // Calculate trends
                List<Double> accuracyTrend = metrics.stream()
                    .map(ModelPerformanceMetrics::getAccuracy)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                
                List<Double> latencyTrend = metrics.stream()
                    .map(m -> m.getInferenceLatencyMs() != null ? m.getInferenceLatencyMs().doubleValue() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                trends.put("modelId", modelId);
                trends.put("periodDays", days);
                trends.put("measurementCount", metrics.size());
                trends.put("accuracyTrend", accuracyTrend);
                trends.put("latencyTrend", latencyTrend);
                trends.put("currentAccuracy", accuracyTrend.isEmpty() ? null : accuracyTrend.get(0));
                trends.put("averageAccuracy", accuracyTrend.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
                trends.put("accuracyChange", calculateTrendChange(accuracyTrend));
                trends.put("latencyChange", calculateTrendChange(latencyTrend));
            }

            return trends;

        } catch (Exception e) {
            logger.error("Error getting performance trends for model: {}", modelId, e);
            throw new RuntimeException("Failed to get performance trends", e);
        }
    }

    /**
     * Generate fingerprint hash from model parameters
     */
    private String generateFingerprintHash(String modelId, Map<String, Double> parameters, 
                                         Map<String, String> weightsHash) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        
        StringBuilder dataToHash = new StringBuilder();
        dataToHash.append(modelId);
        
        // Add parameters in sorted order for consistency
        parameters.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> dataToHash.append(entry.getKey()).append(":").append(entry.getValue()).append(";"));
        
        // Add weights hash in sorted order
        weightsHash.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> dataToHash.append(entry.getKey()).append(":").append(entry.getValue()).append(";"));
        
        byte[] hashBytes = digest.digest(dataToHash.toString().getBytes());
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    /**
     * Generate architecture hash
     */
    private String generateArchitectureHash(Map<String, Double> parameters) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        
        // Use only structural parameters for architecture hash
        StringBuilder archData = new StringBuilder();
        parameters.entrySet().stream()
            .filter(entry -> entry.getKey().contains("layer") || entry.getKey().contains("size") || 
                           entry.getKey().contains("dimension"))
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> archData.append(entry.getKey()).append(":").append(entry.getValue()).append(";"));
        
        byte[] hashBytes = digest.digest(archData.toString().getBytes());
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    /**
     * Calculate parameter similarity
     */
    private double calculateParameterSimilarity(Map<String, Double> baseline, Map<String, Double> current) {
        if (baseline == null || current == null || baseline.isEmpty()) {
            return 0.0;
        }

        double totalDifference = 0.0;
        int comparedParams = 0;

        for (Map.Entry<String, Double> entry : baseline.entrySet()) {
            String paramName = entry.getKey();
            Double baselineValue = entry.getValue();
            Double currentValue = current.get(paramName);

            if (currentValue != null && baselineValue != null) {
                double difference = Math.abs(baselineValue - currentValue) / Math.max(Math.abs(baselineValue), 1e-8);
                totalDifference += difference;
                comparedParams++;
            }
        }

        if (comparedParams == 0) {
            return 0.0;
        }

        double averageDifference = totalDifference / comparedParams;
        return Math.max(0.0, 1.0 - averageDifference);
    }

    /**
     * Calculate weights similarity
     */
    private double calculateWeightsSimilarity(Map<String, String> baselineHashes, Map<String, String> currentHashes) {
        if (baselineHashes == null || currentHashes == null || baselineHashes.isEmpty()) {
            return 0.0;
        }

        int matchingHashes = 0;
        int totalHashes = baselineHashes.size();

        for (Map.Entry<String, String> entry : baselineHashes.entrySet()) {
            String layerName = entry.getKey();
            String baselineHash = entry.getValue();
            String currentHash = currentHashes.get(layerName);

            if (baselineHash != null && baselineHash.equals(currentHash)) {
                matchingHashes++;
            }
        }

        return totalHashes > 0 ? (double) matchingHashes / totalHashes : 0.0;
    }

    /**
     * Collect performance metrics for a model
     */
    private ModelPerformanceMetrics collectPerformanceMetrics(String modelId) {
        try {
            // Simulate metric collection (in real implementation, this would connect to model serving infrastructure)
            ModelPerformanceMetrics metrics = new ModelPerformanceMetrics();
            metrics.setModelId(modelId);
            
            // Simulate realistic metrics with some variance
            Random random = new Random();
            metrics.setAccuracy(0.85 + random.nextGaussian() * 0.05);
            metrics.setPrecision(0.87 + random.nextGaussian() * 0.03);
            metrics.setRecall(0.83 + random.nextGaussian() * 0.04);
            metrics.setF1Score(0.85 + random.nextGaussian() * 0.03);
            metrics.setConfidenceScore(0.92 + random.nextGaussian() * 0.02);
            metrics.setInferenceLatencyMs(100L + (long)(random.nextGaussian() * 20));
            metrics.setThroughputRps(50.0 + random.nextGaussian() * 10);
            metrics.setMemoryUsageMb(512L + (long)(random.nextGaussian() * 100));
            metrics.setCpuUsagePercent(45.0 + random.nextGaussian() * 15);
            
            // Calculate drift scores
            metrics.setDataDriftScore(Math.abs(random.nextGaussian() * 0.1));
            metrics.setModelDriftScore(Math.abs(random.nextGaussian() * 0.05));
            
            return metrics;

        } catch (Exception e) {
            logger.error("Error collecting metrics for model: {}", modelId, e);
            return null;
        }
    }

    /**
     * Analyze performance metrics for anomalies
     */
    private void analyzePerformanceMetrics(ModelPerformanceMetrics metrics) {
        // Check for performance degradation
        if (metrics.getAccuracy() != null && metrics.getAccuracy() < performanceAlertThreshold) {
            metrics.setStatus(ModelPerformanceMetrics.MetricStatus.CRITICAL);
            metrics.setAlertTriggered(true);
            metrics.setNotes("Accuracy below threshold: " + performanceAlertThreshold);
        }
        
        // Check for drift
        if ((metrics.getDataDriftScore() != null && metrics.getDataDriftScore() > driftThreshold) ||
            (metrics.getModelDriftScore() != null && metrics.getModelDriftScore() > driftThreshold)) {
            metrics.setStatus(ModelPerformanceMetrics.MetricStatus.WARNING);
            metrics.setNotes("Data or model drift detected");
        }
        
        // Calculate anomaly score based on multiple factors
        double anomalyScore = calculateAnomalyScore(metrics);
        metrics.setAnomalyScore(anomalyScore);
        
        if (anomalyScore > 0.7) {
            metrics.setStatus(ModelPerformanceMetrics.MetricStatus.CRITICAL);
            metrics.setAlertTriggered(true);
        }
    }

    /**
     * Calculate anomaly score for metrics
     */
    private double calculateAnomalyScore(ModelPerformanceMetrics metrics) {
        double score = 0.0;
        
        // Factor in accuracy deviation
        if (metrics.getAccuracy() != null) {
            double accuracyDeviation = Math.abs(0.9 - metrics.getAccuracy()) / 0.9;
            score += accuracyDeviation * 0.4;
        }
        
        // Factor in latency anomalies
        if (metrics.getInferenceLatencyMs() != null) {
            double latencyDeviation = Math.max(0, (metrics.getInferenceLatencyMs() - 150.0) / 150.0);
            score += latencyDeviation * 0.2;
        }
        
        // Factor in drift scores
        if (metrics.getDataDriftScore() != null) {
            score += metrics.getDataDriftScore() * 0.3;
        }
        
        if (metrics.getModelDriftScore() != null) {
            score += metrics.getModelDriftScore() * 0.1;
        }
        
        return Math.min(1.0, score);
    }

    // Helper methods for theft detection
    private double calculateQueryFrequency(List<Map<String, Object>> queries) {
        // Implement query frequency analysis
        return queries.size() / 3600.0; // queries per second
    }

    private double calculateQueryDiversity(List<Map<String, Object>> queries) {
        // Implement query diversity analysis
        Set<String> uniqueQueries = queries.stream()
            .map(q -> q.toString())
            .collect(Collectors.toSet());
        return (double) uniqueQueries.size() / queries.size();
    }

    private double calculateResponseCorrelation(List<Map<String, Object>> queries) {
        // Implement response correlation analysis
        return Math.random(); // Simplified for demo
    }

    private double calculateTheftProbability(double frequency, double diversity, double correlation) {
        // Weighted combination of factors
        return (frequency * 0.4 + (1.0 - diversity) * 0.3 + correlation * 0.3);
    }

    private String determineRiskLevel(double theftProbability) {
        if (theftProbability > 0.8) return "CRITICAL";
        if (theftProbability > 0.6) return "HIGH";
        if (theftProbability > 0.4) return "MEDIUM";
        return "LOW";
    }

    private double calculateTrendChange(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        double first = values.get(values.size() - 1);
        double last = values.get(0);
        
        return ((last - first) / first) * 100.0; // Percentage change
    }

    // Notification methods
    private void sendFingerprintCreatedNotification(ModelFingerprint fingerprint) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("event", "FINGERPRINT_CREATED");
        notification.put("modelId", fingerprint.getModelId());
        notification.put("fingerprintId", fingerprint.getId());
        notification.put("timestamp", LocalDateTime.now());
        
        kafkaTemplate.send("ciphergenix-model-integrity-events", notification);
    }

    private void sendIntegrityAlertNotification(String modelId, double integrityScore) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("event", "INTEGRITY_ALERT");
        alert.put("modelId", modelId);
        alert.put("integrityScore", integrityScore);
        alert.put("severity", integrityScore < 0.5 ? "CRITICAL" : "WARNING");
        alert.put("timestamp", LocalDateTime.now());
        
        kafkaTemplate.send("ciphergenix-threat-alerts", alert);
    }

    private void sendPerformanceMetricsNotification(ModelPerformanceMetrics metrics) {
        kafkaTemplate.send("ciphergenix-performance-metrics", metrics);
    }

    private void sendTheftAlertNotification(String modelId, Map<String, Object> analysis) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("event", "MODEL_THEFT_ALERT");
        alert.put("modelId", modelId);
        alert.put("analysis", analysis);
        alert.put("timestamp", LocalDateTime.now());
        
        kafkaTemplate.send("ciphergenix-threat-alerts", alert);
    }

    /**
     * Inner class for managing monitoring sessions
     */
    private static class ModelMonitoringSession {
        private final String modelId;
        private volatile boolean active = true;
        private final LocalDateTime startTime;

        public ModelMonitoringSession(String modelId) {
            this.modelId = modelId;
            this.startTime = LocalDateTime.now();
        }

        public boolean isActive() {
            return active;
        }

        public void stop() {
            this.active = false;
        }

        public String getModelId() {
            return modelId;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }
    }
}