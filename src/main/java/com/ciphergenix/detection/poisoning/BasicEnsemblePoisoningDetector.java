package com.ciphergenix.detection.poisoning;

import com.ciphergenix.detection.util.Statistics;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BasicEnsemblePoisoningDetector extends DataPoisoningDetector {

	public BasicEnsemblePoisoningDetector() {
		super();
	}

	@Override
	public DetectionResult detectPoisoning(List<double[]> dataset, Map<String, Object> baselineStats) {
		if (dataset == null || dataset.isEmpty()) {
			return new DetectionResult(0.0, List.of());
		}

		// For demo: compute per-feature JS divergence and KS distance vs baseline vectors
		int numFeatures = dataset.get(0).length;
		List<Integer> anomalousIdx = new ArrayList<>();

		// Build arrays per feature
		double[][] dataByFeature = new double[numFeatures][];
		for (int f = 0; f < numFeatures; f++) {
			double[] col = new double[dataset.size()];
			for (int i = 0; i < dataset.size(); i++) col[i] = dataset.get(i)[f];
			dataByFeature[f] = col;
		}

		double jsSum = 0.0;
		double ksSum = 0.0;
		for (int f = 0; f < numFeatures; f++) {
			double[] baseline = (double[]) baselineStats.getOrDefault("feature_" + f, new double[]{});
			if (baseline.length > 1) {
				jsSum += Statistics.jensenShannonDivergence(dataByFeature[f], baseline, 20);
				ksSum += Statistics.kolmogorovSmirnovD(dataByFeature[f], baseline);
			}
		}
		double jsAvg = jsSum / Math.max(1, numFeatures);
		double ksAvg = ksSum / Math.max(1, numFeatures);
		double threat = Math.min(1.0, 0.5 * jsAvg + 0.5 * ksAvg);

		// Mark top 5% as anomalous by simple z-score heuristic per instance (placeholder)
		double[] rowSums = new double[dataset.size()];
		for (int i = 0; i < dataset.size(); i++) {
			double s = 0.0;
			for (int f = 0; f < numFeatures; f++) s += dataset.get(i)[f];
			rowSums[i] = s;
		}
		double mean = 0.0; for (double v : rowSums) mean += v; mean /= rowSums.length;
		double var = 0.0; for (double v : rowSums) { double d = v - mean; var += d*d; } var /= Math.max(1, rowSums.length - 1);
		double std = Math.sqrt(Math.max(1e-9, var));
		for (int i = 0; i < rowSums.length; i++) {
			double z = Math.abs((rowSums[i] - mean) / std);
			if (z > 2.0) anomalousIdx.add(i);
		}

		return new DetectionResult(threat, anomalousIdx);
	}
}