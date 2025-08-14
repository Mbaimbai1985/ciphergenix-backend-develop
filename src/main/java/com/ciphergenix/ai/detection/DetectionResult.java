package com.ciphergenix.ai.detection;

import java.util.List;

public class DetectionResult {

    private final double threatScore;
    private final List<Integer> anomalousIndexes;

    public DetectionResult(double threatScore, List<Integer> anomalousIndexes) {
        this.threatScore = threatScore;
        this.anomalousIndexes = anomalousIndexes;
    }

    public double getThreatScore() {
        return threatScore;
    }

    public List<Integer> getAnomalousIndexes() {
        return anomalousIndexes;
    }
}