package com.ciphergenix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detection_results")
public class DetectionResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String detectionType;
    
    @Column(nullable = false)
    private Double threatScore;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatLevel threatLevel;
    
    @Column(columnDefinition = "TEXT")
    private String anomalousData;
    
    @ElementCollection
    @CollectionTable(name = "detection_metadata", joinColumns = @JoinColumn(name = "detection_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;
    
    @Column(nullable = false)
    private LocalDateTime detectedAt;
    
    @Column
    private String modelId;
    
    @Column
    private String datasetId;
    
    @PrePersist
    protected void onCreate() {
        detectedAt = LocalDateTime.now();
    }
}