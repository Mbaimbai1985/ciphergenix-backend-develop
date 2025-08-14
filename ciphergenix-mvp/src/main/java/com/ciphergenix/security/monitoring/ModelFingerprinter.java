package com.ciphergenix.security.monitoring;

import com.ciphergenix.dto.ModelMonitoringRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class ModelFingerprinter {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    
    public ModelIntegrityMonitor.FingerprintResult generateFingerprint(
            ModelMonitoringRequest.ModelSnapshot snapshot) {
        
        Map<String, String> layerChecksums = new HashMap<>();
        
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            
            // Process layer weights in deterministic order
            if (snapshot.getLayerWeights() != null) {
                TreeMap<String, double[]> sortedWeights = new TreeMap<>(snapshot.getLayerWeights());
                
                for (Map.Entry<String, double[]> entry : sortedWeights.entrySet()) {
                    String layerName = entry.getKey();
                    double[] weights = entry.getValue();
                    
                    // Generate layer-specific checksum
                    String layerChecksum = generateLayerChecksum(weights);
                    layerChecksums.put(layerName, layerChecksum);
                    
                    // Add to overall digest
                    digest.update(layerName.getBytes());
                    digest.update(layerChecksum.getBytes());
                }
            }
            
            // Include model metadata in fingerprint
            if (snapshot.getAccuracy() != null) {
                digest.update(ByteBuffer.allocate(8).putDouble(snapshot.getAccuracy()).array());
            }
            
            if (snapshot.getLoss() != null) {
                digest.update(ByteBuffer.allocate(8).putDouble(snapshot.getLoss()).array());
            }
            
            // Include output distribution
            if (snapshot.getOutputDistribution() != null) {
                TreeMap<String, Double> sortedDist = new TreeMap<>(snapshot.getOutputDistribution());
                for (Map.Entry<String, Double> entry : sortedDist.entrySet()) {
                    digest.update(entry.getKey().getBytes());
                    digest.update(ByteBuffer.allocate(8).putDouble(entry.getValue()).array());
                }
            }
            
            // Generate final checksum
            byte[] hash = digest.digest();
            String overallChecksum = bytesToHex(hash);
            
            log.debug("Generated model fingerprint: {}", overallChecksum);
            
            return new ModelIntegrityMonitor.FingerprintResult(overallChecksum, layerChecksums);
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate model fingerprint", e);
            return new ModelIntegrityMonitor.FingerprintResult("ERROR", layerChecksums);
        }
    }
    
    private String generateLayerChecksum(double[] weights) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            
            // Create a deterministic representation of weights
            for (double weight : weights) {
                // Convert to fixed-precision representation
                long bits = Double.doubleToLongBits(Math.round(weight * 1e6) / 1e6);
                digest.update(ByteBuffer.allocate(8).putLong(bits).array());
            }
            
            byte[] hash = digest.digest();
            return bytesToHex(hash);
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate layer checksum", e);
            return "ERROR";
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}