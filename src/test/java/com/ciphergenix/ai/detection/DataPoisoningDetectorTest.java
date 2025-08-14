package com.ciphergenix.ai.detection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataPoisoningDetectorTest {

    @Test
    public void testDetectPoisoning() {
        DataPoisoningDetector detector = new DataPoisoningDetector();
        double[][] cleanData = new double[100][5];
        // fill with normal values
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 5; j++) {
                cleanData[i][j] = Math.random();
            }
        }
        DetectionResult cleanResult = detector.detectPoisoning(cleanData);
        Assertions.assertTrue(cleanResult.getThreatScore() < 0.2);

        // create poisoned sample
        double[][] poisoned = new double[110][5];
        System.arraycopy(cleanData, 0, poisoned, 0, 100);
        for (int i = 100; i < 110; i++) {
            for (int j = 0; j < 5; j++) {
                poisoned[i][j] = 10 + Math.random();
            }
        }
        DetectionResult poisonedResult = detector.detectPoisoning(poisoned);
        Assertions.assertTrue(poisonedResult.getThreatScore() > cleanResult.getThreatScore());
    }
}