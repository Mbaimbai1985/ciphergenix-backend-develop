package com.ciphergenix.ai.detection;

import org.springframework.stereotype.Service;
import smile.anomaly.IsolationForest;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects data poisoning attacks in training datasets using an ensemble of statistical and ML-based methods.
 */
@Service
public class DataPoisoningDetector {

    private IsolationForest isolationForest;

    public DataPoisoningDetector() {
        // Initialize IsolationForest with default parameters; in production these could be tuned or injected
        this.isolationForest = null; // Lazy initialization in detectPoisoning
    }

    /**
     * Detects potential poisoning in the provided dataset.
     *
     * @param dataset 2-D array representing samples and features
     * @return DetectionResult containing threat score and list of anomalous sample indices
     */
    public DetectionResult detectPoisoning(double[][] dataset) {
        if (dataset == null || dataset.length == 0) {
            return new DetectionResult(0.0, List.of());
        }

        // Placeholder simple statistical outlier detection using IsolationForest from Smile
        if (isolationForest == null) {
            isolationForest = IsolationForest.fit(dataset);
        }

        double[] scores = isolationForest.score(dataset);
        double threshold = computeThreshold(scores);
        List<Integer> anomalies = new ArrayList<>();
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] < threshold) { // lower scores imply anomalies in Smile's implementation
                anomalies.add(i);
            }
        }

        double threatScore = anomalies.isEmpty() ? 0.0 : (double) anomalies.size() / dataset.length;
        return new DetectionResult(threatScore, anomalies);
    }

    private double computeThreshold(double[] scores) {
        // Simple heuristic: use the 5th percentile as threshold
        double[] copy = scores.clone();
        java.util.Arrays.sort(copy);
        int idx = (int) (copy.length * 0.05);
        return copy[idx];
    }
}