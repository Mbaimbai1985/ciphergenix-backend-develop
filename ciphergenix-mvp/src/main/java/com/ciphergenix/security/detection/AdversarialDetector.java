package com.ciphergenix.security.detection;

import com.ciphergenix.dto.AdversarialDetectionRequest;
import com.ciphergenix.dto.DetectionResponse;
import com.ciphergenix.model.ThreatLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class AdversarialDetector {
    
    @Value("${ai.detection.adversarial.confidence-threshold}")
    private double confidenceThreshold;
    
    @Value("${ai.detection.adversarial.max-perturbation}")
    private double maxPerturbation;
    
    private final MahalanobisDetector mahalanobisDetector;
    private final ReconstructionDetector reconstructionDetector;
    private final GradientAnalyzer gradientAnalyzer;
    
    public AdversarialDetector() {
        this.mahalanobisDetector = new MahalanobisDetector();
        this.reconstructionDetector = new ReconstructionDetector();
        this.gradientAnalyzer = new GradientAnalyzer();
    }
    
    public DetectionResponse detectAdversarial(AdversarialDetectionRequest request) {
        log.info("Starting adversarial attack detection for model: {}", request.getModelId());
        
        double[] inputData = request.getInputData();
        
        // Gradient analysis for known attack signatures
        Map<String, Double> gradientScores = gradientAnalyzer.analyzeGradients(inputData, request);
        
        // Feature space distance computation
        double mahalanobisScore = mahalanobisDetector.computeDistance(inputData, request);
        
        // Reconstruction error analysis
        double reconstructionScore = reconstructionDetector.computeReconstructionError(inputData);
        
        // Ensemble decision
        EnsembleResult ensembleResult = computeEnsembleDecision(
            gradientScores, mahalanobisScore, reconstructionScore
        );
        
        // Determine if adversarial
        boolean isAdversarial = ensembleResult.getConfidenceScore() > confidenceThreshold;
        ThreatLevel threatLevel = determineThreatLevel(ensembleResult.getConfidenceScore(), isAdversarial);
        
        return buildDetectionResponse(
            isAdversarial, ensembleResult, threatLevel, request
        );
    }
    
    private EnsembleResult computeEnsembleDecision(
            Map<String, Double> gradientScores,
            double mahalanobisScore,
            double reconstructionScore) {
        
        // Aggregate gradient scores
        double avgGradientScore = gradientScores.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        // Weighted ensemble voting
        double[] weights = {0.35, 0.35, 0.30}; // gradient, mahalanobis, reconstruction
        double[] scores = {avgGradientScore, mahalanobisScore, reconstructionScore};
        
        double ensembleScore = 0.0;
        for (int i = 0; i < weights.length; i++) {
            ensembleScore += weights[i] * scores[i];
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("gradientScores", gradientScores);
        details.put("mahalanobisScore", mahalanobisScore);
        details.put("reconstructionScore", reconstructionScore);
        details.put("weights", weights);
        
        return new EnsembleResult(ensembleScore, details);
    }
    
    private ThreatLevel determineThreatLevel(double confidenceScore, boolean isAdversarial) {
        if (!isAdversarial) {
            return ThreatLevel.LOW;
        }
        
        if (confidenceScore >= 0.9) {
            return ThreatLevel.CRITICAL;
        } else if (confidenceScore >= 0.8) {
            return ThreatLevel.HIGH;
        } else if (confidenceScore >= 0.7) {
            return ThreatLevel.MEDIUM;
        } else {
            return ThreatLevel.LOW;
        }
    }
    
    private DetectionResponse buildDetectionResponse(
            boolean isAdversarial,
            EnsembleResult ensembleResult,
            ThreatLevel threatLevel,
            AdversarialDetectionRequest request) {
        
        Map<String, Object> detectionDetails = new HashMap<>();
        detectionDetails.putAll(ensembleResult.getDetails());
        detectionDetails.put("isAdversarial", isAdversarial);
        detectionDetails.put("modelId", request.getModelId());
        detectionDetails.put("detectionMethods", Arrays.asList(
            "Gradient Analysis (FGSM, PGD, C&W)",
            "Mahalanobis Distance",
            "Reconstruction Error"
        ));
        
        List<DetectionResponse.AnomalySample> anomalies = new ArrayList<>();
        if (isAdversarial) {
            DetectionResponse.AnomalySample anomaly = DetectionResponse.AnomalySample.builder()
                .sampleIndex(0)
                .features(request.getInputData())
                .anomalyScore(ensembleResult.getConfidenceScore())
                .anomalyType("ADVERSARIAL_EXAMPLE")
                .featureContributions(analyzeFeatureContributions(request.getInputData()))
                .build();
            anomalies.add(anomaly);
        }
        
        String recommendation = generateRecommendation(isAdversarial, threatLevel, ensembleResult);
        
        return DetectionResponse.builder()
            .detectionId(UUID.randomUUID().toString())
            .detectionType("ADVERSARIAL_ATTACK")
            .threatScore(ensembleResult.getConfidenceScore())
            .threatLevel(threatLevel)
            .anomalousSamples(anomalies)
            .detectionDetails(detectionDetails)
            .timestamp(LocalDateTime.now())
            .recommendation(recommendation)
            .build();
    }
    
    private Map<String, Double> analyzeFeatureContributions(double[] input) {
        Map<String, Double> contributions = new HashMap<>();
        
        // Calculate feature importance based on magnitude
        double totalMagnitude = 0.0;
        for (double value : input) {
            totalMagnitude += Math.abs(value);
        }
        
        for (int i = 0; i < input.length; i++) {
            double contribution = Math.abs(input[i]) / (totalMagnitude + 1e-10);
            contributions.put("feature_" + i, contribution);
        }
        
        return contributions;
    }
    
    private String generateRecommendation(boolean isAdversarial, ThreatLevel level, EnsembleResult result) {
        if (!isAdversarial) {
            return "Input appears to be legitimate. Continue normal processing.";
        }
        
        switch (level) {
            case CRITICAL:
                return String.format(
                    "CRITICAL: High-confidence adversarial example detected (%.1f%% confidence). " +
                    "Block this input immediately and log the attack attempt. " +
                    "Consider implementing input sanitization and robust training.",
                    result.getConfidenceScore() * 100
                );
            case HIGH:
                return String.format(
                    "HIGH: Probable adversarial example detected (%.1f%% confidence). " +
                    "Apply defensive transformations before processing. " +
                    "Monitor for similar attack patterns.",
                    result.getConfidenceScore() * 100
                );
            case MEDIUM:
                return String.format(
                    "MEDIUM: Potential adversarial example detected (%.1f%% confidence). " +
                    "Apply input validation and consider ensemble predictions. " +
                    "Log for further analysis.",
                    result.getConfidenceScore() * 100
                );
            case LOW:
            default:
                return String.format(
                    "LOW: Possible adversarial perturbation detected (%.1f%% confidence). " +
                    "Monitor input patterns and validate model outputs.",
                    result.getConfidenceScore() * 100
                );
        }
    }
    
    private static class EnsembleResult {
        private final double confidenceScore;
        private final Map<String, Object> details;
        
        public EnsembleResult(double confidenceScore, Map<String, Object> details) {
            this.confidenceScore = confidenceScore;
            this.details = details;
        }
        
        public double getConfidenceScore() {
            return confidenceScore;
        }
        
        public Map<String, Object> getDetails() {
            return details;
        }
    }
}