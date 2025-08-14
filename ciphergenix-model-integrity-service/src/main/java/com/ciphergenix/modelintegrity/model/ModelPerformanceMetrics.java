package com.ciphergenix.modelintegrity.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "model_performance_metrics")
public class ModelPerformanceMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_id", nullable = false)
    private String modelId;

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "precision_score")
    private Double precision;

    @Column(name = "recall")
    private Double recall;

    @Column(name = "f1_score")
    private Double f1Score;

    @Column(name = "auc_roc")
    private Double aucRoc;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "inference_latency_ms")
    private Long inferenceLatencyMs;

    @Column(name = "throughput_rps")
    private Double throughputRps;

    @Column(name = "memory_usage_mb")
    private Long memoryUsageMb;

    @Column(name = "cpu_usage_percent")
    private Double cpuUsagePercent;

    @Column(name = "gpu_usage_percent")
    private Double gpuUsagePercent;

    @ElementCollection
    @CollectionTable(name = "model_custom_metrics", joinColumns = @JoinColumn(name = "metrics_id"))
    @MapKeyColumn(name = "metric_name")
    @Column(name = "metric_value")
    private Map<String, Double> customMetrics;

    @ElementCollection
    @CollectionTable(name = "model_class_metrics", joinColumns = @JoinColumn(name = "metrics_id"))
    @MapKeyColumn(name = "class_name")
    @Column(name = "class_accuracy")
    private Map<String, Double> perClassAccuracy;

    @Column(name = "data_drift_score")
    private Double dataDriftScore;

    @Column(name = "model_drift_score")
    private Double modelDriftScore;

    @Column(name = "prediction_consistency")
    private Double predictionConsistency;

    @Column(name = "decision_boundary_stability")
    private Double decisionBoundaryStability;

    @Column(name = "sample_size")
    private Long sampleSize;

    @Column(name = "evaluation_dataset")
    private String evaluationDataset;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Column(name = "baseline_comparison")
    private Double baselineComparison;

    @Column(name = "anomaly_score")
    private Double anomalyScore;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MetricStatus status = MetricStatus.NORMAL;

    @Column(name = "alert_triggered")
    private Boolean alertTriggered = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum MetricStatus {
        NORMAL, WARNING, CRITICAL, DEGRADED, IMPROVING
    }

    // Default constructor
    public ModelPerformanceMetrics() {
        this.measuredAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public ModelPerformanceMetrics(String modelId, Double accuracy, Double confidenceScore) {
        this();
        this.modelId = modelId;
        this.accuracy = accuracy;
        this.confidenceScore = confidenceScore;
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

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public Double getF1Score() {
        return f1Score;
    }

    public void setF1Score(Double f1Score) {
        this.f1Score = f1Score;
    }

    public Double getAucRoc() {
        return aucRoc;
    }

    public void setAucRoc(Double aucRoc) {
        this.aucRoc = aucRoc;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Long getInferenceLatencyMs() {
        return inferenceLatencyMs;
    }

    public void setInferenceLatencyMs(Long inferenceLatencyMs) {
        this.inferenceLatencyMs = inferenceLatencyMs;
    }

    public Double getThroughputRps() {
        return throughputRps;
    }

    public void setThroughputRps(Double throughputRps) {
        this.throughputRps = throughputRps;
    }

    public Long getMemoryUsageMb() {
        return memoryUsageMb;
    }

    public void setMemoryUsageMb(Long memoryUsageMb) {
        this.memoryUsageMb = memoryUsageMb;
    }

    public Double getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(Double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public Double getGpuUsagePercent() {
        return gpuUsagePercent;
    }

    public void setGpuUsagePercent(Double gpuUsagePercent) {
        this.gpuUsagePercent = gpuUsagePercent;
    }

    public Map<String, Double> getCustomMetrics() {
        return customMetrics;
    }

    public void setCustomMetrics(Map<String, Double> customMetrics) {
        this.customMetrics = customMetrics;
    }

    public Map<String, Double> getPerClassAccuracy() {
        return perClassAccuracy;
    }

    public void setPerClassAccuracy(Map<String, Double> perClassAccuracy) {
        this.perClassAccuracy = perClassAccuracy;
    }

    public Double getDataDriftScore() {
        return dataDriftScore;
    }

    public void setDataDriftScore(Double dataDriftScore) {
        this.dataDriftScore = dataDriftScore;
    }

    public Double getModelDriftScore() {
        return modelDriftScore;
    }

    public void setModelDriftScore(Double modelDriftScore) {
        this.modelDriftScore = modelDriftScore;
    }

    public Double getPredictionConsistency() {
        return predictionConsistency;
    }

    public void setPredictionConsistency(Double predictionConsistency) {
        this.predictionConsistency = predictionConsistency;
    }

    public Double getDecisionBoundaryStability() {
        return decisionBoundaryStability;
    }

    public void setDecisionBoundaryStability(Double decisionBoundaryStability) {
        this.decisionBoundaryStability = decisionBoundaryStability;
    }

    public Long getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(Long sampleSize) {
        this.sampleSize = sampleSize;
    }

    public String getEvaluationDataset() {
        return evaluationDataset;
    }

    public void setEvaluationDataset(String evaluationDataset) {
        this.evaluationDataset = evaluationDataset;
    }

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }

    public Double getBaselineComparison() {
        return baselineComparison;
    }

    public void setBaselineComparison(Double baselineComparison) {
        this.baselineComparison = baselineComparison;
    }

    public Double getAnomalyScore() {
        return anomalyScore;
    }

    public void setAnomalyScore(Double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }

    public MetricStatus getStatus() {
        return status;
    }

    public void setStatus(MetricStatus status) {
        this.status = status;
    }

    public Boolean getAlertTriggered() {
        return alertTriggered;
    }

    public void setAlertTriggered(Boolean alertTriggered) {
        this.alertTriggered = alertTriggered;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "ModelPerformanceMetrics{" +
                "id=" + id +
                ", modelId='" + modelId + '\'' +
                ", accuracy=" + accuracy +
                ", confidenceScore=" + confidenceScore +
                ", status=" + status +
                ", measuredAt=" + measuredAt +
                '}';
    }
}