package com.ciphergenix.security.monitoring;

import com.ciphergenix.dto.ModelMonitoringRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DriftDetector {
    
    private static final double DRIFT_THRESHOLD = 0.15;
    
    public ModelIntegrityMonitor.DriftResult detectDrift(
            ModelMonitoringRequest.ModelSnapshot current,
            ModelMonitoringRequest.ModelSnapshot baseline) {
        
        Map<String, Double> layerDrifts = new HashMap<>();
        double maxDrift = 0.0;
        boolean hasDrift = false;
        
        // Check weight distribution drift for each layer
        if (current.getLayerWeights() != null && baseline.getLayerWeights() != null) {
            for (Map.Entry<String, double[]> entry : current.getLayerWeights().entrySet()) {
                String layerName = entry.getKey();
                double[] currentWeights = entry.getValue();
                
                if (baseline.getLayerWeights().containsKey(layerName)) {
                    double[] baselineWeights = baseline.getLayerWeights().get(layerName);
                    
                    // Calculate statistical drift measures
                    double driftScore = calculateLayerDrift(currentWeights, baselineWeights);
                    layerDrifts.put(layerName, driftScore);
                    
                    if (driftScore > DRIFT_THRESHOLD) {
                        hasDrift = true;
                        maxDrift = Math.max(maxDrift, driftScore);
                    }
                }
            }
        }
        
        // Check output distribution drift
        if (current.getOutputDistribution() != null && baseline.getOutputDistribution() != null) {
            double outputDrift = calculateOutputDistributionDrift(
                current.getOutputDistribution(),
                baseline.getOutputDistribution()
            );
            layerDrifts.put("output_distribution", outputDrift);
            
            if (outputDrift > DRIFT_THRESHOLD) {
                hasDrift = true;
                maxDrift = Math.max(maxDrift, outputDrift);
            }
        }
        
        // Calculate overall drift score
        double overallDriftScore = calculateOverallDriftScore(layerDrifts);
        
        return new ModelIntegrityMonitor.DriftResult(hasDrift, overallDriftScore, layerDrifts);
    }
    
    private double calculateLayerDrift(double[] current, double[] baseline) {
        if (current.length != baseline.length) {
            log.warn("Layer dimensions mismatch: {} vs {}", current.length, baseline.length);
            return 1.0; // Maximum drift
        }
        
        // Calculate various drift metrics
        double psiScore = calculatePSI(current, baseline);
        double wassersteinDistance = calculateWassersteinDistance(current, baseline);
        double ksStatistic = calculateKolmogorovSmirnov(current, baseline);
        
        // Combine metrics with weights
        double combinedDrift = 0.4 * psiScore + 0.3 * wassersteinDistance + 0.3 * ksStatistic;
        
        return Math.min(1.0, combinedDrift);
    }
    
    private double calculatePSI(double[] current, double[] baseline) {
        // Population Stability Index calculation
        int numBins = Math.min(10, current.length / 10);
        double[] currentHist = createHistogram(current, numBins);
        double[] baselineHist = createHistogram(baseline, numBins);
        
        double psi = 0.0;
        for (int i = 0; i < numBins; i++) {
            double currentPct = currentHist[i] + 1e-10;
            double baselinePct = baselineHist[i] + 1e-10;
            
            psi += (currentPct - baselinePct) * Math.log(currentPct / baselinePct);
        }
        
        return Math.abs(psi);
    }
    
    private double calculateWassersteinDistance(double[] current, double[] baseline) {
        // Simplified 1D Wasserstein distance (Earth Mover's Distance)
        DescriptiveStatistics currentStats = new DescriptiveStatistics(current);
        DescriptiveStatistics baselineStats = new DescriptiveStatistics(baseline);
        
        // Compare distributions using quantiles
        double distance = 0.0;
        for (int p = 10; p <= 90; p += 10) {
            double currentQuantile = currentStats.getPercentile(p);
            double baselineQuantile = baselineStats.getPercentile(p);
            distance += Math.abs(currentQuantile - baselineQuantile);
        }
        
        // Normalize by scale
        double scale = Math.max(
            baselineStats.getMax() - baselineStats.getMin(),
            currentStats.getMax() - currentStats.getMin()
        );
        
        return scale > 0 ? distance / (9 * scale) : distance;
    }
    
    private double calculateKolmogorovSmirnov(double[] current, double[] baseline) {
        // Simplified K-S test statistic
        DescriptiveStatistics currentStats = new DescriptiveStatistics(current);
        DescriptiveStatistics baselineStats = new DescriptiveStatistics(baseline);
        
        double meanDiff = Math.abs(currentStats.getMean() - baselineStats.getMean());
        double stdDiff = Math.abs(currentStats.getStandardDeviation() - baselineStats.getStandardDeviation());
        
        double pooledStd = Math.sqrt(
            (currentStats.getStandardDeviation() * currentStats.getStandardDeviation() +
             baselineStats.getStandardDeviation() * baselineStats.getStandardDeviation()) / 2
        );
        
        if (pooledStd > 0) {
            return (meanDiff / pooledStd + stdDiff / baselineStats.getStandardDeviation()) / 2;
        }
        
        return meanDiff + stdDiff;
    }
    
    private double[] createHistogram(double[] data, int numBins) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (double value : data) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        double binWidth = (max - min) / numBins;
        double[] histogram = new double[numBins];
        
        for (double value : data) {
            int binIndex = (int) ((value - min) / binWidth);
            binIndex = Math.min(binIndex, numBins - 1);
            histogram[binIndex]++;
        }
        
        // Normalize to percentages
        for (int i = 0; i < numBins; i++) {
            histogram[i] /= data.length;
        }
        
        return histogram;
    }
    
    private double calculateOutputDistributionDrift(
            Map<String, Double> current, Map<String, Double> baseline) {
        
        // Jensen-Shannon divergence for probability distributions
        double jsDiv = 0.0;
        
        // Create average distribution
        Map<String, Double> average = new HashMap<>();
        for (String key : current.keySet()) {
            double p = current.get(key);
            double q = baseline.getOrDefault(key, 0.0);
            average.put(key, (p + q) / 2);
        }
        for (String key : baseline.keySet()) {
            if (!average.containsKey(key)) {
                average.put(key, baseline.get(key) / 2);
            }
        }
        
        // Calculate KL divergences
        double klPM = 0.0;
        double klQM = 0.0;
        
        for (String key : average.keySet()) {
            double m = average.get(key);
            if (m > 0) {
                double p = current.getOrDefault(key, 1e-10);
                double q = baseline.getOrDefault(key, 1e-10);
                
                if (p > 0) klPM += p * Math.log(p / m);
                if (q > 0) klQM += q * Math.log(q / m);
            }
        }
        
        jsDiv = (klPM + klQM) / 2;
        
        return Math.sqrt(jsDiv); // JS distance
    }
    
    private double calculateOverallDriftScore(Map<String, Double> layerDrifts) {
        if (layerDrifts.isEmpty()) {
            return 0.0;
        }
        
        // Weighted average with higher weight for output layers
        double totalScore = 0.0;
        double totalWeight = 0.0;
        
        for (Map.Entry<String, Double> entry : layerDrifts.entrySet()) {
            String layerName = entry.getKey();
            double drift = entry.getValue();
            
            // Give higher weight to output-related layers
            double weight = layerName.contains("output") ? 2.0 : 1.0;
            
            totalScore += drift * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? totalScore / totalWeight : 0.0;
    }
}