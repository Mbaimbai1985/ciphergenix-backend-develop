package com.ciphergenix.ml.detection;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.*;

@Component
public class AdversarialDetector {
    
    @Value("${ciphergenix.ml.models.adversarial.mahalanobis.confidence-threshold:0.95}")
    private double mahalanobisThreshold;
    
    @Value("${ciphergenix.ml.models.adversarial.reconstruction.threshold:0.1}")
    private double reconstructionThreshold;
    
    @Value("${ciphergenix.ml.models.ensemble.weights.statistical:0.3}")
    private double statisticalWeight;
    
    @Value("${ciphergenix.ml.models.ensemble.weights.ml-based:0.4}")
    private double mlBasedWeight;
    
    @Value("${ciphergenix.ml.models.ensemble.weights.reconstruction:0.3}")
    private double reconstructionWeight;
    
    /**
     * Main method to detect adversarial examples
     */
    public DetectionResult detectAdversarial(double[] inputData, Object model, double[] originalPrediction) {
        DetectionResult result = new DetectionResult();
        
        // 1. Gradient-based Detection
        double[] gradientScores = performGradientAnalysis(inputData, model, originalPrediction);
        
        // 2. Feature Space Analysis
        double[] featureScores = performFeatureSpaceAnalysis(inputData, model);
        
        // 3. Reconstruction-based Detection
        double[] reconstructionScores = performReconstructionAnalysis(inputData, model);
        
        // 4. Ensemble Decision
        double[] ensembleScores = computeEnsembleScores(gradientScores, featureScores, reconstructionScores);
        
        // 5. Final decision
        boolean isAdversarial = determineAdversarial(ensembleScores);
        double confidenceScore = calculateConfidenceScore(ensembleScores);
        
        result.setAdversarial(isAdversarial);
        result.setConfidenceScore(confidenceScore);
        result.setDetectionScores(ensembleScores);
        result.setDetectionMethod("Ensemble Adversarial Detection");
        
        return result;
    }
    
    /**
     * Gradient-based detection using FGSM, PGD, and C&W attack signatures
     */
    private double[] performGradientAnalysis(double[] inputData, Object model, double[] originalPrediction) {
        // Simulate gradient computation for different attack types
        double[] scores = new double[inputData.length];
        
        // FGSM-like gradient analysis
        double[] fgsmScores = analyzeFGSMGradient(inputData, model, originalPrediction);
        
        // PGD-like gradient analysis
        double[] pgdScores = analyzePGDGradient(inputData, model, originalPrediction);
        
        // C&W-like gradient analysis
        double[] cwScores = analyzeCWGradient(inputData, model, originalPrediction);
        
        // Combine gradient scores
        for (int i = 0; i < scores.length; i++) {
            scores[i] = (fgsmScores[i] + pgdScores[i] + cwScores[i]) / 3.0;
        }
        
        return normalizeScores(scores);
    }
    
    /**
     * Feature space analysis using Mahalanobis distance
     */
    private double[] performFeatureSpaceAnalysis(double[] inputData, Object model) {
        // Extract features from the model (simplified)
        double[] features = extractFeatures(inputData, model);
        
        // Calculate Mahalanobis distance from baseline distribution
        double[] baselineMean = getBaselineFeatureMean();
        double[][] baselineCovariance = getBaselineFeatureCovariance();
        
        double mahalanobisDistance = calculateMahalanobisDistance(features, baselineMean, baselineCovariance);
        
        // Convert to anomaly score
        double[] scores = new double[inputData.length];
        double anomalyScore = Math.exp(-mahalanobisDistance / 2.0);
        
        Arrays.fill(scores, anomalyScore);
        return scores;
    }
    
    /**
     * Reconstruction-based detection using autoencoder
     */
    private double[] performReconstructionAnalysis(double[] inputData, Object model) {
        // Simulate autoencoder reconstruction
        double[] reconstructed = reconstructInput(inputData, model);
        
        // Calculate reconstruction error
        double[] scores = new double[inputData.length];
        for (int i = 0; i < inputData.length; i++) {
            double error = Math.abs(inputData[i] - reconstructed[i]);
            scores[i] = error > reconstructionThreshold ? 1.0 : error / reconstructionThreshold;
        }
        
        return normalizeScores(scores);
    }
    
    /**
     * Compute ensemble scores using weighted combination
     */
    private double[] computeEnsembleScores(double[] gradientScores, double[] featureScores, double[] reconstructionScores) {
        int nFeatures = gradientScores.length;
        double[] ensembleScores = new double[nFeatures];
        
        for (int i = 0; i < nFeatures; i++) {
            ensembleScores[i] = statisticalWeight * gradientScores[i] + 
                               mlBasedWeight * featureScores[i] + 
                               reconstructionWeight * reconstructionScores[i];
        }
        
        return ensembleScores;
    }
    
    /**
     * Determine if input is adversarial based on ensemble scores
     */
    private boolean determineAdversarial(double[] ensembleScores) {
        double averageScore = Arrays.stream(ensembleScores).average().orElse(0.0);
        return averageScore > 0.7; // Threshold for adversarial detection
    }
    
