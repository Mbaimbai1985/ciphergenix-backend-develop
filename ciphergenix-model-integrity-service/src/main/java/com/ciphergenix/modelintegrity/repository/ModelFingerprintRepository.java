package com.ciphergenix.modelintegrity.repository;

import com.ciphergenix.modelintegrity.model.ModelFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ModelFingerprintRepository extends JpaRepository<ModelFingerprint, Long> {

    /**
     * Find fingerprint by model ID
     */
    Optional<ModelFingerprint> findByModelIdAndIsActiveTrue(String modelId);

    /**
     * Find all fingerprints for a model
     */
    List<ModelFingerprint> findByModelIdOrderByCreatedAtDesc(String modelId);

    /**
     * Find fingerprint by hash
     */
    Optional<ModelFingerprint> findByFingerprintHash(String fingerprintHash);

    /**
     * Find fingerprints that need verification
     */
    @Query("SELECT mf FROM ModelFingerprint mf WHERE mf.lastVerified < :threshold AND mf.isActive = true")
    List<ModelFingerprint> findFingerprintsNeedingVerification(@Param("threshold") LocalDateTime threshold);

    /**
     * Find fingerprints with low integrity scores
     */
    @Query("SELECT mf FROM ModelFingerprint mf WHERE mf.integrityScore < :minScore AND mf.isActive = true")
    List<ModelFingerprint> findFingerprintsWithLowIntegrity(@Param("minScore") Double minScore);

    /**
     * Find all active fingerprints
     */
    List<ModelFingerprint> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * Find fingerprints by model name and version
     */
    List<ModelFingerprint> findByModelNameAndModelVersionAndIsActiveTrue(String modelName, String modelVersion);

    /**
     * Count active fingerprints for a model
     */
    @Query("SELECT COUNT(mf) FROM ModelFingerprint mf WHERE mf.modelId = :modelId AND mf.isActive = true")
    Long countActiveByModelId(@Param("modelId") String modelId);

    /**
     * Find fingerprints created within date range
     */
    @Query("SELECT mf FROM ModelFingerprint mf WHERE mf.createdAt BETWEEN :startDate AND :endDate ORDER BY mf.createdAt DESC")
    List<ModelFingerprint> findFingerprintsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find similar fingerprints by architecture hash
     */
    List<ModelFingerprint> findByArchitectureHashAndIsActiveTrue(String architectureHash);

    /**
     * Update integrity score
     */
    @Query("UPDATE ModelFingerprint mf SET mf.integrityScore = :score, mf.lastVerified = :verifiedAt WHERE mf.id = :id")
    void updateIntegrityScore(@Param("id") Long id, @Param("score") Double score, @Param("verifiedAt") LocalDateTime verifiedAt);
}