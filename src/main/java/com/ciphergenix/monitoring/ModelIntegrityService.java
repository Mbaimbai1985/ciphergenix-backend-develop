package com.ciphergenix.monitoring;

import com.ciphergenix.detection.util.Statistics;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
public class ModelIntegrityService {

	public static class PredictionRecord {
		public final Instant timestamp;
		public final double[] outputDistribution;
		public final double latencyMs;

		public PredictionRecord(Instant timestamp, double[] outputDistribution, double latencyMs) {
			this.timestamp = timestamp;
			this.outputDistribution = outputDistribution;
			this.latencyMs = latencyMs;
		}
	}

	private final Deque<PredictionRecord> recentPredictions = new ArrayDeque<>();
	private final int maxWindowSize = 1000;

	public synchronized void recordPrediction(double[] outputDistribution, double latencyMs) {
		recentPredictions.addLast(new PredictionRecord(Instant.now(), outputDistribution, latencyMs));
		while (recentPredictions.size() > maxWindowSize) {
			recentPredictions.removeFirst();
		}
	}

	public synchronized double computeDriftScore() {
		if (recentPredictions.size() < 20) return 0.0;
		int n = recentPredictions.size();
		int half = n / 2;
		double[] a = averageDistribution(recentPredictions.stream().limit(half).toList());
		double[] b = averageDistribution(recentPredictions.stream().skip(half).toList());
		// JS divergence between averaged distributions
		return Statistics.jensenShannonDivergence(a, b, Math.max(5, a.length));
	}

	public synchronized double computePerformanceAnomalyScore() {
		if (recentPredictions.isEmpty()) return 0.0;
		double[] lat = recentPredictions.stream().mapToDouble(r -> r.latencyMs).toArray();
		double mean = Arrays.stream(lat).average().orElse(0.0);
		double var = 0.0; for (double v : lat) { double d = v - mean; var += d*d; }
		var /= Math.max(1, lat.length - 1);
		double std = Math.sqrt(Math.max(1e-9, var));
		double latest = lat[lat.length - 1];
		double z = Math.abs((latest - mean) / std);
		return Math.max(0.0, Math.min(1.0, z / 5.0));
	}

	public synchronized String fingerprintModelBehavior() {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			double[] avg = averageDistribution(new ArrayList<>(recentPredictions));
			String s = Arrays.toString(avg);
			byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 8 && i < hash.length; i++) sb.append(String.format("%02x", hash[i]));
			return sb.toString();
		} catch (Exception e) {
			return "behavior-fingerprint-error";
		}
	}

	public synchronized List<PredictionRecord> getRecentPredictions() {
		return new ArrayList<>(recentPredictions);
	}

	private double[] averageDistribution(List<PredictionRecord> records) {
		if (records.isEmpty()) return new double[]{};
		int d = records.get(0).outputDistribution.length;
		double[] avg = new double[d];
		for (PredictionRecord r : records) {
			for (int i = 0; i < d; i++) avg[i] += r.outputDistribution[i];
		}
		for (int i = 0; i < d; i++) avg[i] /= records.size();
		return avg;
	}
}