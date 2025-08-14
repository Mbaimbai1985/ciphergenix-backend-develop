package com.ciphergenix.service;

import com.ciphergenix.domain.ThreatDetection;
import com.ciphergenix.repository.ThreatDetectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ThreatDetectionService {
    
    @Autowired
    private ThreatDetectionRepository threatDetectionRepository;
    
    /**
     * Get all threat detections
     */
    public List<ThreatDetection> getAllThreatDetections() {
        return threatDetectionRepository.findAll();
    }
    
    /**
     * Get threat detections by model ID
     */
    public List<ThreatDetection> getThreatDetectionsByModel(String modelId) {
        return threatDetectionRepository.findByModelId(modelId);
    }
    
    /**
     * Get threat detections by type
     */
    public List<ThreatDetection> getThreatDetectionsByType(String threatType) {
        return threatDetectionRepository.findByThreatType(
            ThreatDetection.ThreatType.valueOf(threatType.toUpperCase())
        );
    }
    
    /**
     * Get threat detections by severity level
     */
    public List<ThreatDetection> getThreatDetectionsBySeverity(String severityLevel) {
        return threatDetectionRepository.findBySeverityLevel(
            ThreatDetection.SeverityLevel.valueOf(severityLevel.toUpperCase())
        );
    }
    
    /**
     * Get threat detections within time range
     */
    public List<ThreatDetection> getThreatDetectionsInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return threatDetectionRepository.findByDetectedAtBetween(startTime, endTime);
    }
    
    /**
     * Get threat statistics
     */
    public Map<String, Object> getThreatStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Total threats
        long totalThreats = threatDetectionRepository.count();
        statistics.put("totalThreats", totalThreats);
        
        // Threats by type
        Map<String, Long> threatsByType = Arrays.stream(ThreatDetection.ThreatType.values())
            .collect(Collectors.toMap(
                ThreatDetection.ThreatType::name,
                type -> threatDetectionRepository.countByThreatType(type)
            ));
        statistics.put("threatsByType", threatsByType);
        
        // Threats by severity
        Map<String, Long> threatsBySeverity = Arrays.stream(ThreatDetection.SeverityLevel.values())
            .collect(Collectors.toMap(
                ThreatDetection.SeverityLevel::name,
                severity -> threatDetectionRepository.countBySeverityLevel(severity)
            ));
        statistics.put("threatsBySeverity", threatsBySeverity);
        
        // Recent threats (last 24 hours)
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long recentThreats = threatDetectionRepository.countByDetectedAtAfter(last24Hours);
        statistics.put("recentThreats", recentThreats);
        
        // Average confidence score
        Double avgConfidence = threatDetectionRepository.findAverageConfidenceScore();
        statistics.put("averageConfidenceScore", avgConfidence != null ? avgConfidence : 0.0);
        
        // Top models by threat count
        List<Object[]> topModels = threatDetectionRepository.findTopModelsByThreatCount(10);
        List<Map<String, Object>> topModelsList = topModels.stream()
            .map(row -> {
                Map<String, Object> model = new HashMap<>();
                model.put("modelId", row[0]);
                model.put("threatCount", row[1]);
                return model;
            })
            .collect(Collectors.toList());
        statistics.put("topModelsByThreatCount", topModelsList);
        
        return statistics;
    }
    
    /**
     * Create new threat detection
     */
    public ThreatDetection createThreatDetection(ThreatDetection threatDetection) {
        // Set timestamps
        if (threatDetection.getDetectedAt() == null) {
            threatDetection.setDetectedAt(LocalDateTime.now());
        }
        
        // Validate threat detection
        validateThreatDetection(threatDetection);
        
        return threatDetectionRepository.save(threatDetection);
    }
    
    /**
     * Update threat detection
     */
    public ThreatDetection updateThreatDetection(ThreatDetection threatDetection) {
        // Check if exists
        if (!threatDetectionRepository.existsById(threatDetection.getId())) {
            throw new IllegalArgumentException("Threat detection not found with ID: " + threatDetection.getId());
        }
        
        // Validate threat detection
        validateThreatDetection(threatDetection);
        
        return threatDetectionRepository.save(threatDetection);
    }
    
    /**
     * Delete threat detection
     */
    public void deleteThreatDetection(Long id) {
        if (!threatDetectionRepository.existsById(id)) {
            throw new IllegalArgumentException("Threat detection not found with ID: " + id);
        }
        
        threatDetectionRepository.deleteById(id);
    }
    
    /**
     * Bulk create threat detections
     */
    public List<ThreatDetection> createBulkThreatDetections(List<ThreatDetection> threatDetections) {
        // Set timestamps and validate
        threatDetections.forEach(threat -> {
            if (threat.getDetectedAt() == null) {
                threat.setDetectedAt(LocalDateTime.now());
            }
            validateThreatDetection(threat);
        });
        
        return threatDetectionRepository.saveAll(threatDetections);
    }
    
    /**
     * Get threat detections with pagination
     */
    public Map<String, Object> getThreatDetectionsWithPagination(int page, int size, String sortBy, String sortDir) {
        // This would typically use Spring Data's Pageable
        // For now, return a simplified version
        List<ThreatDetection> allThreats = getAllThreatDetections();
        
        // Simple pagination
        int start = page * size;
        int end = Math.min(start + size, allThreats.size());
        
        List<ThreatDetection> pageContent = allThreats.subList(start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", pageContent);
        result.put("totalElements", allThreats.size());
        result.put("totalPages", (int) Math.ceil((double) allThreats.size() / size));
        result.put("currentPage", page);
        result.put("pageSize", size);
        
        return result;
    }
    
    /**
     * Search threat detections
     */
    public List<ThreatDetection> searchThreatDetections(String query, String modelId, 
                                                       String threatType, String severityLevel) {
        List<ThreatDetection> results = new ArrayList<>();
        
        // Simple search implementation
        List<ThreatDetection> allThreats = getAllThreats();
        
        for (ThreatDetection threat : allThreats) {
            boolean matches = true;
            
            // Query search
            if (query != null && !query.trim().isEmpty()) {
                boolean queryMatch = threat.getDescription() != null && 
                                   threat.getDescription().toLowerCase().contains(query.toLowerCase());
                if (!queryMatch) {
                    matches = false;
                }
            }
            
            // Model ID filter
            if (modelId != null && !modelId.trim().isEmpty()) {
                if (!threat.getModelId().equals(modelId)) {
                    matches = false;
                }
            }
            
            // Threat type filter
            if (threatType != null && !threatType.trim().isEmpty()) {
                if (!threat.getThreatType().name().equals(threatType.toUpperCase())) {
                    matches = false;
                }
            }
            
            // Severity level filter
            if (severityLevel != null && !severityLevel.trim().isEmpty()) {
                if (!threat.getSeverityLevel().name().equals(severityLevel.toUpperCase())) {
                    matches = false;
                }
            }
            
            if (matches) {
                results.add(threat);
            }
        }
        
        return results;
    }
    
    /**
     * Get threat trends over time
     */
    public Map<String, Object> getThreatTrends(int days) {
        Map<String, Object> trends = new HashMap<>();
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        List<ThreatDetection> threatsInRange = getThreatDetectionsInTimeRange(startTime, endTime);
        
        // Group by date
        Map<String, Long> threatsByDate = threatsInRange.stream()
            .collect(Collectors.groupingBy(
                threat -> threat.getDetectedAt().toLocalDate().toString(),
                Collectors.counting()
            ));
        
        trends.put("threatsByDate", threatsByDate);
        trends.put("totalThreats", threatsInRange.size());
        trends.put("startDate", startTime);
        trends.put("endDate", endTime);
        
        return trends;
    }
    
    /**
     * Validate threat detection
     */
    private void validateThreatDetection(ThreatDetection threatDetection) {
        if (threatDetection.getThreatType() == null) {
            throw new IllegalArgumentException("Threat type is required");
        }
        
        if (threatDetection.getSeverityLevel() == null) {
            throw new IllegalArgumentException("Severity level is required");
        }
        
        if (threatDetection.getModelId() == null || threatDetection.getModelId().trim().isEmpty()) {
            throw new IllegalArgumentException("Model ID is required");
        }
        
        if (threatDetection.getDatasetId() == null || threatDetection.getDatasetId().trim().isEmpty()) {
            throw new IllegalArgumentException("Dataset ID is required");
        }
        
        if (threatDetection.getConfidenceScore() == null || 
            threatDetection.getConfidenceScore() < 0.0 || 
            threatDetection.getConfidenceScore() > 1.0) {
            throw new IllegalArgumentException("Confidence score must be between 0.0 and 1.0");
        }
    }
    
    /**
     * Get all threats (helper method)
     */
    private List<ThreatDetection> getAllThreats() {
        return getAllThreatDetections();
    }
}