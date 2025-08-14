package com.ciphergenix.repository;

import com.ciphergenix.domain.ThreatDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ThreatDetectionRepository extends JpaRepository<ThreatDetection, Long> {
    
    /**
     * Find threat detections by model ID
     */
    List<ThreatDetection> findByModelId(String modelId);
    
    /**
     * Find threat detections by threat type
     */
    List<ThreatDetection> findByThreatType(ThreatDetection.ThreatType threatType);
    
    /**
     * Find threat detections by severity level
     */
    List<ThreatDetection> findBySeverityLevel(ThreatDetection.SeverityLevel severityLevel);
    
    /**
     * Find threat detections by dataset ID
     */
    List<ThreatDetection> findByDatasetId(String datasetId);
    
    /**
     * Find threat detections detected after a specific time
     */
    List<ThreatDetection> findByDetectedAtAfter(LocalDateTime time);
    
    /**
     * Find threat detections detected before a specific time
     */
    List<ThreatDetection> findByDetectedAtBefore(LocalDateTime time);
    
    /**
     * Find threat detections within a time range
     */
    List<ThreatDetection> findByDetectedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find threat detections by confidence score greater than threshold
     */
    List<ThreatDetection> findByConfidenceScoreGreaterThan(Double threshold);
    
    /**
     * Find threat detections by confidence score less than threshold
     */
    List<ThreatDetection> findByConfidenceScoreLessThan(Double threshold);
    
    /**
     * Find threat detections by model ID and threat type
     */
    List<ThreatDetection> findByModelIdAndThreatType(String modelId, ThreatDetection.ThreatType threatType);
    
    /**
     * Find threat detections by model ID and severity level
     */
    List<ThreatDetection> findByModelIdAndSeverityLevel(String modelId, ThreatDetection.SeverityLevel severityLevel);
    
    /**
     * Find threat detections by threat type and severity level
     */
    List<ThreatDetection> findByThreatTypeAndSeverityLevel(ThreatDetection.ThreatType threatType, 
                                                          ThreatDetection.SeverityLevel severityLevel);
    
    /**
     * Count threat detections by model ID
     */
    long countByModelId(String modelId);
    
    /**
     * Count threat detections by threat type
     */
    long countByThreatType(ThreatDetection.ThreatType threatType);
    
    /**
     * Count threat detections by severity level
     */
    long countBySeverityLevel(ThreatDetection.SeverityLevel severityLevel);
    
    /**
     * Count threat detections by dataset ID
     */
    long countByDatasetId(String datasetId);
    
    /**
     * Count threat detections detected after a specific time
     */
    long countByDetectedAtAfter(LocalDateTime time);
    
    /**
     * Count threat detections detected before a specific time
     */
    long countByDetectedAtBefore(LocalDateTime time);
    
    /**
     * Count threat detections within a time range
     */
    long countByDetectedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Count threat detections by confidence score greater than threshold
     */
    long countByConfidenceScoreGreaterThan(Double threshold);
    
    /**
     * Count threat detections by confidence score less than threshold
     */
    long countByConfidenceScoreLessThan(Double threshold);
    
    /**
     * Find average confidence score
     */
    @Query("SELECT AVG(t.confidenceScore) FROM ThreatDetection t")
    Double findAverageConfidenceScore();
    
    /**
     * Find maximum confidence score
     */
    @Query("SELECT MAX(t.confidenceScore) FROM ThreatDetection t")
    Double findMaxConfidenceScore();
    
    /**
     * Find minimum confidence score
     */
    @Query("SELECT MIN(t.confidenceScore) FROM ThreatDetection t")
    Double findMinConfidenceScore();
    
    /**
     * Find top models by threat count
     */
    @Query("SELECT t.modelId, COUNT(t) as threatCount FROM ThreatDetection t GROUP BY t.modelId ORDER BY threatCount DESC")
    List<Object[]> findTopModelsByThreatCount(int limit);
    
    /**
     * Find threat detections with high confidence (above threshold)
     */
    @Query("SELECT t FROM ThreatDetection t WHERE t.confidenceScore >= :threshold ORDER BY t.confidenceScore DESC")
    List<ThreatDetection> findHighConfidenceThreats(@Param("threshold") Double threshold);
    
    /**
     * Find recent threat detections
     */
    @Query("SELECT t FROM ThreatDetection t WHERE t.detectedAt >= :since ORDER BY t.detectedAt DESC")
    List<ThreatDetection> findRecentThreats(@Param("since") LocalDateTime since);
    
    /**
     * Find threat detections by multiple criteria
     */
    @Query("SELECT t FROM ThreatDetection t WHERE " +
           "(:modelId IS NULL OR t.modelId = :modelId) AND " +
           "(:threatType IS NULL OR t.threatType = :threatType) AND " +
           "(:severityLevel IS NULL OR t.severityLevel = :severityLevel) AND " +
           "(:minConfidence IS NULL OR t.confidenceScore >= :minConfidence) AND " +
           "(:maxConfidence IS NULL OR t.confidenceScore <= :maxConfidence) AND " +
           "(:startTime IS NULL OR t.detectedAt >= :startTime) AND " +
           "(:endTime IS NULL OR t.detectedAt <= :endTime) " +
           "ORDER BY t.detectedAt DESC")
    List<ThreatDetection> findThreatsByCriteria(
        @Param("modelId") String modelId,
        @Param("threatType") ThreatDetection.ThreatType threatType,
        @Param("severityLevel") ThreatDetection.SeverityLevel severityLevel,
        @Param("minConfidence") Double minConfidence,
        @Param("maxConfidence") Double maxConfidence,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find threat statistics by time period
     */
    @Query("SELECT DATE(t.detectedAt) as date, COUNT(t) as count, " +
           "AVG(t.confidenceScore) as avgConfidence " +
           "FROM ThreatDetection t " +
           "WHERE t.detectedAt BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(t.detectedAt) " +
           "ORDER BY date")
    List<Object[]> findThreatStatisticsByDate(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find threat patterns
     */
    @Query("SELECT t.threatType, t.severityLevel, COUNT(t) as count " +
           "FROM ThreatDetection t " +
           "GROUP BY t.threatType, t.severityLevel " +
           "ORDER BY count DESC")
    List<Object[]> findThreatPatterns();
    
    /**
     * Find models with most threats
     */
    @Query("SELECT t.modelId, COUNT(t) as threatCount, " +
           "AVG(t.confidenceScore) as avgConfidence " +
           "FROM ThreatDetection t " +
           "GROUP BY t.modelId " +
           "HAVING COUNT(t) > :minThreatCount " +
           "ORDER BY threatCount DESC")
    List<Object[]> findModelsWithMostThreats(@Param("minThreatCount") long minThreatCount);
    
    /**
     * Find threat detections by description containing text
     */
    @Query("SELECT t FROM ThreatDetection t WHERE t.description LIKE %:text%")
    List<ThreatDetection> findByDescriptionContaining(@Param("text") String text);
    
    /**
     * Find threat detections by metadata key-value pair
     */
    @Query("SELECT t FROM ThreatDetection t JOIN t.metadata m WHERE KEY(m) = :key AND VALUE(m) = :value")
    List<ThreatDetection> findByMetadataKeyValue(@Param("key") String key, @Param("value") String value);
}