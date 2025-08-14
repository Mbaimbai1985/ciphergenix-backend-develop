package com.ciphergenix.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "threat_detections")
public class ThreatDetection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatType threatType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel severityLevel;
    
    @Column(nullable = false)
    private String modelId;
    
    @Column(nullable = false)
    private String datasetId;
    
    @Column(nullable = false)
    private Double confidenceScore;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "threat_detection_metadata")
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;
    
    @Column(nullable = false)
    private LocalDateTime detectedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (detectedAt == null) {
            detectedAt = LocalDateTime.now();
        }
    }
    
    // Constructors
    public ThreatDetection() {}
    
    public ThreatDetection(ThreatType threatType, SeverityLevel severityLevel, 
                          String modelId, String datasetId, Double confidenceScore) {
        this.threatType = threatType;
        this.severityLevel = severityLevel;
        this.modelId = modelId;
        this.datasetId = datasetId;
        this.confidenceScore = confidenceScore;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ThreatType getThreatType() { return threatType; }
    public void setThreatType(ThreatType threatType) { this.threatType = threatType; }
    
    public SeverityLevel getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(SeverityLevel severityLevel) { this.severityLevel = severityLevel; }
    
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    
    public String getDatasetId() { return datasetId; }
    public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
    
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public enum ThreatType {
        DATA_POISONING,
        ADVERSARIAL_ATTACK,
        MODEL_THEFT,
        MODEL_TAMPERING,
        FEATURE_DRIFT,
        PERFORMANCE_DEGRADATION
    }
    
    public enum SeverityLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}