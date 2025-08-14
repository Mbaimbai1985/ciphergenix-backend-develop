package com.ciphergenix.repository;

import com.ciphergenix.model.DetectionResult;
import com.ciphergenix.model.ThreatLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetectionResultRepository extends JpaRepository<DetectionResult, Long> {
    
    Page<DetectionResult> findByDetectionType(String detectionType, Pageable pageable);
    
    Page<DetectionResult> findByThreatLevel(ThreatLevel threatLevel, Pageable pageable);
    
    Page<DetectionResult> findByDetectedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT d FROM DetectionResult d WHERE d.threatScore >= :minScore")
    List<DetectionResult> findHighThreatDetections(@Param("minScore") Double minScore);
    
    @Query("SELECT d FROM DetectionResult d WHERE d.modelId = :modelId ORDER BY d.detectedAt DESC")
    List<DetectionResult> findByModelId(@Param("modelId") String modelId, Pageable pageable);
    
    @Query("SELECT d.detectionType, COUNT(d) FROM DetectionResult d " +
           "WHERE d.detectedAt >= :since " +
           "GROUP BY d.detectionType")
    List<Object[]> getDetectionTypeStats(@Param("since") LocalDateTime since);
    
    @Query("SELECT d.threatLevel, COUNT(d) FROM DetectionResult d " +
           "WHERE d.detectedAt >= :since " +
           "GROUP BY d.threatLevel")
    List<Object[]> getThreatLevelStats(@Param("since") LocalDateTime since);
}