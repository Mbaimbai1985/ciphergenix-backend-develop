package com.ciphergenix.ai.monitoring;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class ModelIntegrityMonitor {

    private static final int WINDOW_SIZE = 1000;
    private final Deque<Double> rollingPredictions = new ArrayDeque<>();

    private double baselineMean = Double.NaN;

    /**
     * Call once after training or deployment to set baseline stats.
     */
    public void setBaselineMean(double mean) {
        this.baselineMean = mean;
    }

    /**
     * Record a new prediction output and return whether drift is detected.
     */
    public boolean recordPrediction(double prediction) {
        rollingPredictions.addLast(prediction);
        if (rollingPredictions.size() > WINDOW_SIZE) {
            rollingPredictions.removeFirst();
        }
        if (Double.isNaN(baselineMean)) {
            return false; // cannot assess yet
        }
        double currentMean = rollingPredictions.stream().mapToDouble(d -> d).average().orElse(prediction);
        double delta = Math.abs(currentMean - baselineMean);
        // simple rule: drift if mean shifts by >10% of baseline magnitude
        return delta > Math.abs(baselineMean) * 0.1;
    }
}