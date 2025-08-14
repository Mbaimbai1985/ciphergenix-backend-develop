package com.ciphergenix.detection.util;

import java.util.Arrays;

public final class Mahalanobis {

	private Mahalanobis() {}

	public static double distance(double[] x, double[] mean, double[][] cov) {
		double[][] inv = invert(copy(cov));
		double[] diff = new double[x.length];
		for (int i = 0; i < x.length; i++) diff[i] = x[i] - mean[i];
		double[] tmp = multiply(inv, diff);
		double sum = 0.0;
		for (int i = 0; i < diff.length; i++) sum += diff[i] * tmp[i];
		return Math.sqrt(Math.max(0.0, sum));
	}

	public static double[] mean(double[][] data) {
		int n = data.length;
		int d = data[0].length;
		double[] mean = new double[d];
		for (double[] row : data) {
			for (int j = 0; j < d; j++) mean[j] += row[j];
		}
		for (int j = 0; j < d; j++) mean[j] /= n;
		return mean;
	}

	public static double[][] covariance(double[][] data, double[] mean) {
		int n = data.length;
		int d = data[0].length;
		double[][] cov = new double[d][d];
		for (double[] row : data) {
			double[] diff = new double[d];
			for (int j = 0; j < d; j++) diff[j] = row[j] - mean[j];
			for (int i = 0; i < d; i++) {
				for (int j = 0; j < d; j++) {
					cov[i][j] += diff[i] * diff[j];
				}
			}
		}
		double denom = Math.max(1, n - 1);
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < d; j++) cov[i][j] /= denom;
		}
		for (int i = 0; i < d; i++) cov[i][i] += 1e-6; // regularization
		return cov;
	}

	private static double[] multiply(double[][] a, double[] x) {
		int n = a.length;
		double[] y = new double[n];
		for (int i = 0; i < n; i++) {
			double s = 0.0;
			for (int j = 0; j < n; j++) s += a[i][j] * x[j];
			y[i] = s;
		}
		return y;
	}

	private static double[][] copy(double[][] m) {
		double[][] c = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++) c[i] = Arrays.copyOf(m[i], m[i].length);
		return c;
	}

	// Gauss-Jordan inversion (for small matrices)
	private static double[][] invert(double[][] a) {
		int n = a.length;
		double[][] inv = new double[n][n];
		for (int i = 0; i < n; i++) inv[i][i] = 1.0;
		for (int i = 0; i < n; i++) {
			int pivot = i;
			double max = Math.abs(a[i][i]);
			for (int r = i + 1; r < n; r++) {
				double val = Math.abs(a[r][i]);
				if (val > max) { max = val; pivot = r; }
			}
			if (pivot != i) {
				double[] tmp = a[i]; a[i] = a[pivot]; a[pivot] = tmp;
				double[] itmp = inv[i]; inv[i] = inv[pivot]; inv[pivot] = itmp;
			}
			double diag = a[i][i];
			if (Math.abs(diag) < 1e-12) diag = 1e-12;
			for (int j = 0; j < n; j++) { a[i][j] /= diag; inv[i][j] /= diag; }
			for (int r = 0; r < n; r++) {
				if (r == i) continue;
				double factor = a[r][i];
				for (int c = 0; c < n; c++) {
					a[r][c] -= factor * a[i][c];
					inv[r][c] -= factor * inv[i][c];
				}
			}
		}
		return inv;
	}
}