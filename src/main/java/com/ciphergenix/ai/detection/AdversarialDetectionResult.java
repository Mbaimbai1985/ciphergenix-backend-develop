package com.ciphergenix.ai.detection;

public class AdversarialDetectionResult {

    private final boolean adversarial;
    private final double confidence;

    public AdversarialDetectionResult(boolean adversarial, double confidence) {
        this.adversarial = adversarial;
        this.confidence = confidence;
    }

    public boolean isAdversarial() {
        return adversarial;
    }

    public double getConfidence() {
        return confidence;
    }
}