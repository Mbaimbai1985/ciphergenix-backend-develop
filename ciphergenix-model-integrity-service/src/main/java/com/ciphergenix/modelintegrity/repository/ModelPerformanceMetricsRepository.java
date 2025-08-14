package com.ciphergenix.modelintegrity.repository;

import com.ciphergenix.modelintegrity.model.ModelPerformanceMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ModelPerformanceMetricsRepository extends JpaRepository<ModelPerformanceMetrics, Long> {

    /**
     * Find latest metrics for a model
     */
    Optional<ModelPerformanceMetrics> findTopByModelIdOrderByMeasuredAtDesc(String modelId);

    /**
     * Find all metrics for a model ordered by measurement date
     */
    List<ModelPerformanceMetrics> findByModelIdOrderByMeasuredAtDesc(String modelId);

    /**
     * Find metrics within date range
     */
    @Query("SELECT mpm FROM ModelPerformanceMetrics mpm WHERE mpm.modelId = :modelId AND mpm.measuredAt BETWEEN :startDate AND :endDate ORDER BY mpm.measuredAt DESC")
    List<ModelPerformanceMetrics> findByModelIdAndDateRange(@Param("modelId") String modelId, 
                                                           @Param("startDate") LocalDateTime startDate, 
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find metrics with accuracy below threshold
     */
    @Query("SELECT mpm FROM ModelPerformanceMetrics mpm WHERE mpm.accuracy < :threshold ORDER BY mpm.measuredAt DESC")
    List<ModelPerformanceMetrics> findByAccuracyBelow(@Param("threshold") Double threshold);

    /**
     * Find metrics with high anomaly scores
     */
    @Query("SELECT mpm FROM ModelPerformanceMetrics mpm WHERE mpm.anomalyScore > :threshold ORDER BY mpm.anomalyScore DESC")
    List<ModelPerformanceMetrics> findByHighAnomalyScore(@Param("threshold") Double threshold);

    /**
     * Find metrics by status
     */
    List<ModelPerformanceMetrics> findByStatusOrderByMeasuredAtDesc(ModelPerformanceMetrics.MetricStatus status);

    /**
     * Find metrics with alerts triggered
     */
    List<ModelPerformanceMetrics> findByAlertTriggeredTrueOrderByMeasuredAtDesc();

    /**
     * Get average accuracy over time period
     */
    @Query("SELECT AVG(mpm.accuracy) FROM ModelPerformanceMetrics mpm WHERE mpm.modelId = :modelId AND mpm.measuredAt >= :since")
    Double getAverageAccuracy(@Param("modelId") String modelId, @Param("since") LocalDateTime since);

    /**
     * Get accuracy trend (last N measurements)
     */
    @Query("SELECT mpm.accuracy FROM ModelPerformanceMetrics mpm WHERE mpm.modelId = :modelId ORDER BY mpm.measuredAt DESC LIMIT :limit")
    List<Double> getAccuracyTrend(@Param("modelId") String modelId, @Param("limit") int limit);

    /**
     * Find models with performance degradation
     */
    @Query("SELECT DISTINCT mpm.modelId FROM ModelPerformanceMetrics mpm WHERE mpm.baselineComparison < :threshold")
    List<String> findModelsWithDegradation(@Param("threshold") Double threshold);

    /**
     * Get performance summary for all models
     */
    @Query("SELECT mpm.modelId, AVG(mpm.accuracy), AVG(mpm.confidenceScore), COUNT(*) FROM ModelPerformanceMetrics mpm WHERE mpm.measuredAt >= :since GROUP BY mpm.modelId")
    List<Object[]> getPerformanceSummary(@Param("since") LocalDateTime since);

    /**
     * Find latest metrics for multiple models
     */
    @Query("SELECT mpm FROM ModelPerformanceMetrics mpm WHERE (mpm.modelId, mpm.measuredAt) IN " +
           "(SELECT mpm2.modelId, MAX(mpm2.measuredAt) FROM ModelPerformanceMetrics mpm2 WHERE mpm2.modelId IN :modelIds GROUP BY mpm2.modelId)")
    List<ModelPerformanceMetrics> findLatestMetricsForModels(@Param("modelIds") List<String> modelIds);

    /**
     * Count measurements for a model
     */
    Long countByModelId(String modelId);

    /**
     * Delete old metrics (older than specified date)
     */
    void deleteByMeasuredAtBefore(LocalDateTime cutoffDate);

    /**
     * Find metrics with drift scores above threshold
     */
    @Query("SELECT mpm FROM ModelPerformanceMetrics mpm WHERE (mpm.dataDriftScore > :threshold OR mpm.modelDriftScore > :threshold) ORDER BY mpm.measuredAt DESC")
    List<ModelPerformanceMetrics> findWithHighDriftScores(@Param("threshold") Double threshold);
}