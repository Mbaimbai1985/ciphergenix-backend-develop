package com.ciphergenix.service;

import com.ciphergenix.dto.*;
import com.ciphergenix.model.DetectionResult;
import com.ciphergenix.repository.DetectionResultRepository;
import com.ciphergenix.security.detection.AdversarialDetector;
import com.ciphergenix.security.detection.DataPoisoningDetector;
import com.ciphergenix.security.engine.RealTimeThreatMonitor;
import com.ciphergenix.security.monitoring.ModelIntegrityMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ThreatDetectionService {
    
    @Autowired
    private DataPoisoningDetector dataPoisoningDetector;
    
    @Autowired
    private AdversarialDetector adversarialDetector;
    
    @Autowired
    private ModelIntegrityMonitor modelIntegrityMonitor;
    
    @Autowired
    private RealTimeThreatMonitor realTimeThreatMonitor;
    
    @Autowired
    private DetectionResultRepository detectionResultRepository;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public DetectionResponse detectDataPoisoning(DatasetAnalysisRequest request) {
        log.info("Processing data poisoning detection request for dataset: {}", request.getDatasetId());
        
        try {
            // Perform detection
            DetectionResponse response = dataPoisoningDetector.detectPoisoning(request);
            
            // Save result to database
            saveDetectionResult(response, request.getModelId(), request.getDatasetId());
            
            // Send to real-time monitoring
            realTimeThreatMonitor.processThreatDetection(response);
            
            // Send to Kafka
            if (kafkaTemplate != null) {
                kafkaTemplate.send("threat-detection-topic", response);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error in data poisoning detection", e);
            throw new RuntimeException("Failed to detect data poisoning", e);
        }
    }
    
    public DetectionResponse detectAdversarialAttack(AdversarialDetectionRequest request) {
        log.info("Processing adversarial attack detection for model: {}", request.getModelId());
        
        try {
            // Perform detection
            DetectionResponse response = adversarialDetector.detectAdversarial(request);
            
            // Save result to database
            saveDetectionResult(response, request.getModelId(), null);
            
            // Send to real-time monitoring
            realTimeThreatMonitor.processThreatDetection(response);
            
            // Send to Kafka
            if (kafkaTemplate != null) {
                kafkaTemplate.send("threat-detection-topic", response);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error in adversarial attack detection", e);
            throw new RuntimeException("Failed to detect adversarial attack", e);
        }
    }
    
    public DetectionResponse monitorModelIntegrity(ModelMonitoringRequest request) {
        log.info("Processing model integrity monitoring for model: {}", request.getModelId());
        
        try {
            // Perform monitoring
            DetectionResponse response = modelIntegrityMonitor.monitorModelIntegrity(request);
            
            // Save result to database
            saveDetectionResult(response, request.getModelId(), null);
            
            // Send to real-time monitoring
            realTimeThreatMonitor.processThreatDetection(response);
            
            // Send to Kafka
            if (kafkaTemplate != null) {
                kafkaTemplate.send("model-monitoring-topic", response);
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("Error in model integrity monitoring", e);
            throw new RuntimeException("Failed to monitor model integrity", e);
        }
    }
    
    private void saveDetectionResult(DetectionResponse response, String modelId, String datasetId) {
        DetectionResult result = DetectionResult.builder()
            .detectionType(response.getDetectionType())
            .threatScore(response.getThreatScore())
            .threatLevel(response.getThreatLevel())
            .anomalousData(convertAnomalousDataToJson(response.getAnomalousSamples()))
            .metadata(convertDetailsToMetadata(response.getDetectionDetails()))
            .modelId(modelId)
            .datasetId(datasetId)
            .build();
        
        detectionResultRepository.save(result);
    }
    
    private String convertAnomalousDataToJson(List<DetectionResponse.AnomalySample> samples) {
        // In production, use proper JSON serialization
        if (samples == null || samples.isEmpty()) {
            return "[]";
        }
        return samples.toString();
    }
    
    private Map<String, String> convertDetailsToMetadata(Map<String, Object> details) {
        if (details == null) {
            return new HashMap<>();
        }
        
        return details.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> String.valueOf(entry.getValue())
            ));
    }
    
    // Query methods
    public Page<DetectionResult> getDetectionHistory(Pageable pageable) {
        return detectionResultRepository.findAll(pageable);
    }
    
    public Page<DetectionResult> getDetectionsByType(String detectionType, Pageable pageable) {
        return detectionResultRepository.findByDetectionType(detectionType, pageable);
    }
    
    public Map<String, Object> getDetectionStatistics(LocalDateTime since) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get detection type statistics
        List<Object[]> typeStats = detectionResultRepository.getDetectionTypeStats(since);
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeStats) {
            typeMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("detectionTypes", typeMap);
        
        // Get threat level statistics
        List<Object[]> levelStats = detectionResultRepository.getThreatLevelStats(since);
        Map<String, Long> levelMap = new HashMap<>();
        for (Object[] row : levelStats) {
            levelMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("threatLevels", levelMap);
        
        // Get high threat detections
        List<DetectionResult> highThreats = detectionResultRepository.findHighThreatDetections(0.8);
        stats.put("highThreatCount", highThreats.size());
        
        return stats;
    }
}