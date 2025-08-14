package com.ciphergenix.controller;

import com.ciphergenix.dto.*;
import com.ciphergenix.model.DetectionResult;
import com.ciphergenix.service.ThreatDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/threat-detection")
@Tag(name = "Threat Detection", description = "AI Security Threat Detection APIs")
@Validated
public class ThreatDetectionController {
    
    @Autowired
    private ThreatDetectionService threatDetectionService;
    
    @PostMapping("/data-poisoning/detect")
    @Operation(summary = "Detect data poisoning in training dataset")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detection completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DetectionResponse> detectDataPoisoning(
            @Valid @RequestBody DatasetAnalysisRequest request) {
        
        log.info("Received data poisoning detection request for dataset: {}", request.getDatasetId());
        
        try {
            DetectionResponse response = threatDetectionService.detectDataPoisoning(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in data poisoning detection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/adversarial/detect")
    @Operation(summary = "Detect adversarial attacks on model inputs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detection completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DetectionResponse> detectAdversarialAttack(
            @Valid @RequestBody AdversarialDetectionRequest request) {
        
        log.info("Received adversarial detection request for model: {}", request.getModelId());
        
        try {
            DetectionResponse response = threatDetectionService.detectAdversarialAttack(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in adversarial attack detection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/model-integrity/monitor")
    @Operation(summary = "Monitor model integrity and detect tampering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Monitoring completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DetectionResponse> monitorModelIntegrity(
            @Valid @RequestBody ModelMonitoringRequest request) {
        
        log.info("Received model integrity monitoring request for model: {}", request.getModelId());
        
        try {
            DetectionResponse response = threatDetectionService.monitorModelIntegrity(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in model integrity monitoring", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/history")
    @Operation(summary = "Get detection history with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<DetectionResult>> getDetectionHistory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "detectedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<DetectionResult> history = threatDetectionService.getDetectionHistory(pageable);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error retrieving detection history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/history/by-type/{detectionType}")
    @Operation(summary = "Get detection history filtered by type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<DetectionResult>> getDetectionsByType(
            @PathVariable String detectionType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "detectedAt"));
            Page<DetectionResult> history = threatDetectionService.getDetectionsByType(detectionType, pageable);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error retrieving detection history by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get threat detection statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getDetectionStatistics(
            @Parameter(description = "Start date for statistics")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        
        try {
            if (since == null) {
                since = LocalDateTime.now().minusDays(7); // Default to last 7 days
            }
            
            Map<String, Object> statistics = threatDetectionService.getDetectionStatistics(since);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error retrieving detection statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Check API health status")
    @ApiResponse(responseCode = "200", description = "API is healthy")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = Map.of(
            "status", "UP",
            "service", "ThreatDetectionAPI",
            "timestamp", LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(health);
    }
}