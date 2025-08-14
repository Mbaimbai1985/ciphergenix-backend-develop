package com.ciphergenix.ai.detection;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AdversarialDetector {

    private double[] baselineMean;
    private double[] baselineStd;

    /**
     * Initializes the detector with baseline statistics from legitimate inputs.
     * In a production setting these could be periodically updated.
     *
     * @param baselineInputs 2D array, each row an input sample
     */
    public void calibrate(double[][] baselineInputs) {
        int dim = baselineInputs[0].length;
        baselineMean = new double[dim];
        baselineStd = new double[dim];
        int n = baselineInputs.length;
        for (double[] sample : baselineInputs) {
            for (int i = 0; i < dim; i++) {
                baselineMean[i] += sample[i];
            }
        }
        for (int i = 0; i < dim; i++) {
            baselineMean[i] /= n;
        }
        // Compute std
        for (double[] sample : baselineInputs) {
            for (int i = 0; i < dim; i++) {
                baselineStd[i] += Math.pow(sample[i] - baselineMean[i], 2);
            }
        }
        for (int i = 0; i < dim; i++) {
            baselineStd[i] = Math.sqrt(baselineStd[i] / n);
            if (baselineStd[i] == 0) baselineStd[i] = 1e-6; // avoid div by zero
        }
    }

    /**
     * Detects whether the given input appears adversarial based on distance from baseline statistics.
     *
     * @param input one-dimensional feature vector
     * @return result containing adversarial flag and confidence score (0-1)
     */
    public AdversarialDetectionResult detect(double[] input) {
        if (baselineMean == null || baselineStd == null) {
            throw new IllegalStateException("Detector not calibrated. Call calibrate() first.");
        }

        double mahalanobis = computeMahalanobisDistance(input);
        // Simple threshold: flag as adversarial if distance > 3 (assuming normalized features)
        boolean isAdv = mahalanobis > 3.0;
        double confidence = Math.min(1.0, mahalanobis / 10.0);
        return new AdversarialDetectionResult(isAdv, confidence);
    }

    private double computeMahalanobisDistance(double[] x) {
        double sum = 0.0;
        for (int i = 0; i < x.length; i++) {
            double z = (x[i] - baselineMean[i]) / baselineStd[i];
            sum += z * z;
        }
        return Math.sqrt(sum);
    }
}