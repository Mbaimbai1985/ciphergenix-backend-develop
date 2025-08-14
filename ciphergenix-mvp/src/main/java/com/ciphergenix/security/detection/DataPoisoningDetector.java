package com.ciphergenix.security.detection;

import com.ciphergenix.dto.DatasetAnalysisRequest;
import com.ciphergenix.dto.DetectionResponse;
import com.ciphergenix.model.ThreatLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import smile.clustering.DBSCAN;
import smile.neighbor.LocalitySensitiveHash;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class DataPoisoningDetector {
    
    @Value("${ai.detection.poisoning.contamination-threshold}")
    private double contaminationThreshold;
    
    @Value("${ai.detection.poisoning.ensemble-voting-threshold}")
    private double ensembleVotingThreshold;
    
    private final IsolationForestDetector isolationForest;
    private final AutoencoderAnomalyDetector autoencoderDetector;
    private final InfluenceFunctionAnalyzer influenceAnalyzer;
    
    public DataPoisoningDetector() {
        this.isolationForest = new IsolationForestDetector();
        this.autoencoderDetector = new AutoencoderAnomalyDetector();
        this.influenceAnalyzer = new InfluenceFunctionAnalyzer();
    }
    
    public DetectionResponse detectPoisoning(DatasetAnalysisRequest request) {
        log.info("Starting data poisoning detection for dataset: {}", request.getDatasetId());
        
        List<double[]> dataSamples = request.getDataSamples();
        DatasetAnalysisRequest.BaselineStatistics baselineStats = request.getBaselineStats();
        
        // Statistical anomaly detection
        Map<Integer, Double> statisticalScores = performStatisticalAnalysis(dataSamples, baselineStats);
        
        // Distribution shift analysis
        Map<Integer, Double> distributionScores = analyzeDistributionShift(dataSamples, baselineStats);
        
        // Influence function computation
        Map<Integer, Double> influenceScores = computeInfluenceScores(dataSamples);
        
        // Ensemble scoring
        Map<Integer, Double> ensembleScores = computeEnsembleScores(
            statisticalScores, distributionScores, influenceScores
        );
        
        // Identify anomalous samples
        List<DetectionResponse.AnomalySample> anomalousSamples = identifyAnomalies(
            dataSamples, ensembleScores, request.getFeatureNames()
        );
        
        // Calculate overall threat score and level
        double threatScore = calculateThreatScore(ensembleScores);
        ThreatLevel threatLevel = determineThreatLevel(threatScore);
        
        return buildDetectionResponse(
            anomalousSamples, threatScore, threatLevel, ensembleScores.size()
        );
    }
    
    private Map<Integer, Double> performStatisticalAnalysis(
            List<double[]> samples, DatasetAnalysisRequest.BaselineStatistics baseline) {
        Map<Integer, Double> scores = new HashMap<>();
        
        // Calculate Mahalanobis distance for each sample
        if (baseline != null && baseline.getMeans() != null && baseline.getCovarianceMatrix() != null) {
            for (int i = 0; i < samples.size(); i++) {
                double distance = calculateMahalanobisDistance(
                    samples.get(i), baseline.getMeans(), baseline.getCovarianceMatrix()
                );
                scores.put(i, normalizeScore(distance));
            }
        } else {
            // Use Isolation Forest for unsupervised anomaly detection
            double[][] dataMatrix = samples.toArray(new double[0][]);
            scores = isolationForest.detectAnomalies(dataMatrix);
        }
        
        return scores;
    }
    
    private Map<Integer, Double> analyzeDistributionShift(
            List<double[]> samples, DatasetAnalysisRequest.BaselineStatistics baseline) {
        Map<Integer, Double> scores = new HashMap<>();
        
        if (baseline != null && baseline.getMeans() != null) {
            // Compute feature-wise distribution divergence
            int numFeatures = samples.get(0).length;
            
            for (int featureIdx = 0; featureIdx < numFeatures; featureIdx++) {
                final int idx = featureIdx;
                double[] featureValues = samples.stream()
                    .mapToDouble(sample -> sample[idx])
                    .toArray();
                
                // Calculate Kolmogorov-Smirnov statistic
                double ksStatistic = calculateKSStatistic(
                    featureValues, baseline.getMeans()[idx], 
                    baseline.getStandardDeviations()[idx]
                );
                
                // Update scores based on feature divergence
                for (int i = 0; i < samples.size(); i++) {
                    scores.merge(i, ksStatistic, Double::sum);
                }
            }
            
            // Normalize scores
            double maxScore = scores.values().stream().max(Double::compare).orElse(1.0);
            scores.replaceAll((k, v) -> v / maxScore);
        }
        
        return scores;
    }
    
    private Map<Integer, Double> computeInfluenceScores(List<double[]> samples) {
        // Simplified influence function approximation
        return influenceAnalyzer.computeInfluences(samples);
    }
    
    private Map<Integer, Double> computeEnsembleScores(
            Map<Integer, Double> statistical,
            Map<Integer, Double> distribution,
            Map<Integer, Double> influence) {
        
        Map<Integer, Double> ensembleScores = new HashMap<>();
        Set<Integer> allIndices = new HashSet<>();
        allIndices.addAll(statistical.keySet());
        allIndices.addAll(distribution.keySet());
        allIndices.addAll(influence.keySet());
        
        for (Integer idx : allIndices) {
            double statScore = statistical.getOrDefault(idx, 0.0);
            double distScore = distribution.getOrDefault(idx, 0.0);
            double infScore = influence.getOrDefault(idx, 0.0);
            
            // Weighted ensemble voting
            double ensembleScore = (0.4 * statScore + 0.3 * distScore + 0.3 * infScore);
            
            if (ensembleScore > ensembleVotingThreshold) {
                ensembleScores.put(idx, ensembleScore);
            }
        }
        
        return ensembleScores;
    }
    
    private List<DetectionResponse.AnomalySample> identifyAnomalies(
            List<double[]> samples, Map<Integer, Double> scores, List<String> featureNames) {
        
        return scores.entrySet().stream()
            .map(entry -> {
                int idx = entry.getKey();
                double score = entry.getValue();
                double[] sample = samples.get(idx);
                
                return DetectionResponse.AnomalySample.builder()
                    .sampleIndex(idx)
                    .features(sample)
                    .anomalyScore(score)
                    .anomalyType("DATA_POISONING")
                    .featureContributions(calculateFeatureContributions(sample, featureNames))
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private double calculateMahalanobisDistance(double[] sample, double[] means, double[][] covariance) {
        int n = sample.length;
        double[] diff = new double[n];
        
        for (int i = 0; i < n; i++) {
            diff[i] = sample[i] - means[i];
        }
        
        // Simplified Mahalanobis distance calculation
        double distance = 0.0;
        for (int i = 0; i < n; i++) {
            distance += diff[i] * diff[i] / (covariance[i][i] + 1e-10);
        }
        
        return Math.sqrt(distance);
    }
    
    private double calculateKSStatistic(double[] values, double mean, double std) {
        // Simplified K-S statistic calculation
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double value : values) {
            stats.addValue(value);
        }
        
        double observedMean = stats.getMean();
        double observedStd = stats.getStandardDeviation();
        
        return Math.abs(observedMean - mean) / (std + 1e-10) + 
               Math.abs(observedStd - std) / (std + 1e-10);
    }
    
    private double normalizeScore(double score) {
        // Sigmoid normalization to [0, 1]
        return 1.0 / (1.0 + Math.exp(-score));
    }
    
    private Map<String, Double> calculateFeatureContributions(double[] sample, List<String> featureNames) {
        Map<String, Double> contributions = new HashMap<>();
        
        if (featureNames != null && featureNames.size() == sample.length) {
            for (int i = 0; i < sample.length; i++) {
                contributions.put(featureNames.get(i), Math.abs(sample[i]));
            }
        } else {
            for (int i = 0; i < sample.length; i++) {
                contributions.put("feature_" + i, Math.abs(sample[i]));
            }
        }
        
        return contributions;
    }
    
    private double calculateThreatScore(Map<Integer, Double> anomalyScores) {
        if (anomalyScores.isEmpty()) {
            return 0.0;
        }
        
        double avgScore = anomalyScores.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double maxScore = anomalyScores.values().stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(0.0);
        
        // Weighted combination of average and max scores
        return 0.7 * avgScore + 0.3 * maxScore;
    }
    
    private ThreatLevel determineThreatLevel(double threatScore) {
        if (threatScore >= 0.8) {
            return ThreatLevel.CRITICAL;
        } else if (threatScore >= 0.6) {
            return ThreatLevel.HIGH;
        } else if (threatScore >= 0.4) {
            return ThreatLevel.MEDIUM;
        } else {
            return ThreatLevel.LOW;
        }
    }
    
    private DetectionResponse buildDetectionResponse(
            List<DetectionResponse.AnomalySample> anomalies,
            double threatScore,
            ThreatLevel threatLevel,
            int totalSamples) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("totalSamplesAnalyzed", totalSamples);
        details.put("anomalousCount", anomalies.size());
        details.put("contaminationRate", (double) anomalies.size() / totalSamples);
        details.put("detectionMethods", Arrays.asList("Statistical Analysis", "Distribution Shift", "Influence Function"));
        
        String recommendation = generateRecommendation(threatLevel, anomalies.size(), totalSamples);
        
        return DetectionResponse.builder()
            .detectionId(UUID.randomUUID().toString())
            .detectionType("DATA_POISONING")
            .threatScore(threatScore)
            .threatLevel(threatLevel)
            .anomalousSamples(anomalies)
            .detectionDetails(details)
            .timestamp(LocalDateTime.now())
            .recommendation(recommendation)
            .build();
    }
    
    private String generateRecommendation(ThreatLevel level, int anomalousCount, int totalSamples) {
        double contaminationRate = (double) anomalousCount / totalSamples;
        
        switch (level) {
            case CRITICAL:
                return String.format(
                    "CRITICAL: Significant data poisoning detected (%.1f%% contamination). " +
                    "Immediately quarantine the dataset and retrain the model with clean data. " +
                    "Investigate the source of contamination.",
                    contaminationRate * 100
                );
            case HIGH:
                return String.format(
                    "HIGH: Substantial poisoning detected (%.1f%% contamination). " +
                    "Remove identified anomalous samples and validate model performance. " +
                    "Consider retraining if performance degradation is observed.",
                    contaminationRate * 100
                );
            case MEDIUM:
                return String.format(
                    "MEDIUM: Moderate anomalies detected (%.1f%% contamination). " +
                    "Review anomalous samples and monitor model behavior closely. " +
                    "Consider data augmentation to balance the dataset.",
                    contaminationRate * 100
                );
            case LOW:
            default:
                return String.format(
                    "LOW: Minor anomalies detected (%.1f%% contamination). " +
                    "Continue monitoring. Anomalies may be natural outliers.",
                    contaminationRate * 100
                );
        }
    }
}