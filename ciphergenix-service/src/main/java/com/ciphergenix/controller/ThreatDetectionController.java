package com.ciphergenix.controller;

import com.ciphergenix.domain.ThreatDetection;
import com.ciphergenix.ml.detection.DataPoisoningDetector;
import com.ciphergenix.ml.detection.AdversarialDetector;
import com.ciphergenix.ml.monitoring.ModelIntegrityMonitor;
import com.ciphergenix.service.ThreatDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/threat-detection")
@CrossOrigin(origins = "*")
public class ThreatDetectionController {
    
    @Autowired
    private ThreatDetectionService threatDetectionService;
    
    @Autowired
    private DataPoisoningDetector dataPoisoningDetector;
    
    @Autowired
    private AdversarialDetector adversarialDetector;
    
    @Autowired
    private ModelIntegrityMonitor modelIntegrityMonitor;
    
    /**
     * Detect data poisoning in training dataset
     */
    @PostMapping("/data-poisoning")
    public ResponseEntity<DataPoisoningDetector.DetectionResult> detectDataPoisoning(
            @RequestBody DataPoisoningRequest request) {
        
        try {
            DataPoisoningDetector.DetectionResult result = 
                dataPoisoningDetector.detectPoisoning(request.getDataset(), request.getBaselineStats());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Detect adversarial attacks
     */
    @PostMapping("/adversarial")
    public ResponseEntity<AdversarialDetector.DetectionResult> detectAdversarial(
            @RequestBody AdversarialDetectionRequest request) {
        
        try {
            AdversarialDetector.DetectionResult result = 
                adversarialDetector.detectAdversarial(request.getInputData(), request.getModel(), request.getOriginalPrediction());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Monitor model integrity
     */
    @PostMapping("/model-integrity")
    public ResponseEntity<ModelIntegrityMonitor.MonitoringResult> monitorModelIntegrity(
            @RequestBody ModelIntegrityRequest request) {
        
        try {
            ModelIntegrityMonitor.MonitoringResult result = 
                modelIntegrityMonitor.monitorModelIntegrity(
                    request.getModelId(), 
                    request.getInputData(), 
                    request.getOutputData(), 
                    request.getMetadata()
                );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all threat detections
     */
    @GetMapping("/threats")
    public ResponseEntity<List<ThreatDetection>> getAllThreats() {
        List<ThreatDetection> threats = threatDetectionService.getAllThreatDetections();
        return ResponseEntity.ok(threats);
    }
    
    /**
     * Get threats by model ID
     */
    @GetMapping("/threats/model/{modelId}")
    public ResponseEntity<List<ThreatDetection>> getThreatsByModel(@PathVariable String modelId) {
        List<ThreatDetection> threats = threatDetectionService.getThreatDetectionsByModel(modelId);
        return ResponseEntity.ok(threats);
    }
    
    /**
     * Get threats by type
     */
    @GetMapping("/threats/type/{threatType}")
    public ResponseEntity<List<ThreatDetection>> getThreatsByType(@PathVariable String threatType) {
        List<ThreatDetection> threats = threatDetectionService.getThreatDetectionsByType(threatType);
        return ResponseEntity.ok(threats);
    }
    
    /**
     * Get threat statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getThreatStatistics() {
        Map<String, Object> statistics = threatDetectionService.getThreatStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Create new threat detection
     */
    @PostMapping("/threats")
    public ResponseEntity<ThreatDetection> createThreatDetection(@RequestBody ThreatDetection threatDetection) {
        ThreatDetection created = threatDetectionService.createThreatDetection(threatDetection);
        return ResponseEntity.ok(created);
    }
    
    /**
     * Update threat detection
     */
    @PutMapping("/threats/{id}")
    public ResponseEntity<ThreatDetection> updateThreatDetection(
            @PathVariable Long id, @RequestBody ThreatDetection threatDetection) {
        threatDetection.setId(id);
        ThreatDetection updated = threatDetectionService.updateThreatDetection(threatDetection);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Delete threat detection
     */
    @DeleteMapping("/threats/{id}")
    public ResponseEntity<Void> deleteThreatDetection(@PathVariable Long id) {
        threatDetectionService.deleteThreatDetection(id);
        return ResponseEntity.noContent().build();
    }
    
    // Data transfer objects
    public static class DataPoisoningRequest {
        private double[][] dataset;
        private Map<String, double[]> baselineStats;
        
        // Getters and Setters
        public double[][] getDataset() { return dataset; }
        public void setDataset(double[][] dataset) { this.dataset = dataset; }
        
        public Map<String, double[]> getBaselineStats() { return baselineStats; }
        public void setBaselineStats(Map<String, double[]> baselineStats) { this.baselineStats = baselineStats; }
    }
    
    public static class AdversarialDetectionRequest {
        private double[] inputData;
        private Object model;
        private double[] originalPrediction;
        
        // Getters and Setters
        public double[] getInputData() { return inputData; }
        public void setInputData(double[] inputData) { this.inputData = inputData; }
        
        public Object getModel() { return model; }
        public void setModel(Object model) { this.model = model; }
        
        public double[] getOriginalPrediction() { return originalPrediction; }
        public void setOriginalPrediction(double[] originalPrediction) { this.originalPrediction = originalPrediction; }
    }
    
    public static class ModelIntegrityRequest {
        private String modelId;
        private double[] inputData;
        private double[] outputData;
        private Map<String, Object> metadata;
        
        // Getters and Setters
        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
        
        public double[] getInputData() { return inputData; }
        public void setInputData(double[] inputData) { this.inputData = inputData; }
        
        public double[] getOutputData() { return outputData; }
        public void setOutputData(double[] outputData) { this.outputData = outputData; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}