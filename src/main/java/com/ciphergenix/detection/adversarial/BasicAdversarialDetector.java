package com.ciphergenix.detection.adversarial;

import com.ciphergenix.detection.util.Mahalanobis;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class BasicAdversarialDetector extends AdversarialDetector {

	private volatile double[] featureMean = new double[] {0.0};
	private volatile double[][] featureCov = new double[][] {{1.0}};

	public void updateReference(double[][] cleanFeatureEmbeddings) {
		if (cleanFeatureEmbeddings == null || cleanFeatureEmbeddings.length == 0) return;
		this.featureMean = Mahalanobis.mean(cleanFeatureEmbeddings);
		this.featureCov = Mahalanobis.covariance(cleanFeatureEmbeddings, featureMean);
	}

	@Override
	public AdversarialResult detectAdversarial(double[] inputFeatures) {
		// Mahalanobis distance in feature space
		double dist = Mahalanobis.distance(inputFeatures, featureMean, featureCov);
		// Simple threshold/score mapping
		double score = 1.0 / (1.0 + Math.exp(- (dist - 3.0))); // sigmoid centered at 3.0
		boolean isAdv = score > 0.5;
		return new AdversarialResult(isAdv, score);
	}
}