    /**
     * Calculate confidence score for the detection
     */
    private double calculateConfidenceScore(double[] ensembleScores) {
        double maxScore = Arrays.stream(ensembleScores).max().orElse(0.0);
        double averageScore = Arrays.stream(ensembleScores).average().orElse(0.0);
        
        // Combine max and average scores for confidence
        return (maxScore * 0.6) + (averageScore * 0.4);
    }
    
    // Helper methods for gradient analysis
    private double[] analyzeFGSMGradient(double[] inputData, Object model, double[] originalPrediction) {
        // Simulate FGSM gradient computation
        double[] scores = new double[inputData.length];
        double epsilon = 0.1;
        
        for (int i = 0; i < inputData.length; i++) {
            // Simulate gradient sign
            double gradient = Math.signum(inputData[i] - epsilon);
            scores[i] = Math.abs(gradient);
        }
        
        return scores;
    }
    
    private double[] analyzePGDGradient(double[] inputData, Object model, double[] originalPrediction) {
        // Simulate PGD gradient computation
        double[] scores = new double[inputData.length];
        double epsilon = 0.1;
        int iterations = 10;
        
        for (int i = 0; i < inputData.length; i++) {
            double cumulativeGradient = 0.0;
            for (int iter = 0; iter < iterations; iter++) {
                cumulativeGradient += Math.signum(inputData[i] - epsilon * iter / iterations);
            }
            scores[i] = Math.abs(cumulativeGradient) / iterations;
        }
        
        return scores;
    }
    
    private double[] analyzeCWGradient(double[] inputData, Object model, double[] originalPrediction) {
        // Simulate C&W gradient computation
        double[] scores = new double[inputData.length];
        
        for (int i = 0; i < inputData.length; i++) {
            // Simulate C&W optimization objective
            double perturbation = Math.abs(inputData[i]);
            scores[i] = Math.exp(-perturbation);
        }
        
        return scores;
    }
    
    // Helper methods for feature analysis
    private double[] extractFeatures(double[] inputData, Object model) {
        // Simplified feature extraction (in real implementation, this would use the actual model)
        return Arrays.copyOf(inputData, inputData.length);
    }
    
    private double[] getBaselineFeatureMean() {
        // Return baseline feature mean (in real implementation, this would be learned from training data)
        return new double[10]; // Assuming 10 features
    }
    
    private double[][] getBaselineFeatureCovariance() {
        // Return baseline feature covariance matrix (in real implementation, this would be learned from training data)
        int nFeatures = 10;
        double[][] covariance = new double[nFeatures][nFeatures];
        for (int i = 0; i < nFeatures; i++) {
            covariance[i][i] = 1.0; // Identity matrix as default
        }
        return covariance;
    }
    
    private double calculateMahalanobisDistance(double[] features, double[] mean, double[][] covariance) {
        // Calculate Mahalanobis distance
        RealMatrix covMatrix = new Array2DRowRealMatrix(covariance);
        LUDecomposition lu = new LUDecomposition(covMatrix);
        
        if (!lu.getSolver().isNonSingular()) {
            return Double.MAX_VALUE; // Return large distance if covariance is singular
        }
        
        RealMatrix invCov = lu.getSolver().getInverse();
        RealVector diff = new ArrayRealVector(features).subtract(new ArrayRealVector(mean));
        
        RealVector result = invCov.preMultiply(diff);
        return Math.sqrt(diff.dotProduct(result));
    }
    
    // Helper methods for reconstruction
    private double[] reconstructInput(double[] inputData, Object model) {
        // Simulate autoencoder reconstruction (in real implementation, this would use the actual autoencoder)
        double[] reconstructed = new double[inputData.length];
        for (int i = 0; i < inputData.length; i++) {
            // Add some noise to simulate reconstruction
            reconstructed[i] = inputData[i] + (Math.random() - 0.5) * 0.1;
        }
        return reconstructed;
    }
    
    private double[] normalizeScores(double[] scores) {
        double min = Arrays.stream(scores).min().orElse(0.0);
        double max = Arrays.stream(scores).max().orElse(1.0);
        
        if (max == min) return scores;
        
        return Arrays.stream(scores)
                .map(score -> (score - min) / (max - min))
                .toArray();
    }
    
    /**
     * Result class for adversarial detection
     */
    public static class DetectionResult {
        private boolean isAdversarial;
        private double confidenceScore;
        private double[] detectionScores;
        private String detectionMethod;
        
        // Getters and Setters
        public boolean isAdversarial() { return isAdversarial; }
        public void setAdversarial(boolean adversarial) { isAdversarial = adversarial; }
        
        public double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
        
        public double[] getDetectionScores() { return detectionScores; }
        public void setDetectionScores(double[] detectionScores) { this.detectionScores = detectionScores; }
        
        public String getDetectionMethod() { return detectionMethod; }
        public void setDetectionMethod(String detectionMethod) { this.detectionMethod = detectionMethod; }
    }
}