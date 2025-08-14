package com.ciphergenix.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "ai_models")
public class AIModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String modelId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String version;
    
    @Column(nullable = false)
    private String modelType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String ownerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModelStatus status;
    
    @Column(nullable = false)
    private LocalDateTime deployedAt;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @ElementCollection
    @CollectionTable(name = "model_metadata")
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;
    
    @Column
    private String modelHash;
    
    @Column
    private String baselinePerformance;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (deployedAt == null) {
            deployedAt = LocalDateTime.now();
        }
        if (lastUpdatedAt == null) {
            lastUpdatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public AIModel() {}
    
    public AIModel(String modelId, String name, String version, String modelType, String ownerId) {
        this.modelId = modelId;
        this.name = name;
        this.version = version;
        this.modelType = modelType;
        this.ownerId = ownerId;
        this.status = ModelStatus.ACTIVE;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    public ModelStatus getStatus() { return status; }
    public void setStatus(ModelStatus status) { this.status = status; }
    
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }
    
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    
    public String getModelHash() { return modelHash; }
    public void setModelHash(String modelHash) { this.modelHash = modelHash; }
    
    public String getBaselinePerformance() { return baselinePerformance; }
    public void setBaselinePerformance(String baselinePerformance) { this.baselinePerformance = baselinePerformance; }
    
    public enum ModelStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        DEPRECATED
    }
}