package com.ciphergenix.security.detection;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class InfluenceFunctionAnalyzer {
    
    private static final double INFLUENCE_THRESHOLD = 0.7;
    private static final double EPSILON = 1e-6;
    
    public Map<Integer, Double> computeInfluences(List<double[]> samples) {
        Map<Integer, Double> influenceScores = new HashMap<>();
        
        if (samples.size() < 2) {
            log.warn("Not enough samples to compute influence functions");
            return influenceScores;
        }
        
        try {
            // Convert to matrix for easier computation
            double[][] dataMatrix = samples.toArray(new double[0][]);
            RealMatrix X = MatrixUtils.createRealMatrix(dataMatrix);
            
            // Compute sample statistics
            RealVector mean = computeMean(X);
            RealMatrix covariance = computeCovariance(X, mean);
            
            // Compute influence for each sample
            for (int i = 0; i < samples.size(); i++) {
                double influence = computeSampleInfluence(X, i, mean, covariance);
                influenceScores.put(i, influence);
            }
            
            // Normalize influence scores
            normalizeInfluenceScores(influenceScores);
            
        } catch (Exception e) {
            log.error("Error computing influence functions", e);
            // Return zero influences on error
            for (int i = 0; i < samples.size(); i++) {
                influenceScores.put(i, 0.0);
            }
        }
        
        return influenceScores;
    }
    
    private RealVector computeMean(RealMatrix X) {
        int n = X.getRowDimension();
        int d = X.getColumnDimension();
        
        RealVector mean = new ArrayRealVector(d);
        for (int j = 0; j < d; j++) {
            double sum = 0.0;
            for (int i = 0; i < n; i++) {
                sum += X.getEntry(i, j);
            }
            mean.setEntry(j, sum / n);
        }
        
        return mean;
    }
    
    private RealMatrix computeCovariance(RealMatrix X, RealVector mean) {
        int n = X.getRowDimension();
        int d = X.getColumnDimension();
        
        RealMatrix covariance = MatrixUtils.createRealMatrix(d, d);
        
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                double cov = 0.0;
                for (int k = 0; k < n; k++) {
                    cov += (X.getEntry(k, i) - mean.getEntry(i)) * 
                           (X.getEntry(k, j) - mean.getEntry(j));
                }
                covariance.setEntry(i, j, cov / (n - 1));
            }
        }
        
        return covariance;
    }
    
    private double computeSampleInfluence(RealMatrix X, int sampleIdx, 
                                        RealVector mean, RealMatrix covariance) {
        int n = X.getRowDimension();
        
        // Get the sample
        RealVector sample = X.getRowVector(sampleIdx);
        
        // Compute leave-one-out statistics
        RealVector looMean = computeLeaveOneOutMean(X, sampleIdx, mean);
        
        // Compute influence as the change in model parameters
        RealVector diff = sample.subtract(looMean);
        
        // Use Mahalanobis distance as influence measure
        double influence = 0.0;
        try {
            // Add regularization to avoid singular matrix
            RealMatrix regularizedCov = covariance.add(
                MatrixUtils.createRealIdentityMatrix(covariance.getColumnDimension())
                    .scalarMultiply(EPSILON)
            );
            
            RealMatrix covInverse = new LUDecomposition(regularizedCov).getSolver().getInverse();
            RealVector temp = covInverse.operate(diff);
            influence = Math.sqrt(diff.dotProduct(temp));
            
            // Scale by sample size to get relative influence
            influence = influence * n / (n - 1);
            
        } catch (SingularMatrixException e) {
            // Fallback to Euclidean distance if covariance is singular
            influence = diff.getNorm();
        }
        
        return influence;
    }
    
    private RealVector computeLeaveOneOutMean(RealMatrix X, int excludeIdx, RealVector fullMean) {
        int n = X.getRowDimension();
        int d = X.getColumnDimension();
        
        RealVector looMean = new ArrayRealVector(d);
        RealVector excludedSample = X.getRowVector(excludeIdx);
        
        // Efficient computation: mean_loo = (n * mean - sample) / (n - 1)
        for (int j = 0; j < d; j++) {
            double looValue = (n * fullMean.getEntry(j) - excludedSample.getEntry(j)) / (n - 1);
            looMean.setEntry(j, looValue);
        }
        
        return looMean;
    }
    
    private void normalizeInfluenceScores(Map<Integer, Double> scores) {
        if (scores.isEmpty()) {
            return;
        }
        
        // Find max influence for normalization
        double maxInfluence = scores.values().stream()
            .mapToDouble(Double::doubleValue)
            .max()
            .orElse(1.0);
        
        // Normalize to [0, 1] range
        if (maxInfluence > 0) {
            scores.replaceAll((k, v) -> v / maxInfluence);
        }
        
        // Apply threshold to identify high-influence samples
        scores.entrySet().removeIf(entry -> entry.getValue() < INFLUENCE_THRESHOLD);
    }
}