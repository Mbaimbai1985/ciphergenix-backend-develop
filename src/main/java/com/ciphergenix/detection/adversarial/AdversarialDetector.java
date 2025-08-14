package com.ciphergenix.detection.adversarial;

public abstract class AdversarialDetector {

	public static class AdversarialResult {
		public final boolean isAdversarial;
		public final double confidenceScore;

		public AdversarialResult(boolean isAdversarial, double confidenceScore) {
			this.isAdversarial = isAdversarial;
			this.confidenceScore = confidenceScore;
		}
	}

	public AdversarialDetector() {}

	public abstract AdversarialResult detectAdversarial(double[] inputFeatures);
}