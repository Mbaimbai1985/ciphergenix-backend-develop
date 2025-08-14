package com.ciphergenix.detection.util;

import java.util.Arrays;

public final class Statistics {

	private Statistics() {}

	public static double kolmogorovSmirnovD(double[] sampleA, double[] sampleB) {
		double[] a = Arrays.copyOf(sampleA, sampleA.length);
		double[] b = Arrays.copyOf(sampleB, sampleB.length);
		Arrays.sort(a);
		Arrays.sort(b);
		int i = 0, j = 0;
		double d = 0.0;
		while (i < a.length && j < b.length) {
			double va = a[i];
			double vb = b[j];
			if (va <= vb) {
				i++;
			} else {
				j++;
			}
			double cdfA = (double) i / a.length;
			double cdfB = (double) j / b.length;
			d = Math.max(d, Math.abs(cdfA - cdfB));
		}
		return d;
	}

	public static double jensenShannonDivergence(double[] sampleA, double[] sampleB, int numBins) {
		double min = Math.min(Arrays.stream(sampleA).min().orElse(0), Arrays.stream(sampleB).min().orElse(0));
		double max = Math.max(Arrays.stream(sampleA).max().orElse(1), Arrays.stream(sampleB).max().orElse(1));
		if (max == min) {
			return 0.0;
		}
		double[] p = histogram(sampleA, numBins, min, max);
		double[] q = histogram(sampleB, numBins, min, max);
		double[] m = new double[numBins];
		for (int i = 0; i < numBins; i++) {
			m[i] = 0.5 * (p[i] + q[i]);
		}
		double klPM = klDivergence(p, m);
		double klQM = klDivergence(q, m);
		return 0.5 * (klPM + klQM);
	}

	private static double klDivergence(double[] p, double[] q) {
		double sum = 0.0;
		for (int i = 0; i < p.length; i++) {
			if (p[i] > 0 && q[i] > 0) {
				sum += p[i] * Math.log(p[i] / q[i]);
			}
		}
		return sum / Math.log(2);
	}

	public static double[] histogram(double[] data, int bins, double min, double max) {
		double[] counts = new double[bins];
		double width = (max - min) / bins;
		if (width == 0) {
			counts[0] = 1.0;
			return counts;
		}
		for (double v : data) {
			int idx = (int) Math.floor((v - min) / width);
			if (idx < 0) idx = 0;
			if (idx >= bins) idx = bins - 1;
			counts[idx] += 1.0;
		}
		double total = data.length;
		if (total == 0) return counts;
		for (int i = 0; i < counts.length; i++) {
			counts[i] /= total;
		}
		return counts;
	}
}