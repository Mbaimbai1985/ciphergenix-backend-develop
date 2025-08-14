package com.ciphergenix.ml.detection;

import com.ciphergenix.domain.ThreatDetection;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataPoisoningDetector {
    
    @Value("${ciphergenix.ml.models.data-poisoning.contamination:0.1}")
    private double contamination;
    
    @Value("${ciphergenix.ml.models.data-poisoning.isolation-forest.n-estimators:100}")
    private int nEstimators;
    
    @Value("${ciphergenix.ml.models.data-poisoning.isolation-forest.max-samples:256}")
    private int maxSamples;
    
    private final KolmogorovSmirnovTest ksTest;
    private final Map<String, double[]> baselineDistributions;
    
    public DataPoisoningDetector() {
        this.ksTest = new KolmogorovSmirnovTest();
        this.baselineDistributions = new HashMap<>();
    }
    
    /**
     * Main method to detect data poisoning in training datasets
     */
    public DetectionResult detectPoisoning(double[][] dataset, Map<String, double[]> baselineStats) {
        DetectionResult result = new DetectionResult();
        
        // 1. Statistical Anomaly Detection
        double[] statisticalScores = performStatisticalAnomalyDetection(dataset);
        
        // 2. Feature Distribution Analysis
        double[] distributionScores = performFeatureDistributionAnalysis(dataset, baselineStats);
        
        // 3. Isolation Forest Detection
        double[] isolationScores = performIsolationForestDetection(dataset);
        
        // 4. Ensemble Scoring
        double[] ensembleScores = computeEnsembleScores(statisticalScores, distributionScores, isolationScores);
        
        // 5. Identify anomalous samples
        List<Integer> anomalousIndices = identifyAnomalousSamples(ensembleScores);
        
        result.setAnomalousSamples(anomalousIndices);
        result.setThreatScore(calculateThreatScore(ensembleScores, anomalousIndices.size(), dataset.length));
        result.setConfidenceScores(ensembleScores);
        result.setDetectionMethod("Ensemble Data Poisoning Detection");
        
        return result;
    }
    
    /**
     * Statistical anomaly detection using multivariate analysis
     */
    private double[] performStatisticalAnomalyDetection(double[][] dataset) {
        int nSamples = dataset.length;
        int nFeatures = dataset[0].length;
        double[] scores = new double[nSamples];
        
        // Calculate Mahalanobis distance for each sample
        double[] mean = calculateMean(dataset);
        double[][] covariance = calculateCovariance(dataset, mean);
        
        for (int i = 0; i < nSamples; i++) {
            scores[i] = calculateMahalanobisDistance(dataset[i], mean, covariance);
        }
        
        // Normalize scores to [0, 1] range
        return normalizeScores(scores);
    }
    
    /**
     * Feature distribution analysis using Kolmogorov-Smirnov tests
     */
    private double[] performFeatureDistributionAnalysis(double[][] dataset, Map<String, double[]> baselineStats) {
        int nSamples = dataset.length;
        double[] scores = new double[nSamples];
        
        for (int i = 0; i < nSamples; i++) {
            double sampleScore = 0.0;
            int featureCount = 0;
            
            for (int j = 0; j < dataset[i].length; j++) {
                String featureKey = "feature_" + j;
                if (baselineStats.containsKey(featureKey)) {
                    double[] baseline = baselineStats.get(featureKey);
                    double ksStatistic = ksTest.kolmogorovSmirnovStatistic(baseline, new double[]{dataset[i][j]});
                    sampleScore += ksStatistic;
                    featureCount++;
                }
            }
            
            scores[i] = featureCount > 0 ? sampleScore / featureCount : 0.0;
        }
        
        return normalizeScores(scores);
    }
    
    /**
     * Isolation Forest implementation for anomaly detection
     */
    private double[] performIsolationForestDetection(double[][] dataset) {
        int nSamples = dataset.length;
        double[] scores = new double[nSamples];
        
        // Simplified Isolation Forest implementation
        for (int i = 0; i < nSamples; i++) {
            scores[i] = calculateIsolationScore(dataset[i], dataset);
        }
        
        return normalizeScores(scores);
    }
    
    /**
     * Compute ensemble scores using weighted combination
     */
    private double[] computeEnsembleScores(double[] statisticalScores, double[] distributionScores, double[] isolationScores) {
        int nSamples = statisticalScores.length;
        double[] ensembleScores = new double[nSamples];
        
        // Weights for different detection methods
        double w1 = 0.3; // Statistical
        double w2 = 0.3; // Distribution
        double w3 = 0.4; // Isolation Forest
        
        for (int i = 0; i < nSamples; i++) {
            ensembleScores[i] = w1 * statisticalScores[i] + 
                               w2 * distributionScores[i] + 
                               w3 * isolationScores[i];
        }
        
        return ensembleScores;
    }
    
    /**
     * Identify anomalous samples based on threshold
     */
    private List<Integer> identifyAnomalousSamples(double[] scores) {
        List<Integer> anomalousIndices = new ArrayList<>();
        double threshold = calculateThreshold(scores);
        
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > threshold) {
                anomalousIndices.add(i);
            }
        }
        
        return anomalousIndices;
    }
    
    /**
     * Calculate threat score based on anomaly ratio and severity
     */
    private double calculateThreatScore(double[] scores, int anomalousCount, int totalCount) {
        double anomalyRatio = (double) anomalousCount / totalCount;
        double averageAnomalyScore = Arrays.stream(scores)
                .filter(score -> score > calculateThreshold(scores))
                .average()
                .orElse(0.0);
        
        return (anomalyRatio * 0.6) + (averageAnomalyScore * 0.4);
    }
    
    // Helper methods
    private double[] calculateMean(double[][] dataset) {
        int nFeatures = dataset[0].length;
        double[] mean = new double[nFeatures];
        
        for (int j = 0; j < nFeatures; j++) {
            double sum = 0.0;
            for (double[] sample : dataset) {
                sum += sample[j];
            }
            mean[j] = sum / dataset.length;
        }
        
        return mean;
    }
    
    private double[][] calculateCovariance(double[][] dataset, double[] mean) {
        int nFeatures = dataset[0].length;
        double[][] covariance = new double[nFeatures][nFeatures];
        
        for (int i = 0; i < nFeatures; i++) {
            for (int j = 0; j < nFeatures; j++) {
                double sum = 0.0;
                for (double[] sample : dataset) {
                    sum += (sample[i] - mean[i]) * (sample[j] - mean[j]);
                }
                covariance[i][j] = sum / (dataset.length - 1);
            }
        }
        
        return covariance;
    }
    
    private double calculateMahalanobisDistance(double[] sample, double[] mean, double[][] covariance) {
        // Simplified Mahalanobis distance calculation
        double distance = 0.0;
        for (int i = 0; i < sample.length; i++) {
            double diff = sample[i] - mean[i];
            distance += diff * diff;
        }
        return Math.sqrt(distance);
    }
    
    private double calculateIsolationScore(double[] sample, double[][] dataset) {
        // Simplified isolation score calculation
        double minDistance = Double.MAX_VALUE;
        
        for (double[] otherSample : dataset) {
            if (!Arrays.equals(sample, otherSample)) {
                double distance = calculateEuclideanDistance(sample, otherSample);
                minDistance = Math.min(minDistance, distance);
            }
        }
        
        return minDistance;
    }
    
    private double calculateEuclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
    
    private double[] normalizeScores(double[] scores) {
        double min = Arrays.stream(scores).min().orElse(0.0);
        double max = Arrays.stream(scores).max().orElse(1.0);
        
        if (max == min) return scores;
        
        return Arrays.stream(scores)
                .map(score -> (score - min) / (max - min))
                .toArray();
    }
    
    private double calculateThreshold(double[] scores) {
        // Use 95th percentile as threshold
        double[] sortedScores = Arrays.copyOf(scores, scores.length);
        Arrays.sort(sortedScores);
        int index = (int) Math.ceil(0.95 * sortedScores.length) - 1;
        return sortedScores[Math.max(0, index)];
    }
    
    /**
     * Result class for data poisoning detection
     */
    public static class DetectionResult {
        private List<Integer> anomalousSamples;
        private double threatScore;
        private double[] confidenceScores;
        private String detectionMethod;
        
        // Getters and Setters
        public List<Integer> getAnomalousSamples() { return anomalousSamples; }
        public void setAnomalousSamples(List<Integer> anomalousSamples) { this.anomalousSamples = anomalousSamples; }
        
        public double getThreatScore() { return threatScore; }
        public void setThreatScore(double threatScore) { this.threatScore = threatScore; }
        
        public double[] getConfidenceScores() { return confidenceScores; }
        public void setConfidenceScores(double[] confidenceScores) { this.confidenceScores = confidenceScores; }
        
        public String getDetectionMethod() { return detectionMethod; }
        public void setDetectionMethod(String detectionMethod) { this.detectionMethod = detectionMethod; }
    }
}