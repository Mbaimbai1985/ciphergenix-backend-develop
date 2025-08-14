package com.ciphergenix.modelintegrity.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "model_fingerprints")
public class ModelFingerprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_id", nullable = false)
    private String modelId;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "fingerprint_hash", nullable = false, unique = true)
    private String fingerprintHash;

    @ElementCollection
    @CollectionTable(name = "model_parameters", joinColumns = @JoinColumn(name = "fingerprint_id"))
    @MapKeyColumn(name = "parameter_name")
    @Column(name = "parameter_value")
    private Map<String, Double> parameters;

    @ElementCollection
    @CollectionTable(name = "model_weights_summary", joinColumns = @JoinColumn(name = "fingerprint_id"))
    @MapKeyColumn(name = "layer_name")
    @Column(name = "weight_hash")
    private Map<String, String> weightsHash;

    @Column(name = "architecture_hash")
    private String architectureHash;

    @Column(name = "performance_baseline", columnDefinition = "TEXT")
    private String performanceBaseline;

    @Column(name = "training_data_hash")
    private String trainingDataHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "integrity_score")
    private Double integrityScore;

    @Column(name = "last_verified")
    private LocalDateTime lastVerified;

    // Default constructor
    public ModelFingerprint() {
        this.createdAt = LocalDateTime.now();
        this.lastVerified = LocalDateTime.now();
    }

    // Constructor with essential fields
    public ModelFingerprint(String modelId, String modelName, String modelVersion, String fingerprintHash) {
        this();
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.fingerprintHash = fingerprintHash;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getFingerprintHash() {
        return fingerprintHash;
    }

    public void setFingerprintHash(String fingerprintHash) {
        this.fingerprintHash = fingerprintHash;
    }

    public Map<String, Double> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Double> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getWeightsHash() {
        return weightsHash;
    }

    public void setWeightsHash(Map<String, String> weightsHash) {
        this.weightsHash = weightsHash;
    }

    public String getArchitectureHash() {
        return architectureHash;
    }

    public void setArchitectureHash(String architectureHash) {
        this.architectureHash = architectureHash;
    }

    public String getPerformanceBaseline() {
        return performanceBaseline;
    }

    public void setPerformanceBaseline(String performanceBaseline) {
        this.performanceBaseline = performanceBaseline;
    }

    public String getTrainingDataHash() {
        return trainingDataHash;
    }

    public void setTrainingDataHash(String trainingDataHash) {
        this.trainingDataHash = trainingDataHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Double getIntegrityScore() {
        return integrityScore;
    }

    public void setIntegrityScore(Double integrityScore) {
        this.integrityScore = integrityScore;
    }

    public LocalDateTime getLastVerified() {
        return lastVerified;
    }

    public void setLastVerified(LocalDateTime lastVerified) {
        this.lastVerified = lastVerified;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ModelFingerprint{" +
                "id=" + id +
                ", modelId='" + modelId + '\'' +
                ", modelName='" + modelName + '\'' +
                ", modelVersion='" + modelVersion + '\'' +
                ", fingerprintHash='" + fingerprintHash + '\'' +
                ", integrityScore=" + integrityScore +
                ", createdAt=" + createdAt +
                '}';
    }
}