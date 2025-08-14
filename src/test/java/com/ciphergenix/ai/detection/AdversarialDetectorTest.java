package com.ciphergenix.ai.detection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AdversarialDetectorTest {

    private AdversarialDetector detector;

    @BeforeEach
    public void setup() {
        detector = new AdversarialDetector();
        double[][] baseline = new double[100][3];
        for (int i = 0; i < 100; i++) {
            baseline[i][0] = Math.random();
            baseline[i][1] = Math.random();
            baseline[i][2] = Math.random();
        }
        detector.calibrate(baseline);
    }

    @Test
    public void testDetectNonAdversarial() {
        double[] input = {0.5, 0.5, 0.5};
        AdversarialDetectionResult result = detector.detect(input);
        Assertions.assertFalse(result.isAdversarial());
    }

    @Test
    public void testDetectAdversarial() {
        double[] input = {10, 10, 10};
        AdversarialDetectionResult result = detector.detect(input);
        Assertions.assertTrue(result.isAdversarial());
        Assertions.assertTrue(result.getConfidence() > 0.5);
    }
}