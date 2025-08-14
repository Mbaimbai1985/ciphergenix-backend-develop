package com.ciphergenix.security.detection;

import com.ciphergenix.dto.AdversarialDetectionRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GradientAnalyzer {
    
    private static final double FGSM_THRESHOLD = 0.7;
    private static final double PGD_THRESHOLD = 0.75;
    private static final double CW_THRESHOLD = 0.8;
    
    public Map<String, Double> analyzeGradients(double[] input, AdversarialDetectionRequest request) {
        Map<String, Double> attackScores = new HashMap<>();
        
        try {
            // Analyze for FGSM (Fast Gradient Sign Method) patterns
            double fgsmScore = detectFGSMPattern(input);
            attackScores.put("FGSM", fgsmScore);
            
            // Analyze for PGD (Projected Gradient Descent) patterns
            double pgdScore = detectPGDPattern(input);
            attackScores.put("PGD", pgdScore);
            
            // Analyze for C&W (Carlini & Wagner) patterns
            double cwScore = detectCWPattern(input);
            attackScores.put("C&W", cwScore);
            
            log.debug("Gradient analysis scores - FGSM: {}, PGD: {}, C&W: {}", 
                     fgsmScore, pgdScore, cwScore);
            
        } catch (Exception e) {
            log.error("Error in gradient analysis", e);
            // Return neutral scores on error
            attackScores.put("FGSM", 0.0);
            attackScores.put("PGD", 0.0);
            attackScores.put("C&W", 0.0);
        }
        
        return attackScores;
    }
    
    private double detectFGSMPattern(double[] input) {
        // FGSM creates perturbations with uniform sign changes
        // Check for suspiciously uniform perturbations
        
        double meanAbs = 0.0;
        double variance = 0.0;
        
        for (double value : input) {
            meanAbs += Math.abs(value);
        }
        meanAbs /= input.length;
        
        for (double value : input) {
            double diff = Math.abs(value) - meanAbs;
            variance += diff * diff;
        }
        variance /= input.length;
        
        // Low variance in absolute values suggests FGSM
        double uniformityScore = 1.0 - Math.min(1.0, variance / (meanAbs * meanAbs + 1e-10));
        
        // Check for sign patterns
        int signChanges = 0;
        for (int i = 1; i < input.length; i++) {
            if (Math.signum(input[i]) != Math.signum(input[i-1])) {
                signChanges++;
            }
        }
        
        double signChangeRate = (double) signChanges / (input.length - 1);
        
        // FGSM tends to have regular sign changes
        double fgsmScore = 0.6 * uniformityScore + 0.4 * (signChangeRate > 0.3 ? 1.0 : signChangeRate / 0.3);
        
        return fgsmScore > FGSM_THRESHOLD ? fgsmScore : 0.0;
    }
    
    private double detectPGDPattern(double[] input) {
        // PGD creates iterative perturbations with bounded norms
        // Check for patterns indicating iterative optimization
        
        double l2Norm = 0.0;
        double linfNorm = 0.0;
        
        for (double value : input) {
            l2Norm += value * value;
            linfNorm = Math.max(linfNorm, Math.abs(value));
        }
        l2Norm = Math.sqrt(l2Norm);
        
        // Check if perturbations are bounded
        double boundednessScore = 0.0;
        if (linfNorm > 0) {
            // Check how many values are close to the L-inf bound
            int nearBound = 0;
            for (double value : input) {
                if (Math.abs(value) > 0.9 * linfNorm) {
                    nearBound++;
                }
            }
            boundednessScore = (double) nearBound / input.length;
        }
        
        // Check for structured patterns (PGD often creates structured perturbations)
        double structureScore = analyzeStructure(input);
        
        double pgdScore = 0.5 * boundednessScore + 0.5 * structureScore;
        
        return pgdScore > PGD_THRESHOLD ? pgdScore : 0.0;
    }
    
    private double detectCWPattern(double[] input) {
        // C&W creates minimal perturbations optimized for specific objectives
        // Check for sparse, targeted perturbations
        
        int nonZeroCount = 0;
        double totalMagnitude = 0.0;
        
        for (double value : input) {
            if (Math.abs(value) > 1e-6) {
                nonZeroCount++;
                totalMagnitude += Math.abs(value);
            }
        }
        
        // Sparsity score - C&W tends to create sparse perturbations
        double sparsityScore = 1.0 - ((double) nonZeroCount / input.length);
        
        // Concentration score - perturbations are concentrated in few dimensions
        double avgNonZeroMagnitude = nonZeroCount > 0 ? totalMagnitude / nonZeroCount : 0.0;
        double concentrationScore = 0.0;
        
        if (avgNonZeroMagnitude > 0) {
            for (double value : input) {
                if (Math.abs(value) > 1e-6) {
                    double relMagnitude = Math.abs(value) / avgNonZeroMagnitude;
                    concentrationScore += relMagnitude > 0.8 ? 1.0 : 0.0;
                }
            }
            concentrationScore = nonZeroCount > 0 ? concentrationScore / nonZeroCount : 0.0;
        }
        
        double cwScore = 0.6 * sparsityScore + 0.4 * concentrationScore;
        
        return cwScore > CW_THRESHOLD ? cwScore : 0.0;
    }
    
    private double analyzeStructure(double[] input) {
        // Analyze for structured patterns in the perturbations
        // Look for autocorrelation and patterns
        
        if (input.length < 2) {
            return 0.0;
        }
        
        double autocorrelation = 0.0;
        double mean = 0.0;
        
        for (double value : input) {
            mean += value;
        }
        mean /= input.length;
        
        double variance = 0.0;
        for (double value : input) {
            variance += (value - mean) * (value - mean);
        }
        variance /= input.length;
        
        if (variance > 1e-10) {
            for (int lag = 1; lag < Math.min(10, input.length); lag++) {
                double lagCorr = 0.0;
                for (int i = 0; i < input.length - lag; i++) {
                    lagCorr += (input[i] - mean) * (input[i + lag] - mean);
                }
                lagCorr /= (input.length - lag) * variance;
                autocorrelation += Math.abs(lagCorr);
            }
            autocorrelation /= Math.min(9, input.length - 1);
        }
        
        return Math.min(1.0, autocorrelation * 2.0);
    }
}