package com.ciphergenix.detection.poisoning;

import java.util.List;
import java.util.Map;

public abstract class DataPoisoningDetector {

	public static class DetectionResult {
		public final double threatScore;
		public final List<Integer> anomalousSampleIndices;

		public DetectionResult(double threatScore, List<Integer> anomalousSampleIndices) {
			this.threatScore = threatScore;
			this.anomalousSampleIndices = anomalousSampleIndices;
		}
	}

	public DataPoisoningDetector() {
	}

	public abstract DetectionResult detectPoisoning(List<double[]> dataset, Map<String, Object> baselineStats);
}