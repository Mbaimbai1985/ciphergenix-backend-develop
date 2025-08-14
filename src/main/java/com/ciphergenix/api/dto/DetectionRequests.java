package com.ciphergenix.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public class DetectionRequests {

	public static class PoisoningDetectRequest {
		@NotNull
		public List<double[]> dataset;
		public Map<String, Object> baselineStats;
	}

	public static class PoisoningDetectResponse {
		public double threatScore;
		public List<Integer> anomalousSampleIndices;
	}

	public static class AdversarialDetectRequest {
		@NotNull
		public double[] inputFeatures;
	}

	public static class AdversarialDetectResponse {
		public boolean adversarial;
		public double confidence;
	}

	public static class IngestRequest {
		@NotNull
		public String data;
		@NotNull
		public String role;
	}

	public static class IngestResponse {
		public String ciphertextBase64;
		public String ivBase64;
		public String hmacBase64;
	}

	public static class PredictionRecordRequest {
		@NotNull
		public double[] outputDistribution;
		public double latencyMs;
	}

	public static class MonitoringStatusResponse {
		public double driftScore;
		public double performanceAnomalyScore;
		public String behaviorFingerprint;
	}
}