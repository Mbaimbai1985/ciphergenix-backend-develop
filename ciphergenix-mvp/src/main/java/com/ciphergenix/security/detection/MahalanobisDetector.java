package com.ciphergenix.security.detection;

import com.ciphergenix.dto.AdversarialDetectionRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.descriptive.MultivariateSummaryStatistics;

import java.util.Random;

@Slf4j
public class MahalanobisDetector {
    
    private static final double EPSILON = 1e-6;
    private RealVector cachedMean;
    private RealMatrix cachedCovariance;
    
    public double computeDistance(double[] input, AdversarialDetectionRequest request) {
        try {
            // In production, these statistics would be pre-computed from clean training data
            if (cachedMean == null || cachedCovariance == null) {
                initializeStatistics(input.length);
            }
            
            RealVector inputVector = new ArrayRealVector(input);
            
            // Compute Mahalanobis distance
            RealVector diff = inputVector.subtract(cachedMean);
            
            // Regularize covariance matrix to avoid singularity
            RealMatrix regularizedCov = cachedCovariance.add(
                MatrixUtils.createRealIdentityMatrix(cachedCovariance.getColumnDimension())
                    .scalarMultiply(EPSILON)
            );
            
            DecompositionSolver solver = new LUDecomposition(regularizedCov).getSolver();
            RealMatrix covInverse = solver.getInverse();
            
            RealVector temp = covInverse.operate(diff);
            double distance = Math.sqrt(diff.dotProduct(temp));
            
            // Normalize to [0, 1] range
            return normalizeDistance(distance);
            
        } catch (Exception e) {
            log.error("Error computing Mahalanobis distance", e);
            return 0.5; // Return neutral score on error
        }
    }
    
    private void initializeStatistics(int dimension) {
        // In production, load from pre-computed statistics
        // For demo, generate synthetic statistics
        Random random = new Random(42);
        
        // Generate mean vector
        double[] meanArray = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            meanArray[i] = random.nextGaussian() * 0.5;
        }
        cachedMean = new ArrayRealVector(meanArray);
        
        // Generate covariance matrix (positive semi-definite)
        cachedCovariance = generateCovarianceMatrix(dimension, random);
    }
    
    private RealMatrix generateCovarianceMatrix(int dimension, Random random) {
        // Generate a random positive semi-definite matrix
        RealMatrix A = MatrixUtils.createRealMatrix(dimension, dimension);
        
        // Fill with random values
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                A.setEntry(i, j, random.nextGaussian() * 0.3);
            }
        }
        
        // Make it symmetric positive semi-definite: C = A^T * A
        RealMatrix covariance = A.transpose().multiply(A);
        
        // Add small diagonal term for numerical stability
        for (int i = 0; i < dimension; i++) {
            covariance.setEntry(i, i, covariance.getEntry(i, i) + 0.1);
        }
        
        return covariance;
    }
    
    private double normalizeDistance(double distance) {
        // Use sigmoid-like function to map distance to [0, 1]
        // Higher distance indicates higher likelihood of adversarial example
        return 1.0 - Math.exp(-distance * 0.5);
    }
}