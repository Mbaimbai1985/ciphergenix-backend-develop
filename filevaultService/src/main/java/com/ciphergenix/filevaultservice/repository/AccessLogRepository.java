package com.ciphergenix.filevaultservice.repository;

import com.ciphergenix.filevaultservice.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}