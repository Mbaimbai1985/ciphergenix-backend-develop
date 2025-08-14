package com.ciphergenix.security;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SecureDataPipeline {
    
    @Value("${ciphergenix.security.encryption.key:default-encryption-key}")
    private String encryptionKey;
    
    @Value("${ciphergenix.security.encryption.salt:default-salt}")
    private String encryptionSalt;
    
    private final AesBytesEncryptor encryptor;
    private final SecureRandom secureRandom;
    private final Map<String, AccessControlEntry> accessControlList;
    private final List<AuditLogEntry> auditLog;
    
    public SecureDataPipeline() {
        this.secureRandom = new SecureRandom();
        this.accessControlList = new ConcurrentHashMap<>();
        this.auditLog = Collections.synchronizedList(new ArrayList<>());
        
        // Initialize AES encryptor
        byte[] key = KeyGenerators.secureRandom(32).generateKey();
        byte[] salt = KeyGenerators.secureRandom(16).generateKey();
        this.encryptor = new AesBytesEncryptor(key, salt);
    }
    
    /**
     * Secure data ingestion with encryption, integrity verification, and access control
     */
    public SecuredData secureDataIngestion(DataIngestionRequest request) {
        // 1. Access Control Enforcement
        if (!enforceAccessControl(request.getUserId(), request.getDataType(), request.getOperation())) {
            logAuditEvent(request.getUserId(), "ACCESS_DENIED", request.getDataType(), 
                         "Access control violation for data ingestion");
            throw new SecurityException("Access denied for data ingestion");
        }
        
        // 2. Data Encryption
        byte[] encryptedData = encryptData(request.getData());
        
        // 3. Integrity Verification
        String dataHash = calculateDataHash(request.getData());
        String hmacSignature = calculateHMAC(request.getData(), encryptionKey);
        
        // 4. Create secured data object
        SecuredData securedData = new SecuredData();
        securedData.setEncryptedData(encryptedData);
        securedData.setDataHash(dataHash);
        securedData.setHmacSignature(hmacSignature);
        securedData.setEncryptionTimestamp(LocalDateTime.now());
        securedData.setOriginalSize(request.getData().length);
        
        // 5. Audit Logging
        logAuditEvent(request.getUserId(), "DATA_ENCRYPTED", request.getDataType(), 
                     "Data successfully encrypted and secured");
        
        return securedData;
    }
    
    /**
     * Decrypt and verify data integrity
     */
    public byte[] secureDataRetrieval(DataRetrievalRequest request) {
        // 1. Access Control Enforcement
        if (!enforceAccessControl(request.getUserId(), request.getDataType(), "READ")) {
            logAuditEvent(request.getUserId(), "ACCESS_DENIED", request.getDataType(), 
                         "Access control violation for data retrieval");
            throw new SecurityException("Access denied for data retrieval");
        }
        
        // 2. Decrypt data
        byte[] decryptedData = decryptData(request.getEncryptedData());
        
        // 3. Verify integrity
        if (!verifyDataIntegrity(decryptedData, request.getDataHash(), request.getHmacSignature())) {
            logAuditEvent(request.getUserId(), "INTEGRITY_VIOLATION", request.getDataType(), 
                         "Data integrity verification failed");
            throw new SecurityException("Data integrity verification failed");
        }
        
        // 4. Audit Logging
        logAuditEvent(request.getUserId(), "DATA_DECRYPTED", request.getDataType(), 
                     "Data successfully decrypted and verified");
        
        return decryptedData;
    }
    
    /**
     * Role-based access control enforcement
     */
    private boolean enforceAccessControl(String userId, String dataType, String operation) {
        AccessControlEntry entry = accessControlList.get(userId);
        if (entry == null) {
            // Default access control - deny by default
            return false;
        }
        
        // Check role-based permissions
        Set<String> allowedOperations = entry.getAllowedOperations().get(dataType);
        if (allowedOperations == null) {
            return false;
        }
        
        return allowedOperations.contains(operation) || allowedOperations.contains("ALL");
    }
    
    /**
     * Data encryption using AES
     */
    private byte[] encryptData(byte[] data) {
        try {
            return encryptor.encrypt(data);
        } catch (Exception e) {
            throw new SecurityException("Failed to encrypt data", e);
        }
    }
    
    /**
     * Data decryption using AES
     */
    private byte[] decryptData(byte[] encryptedData) {
        try {
            return encryptor.decrypt(encryptedData);
        } catch (Exception e) {
            throw new SecurityException("Failed to decrypt data", e);
        }
    }
    
    /**
     * Calculate SHA-256 hash for data integrity
     */
    private String calculateDataHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return new String(Hex.encode(hash));
        } catch (Exception e) {
            throw new SecurityException("Failed to calculate data hash", e);
        }
    }
    
    /**
     * Calculate HMAC-SHA256 for data authentication
     */
    private String calculateHMAC(byte[] data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(data);
            return new String(Hex.encode(hmac));
        } catch (Exception e) {
            throw new SecurityException("Failed to calculate HMAC", e);
        }
    }
    
    /**
     * Verify data integrity using hash and HMAC
     */
    private boolean verifyDataIntegrity(byte[] data, String expectedHash, String expectedHmac) {
        String actualHash = calculateDataHash(data);
        String actualHmac = calculateHMAC(data, encryptionKey);
        
        return actualHash.equals(expectedHash) && actualHmac.equals(expectedHmac);
    }
    
    /**
     * Audit logging for security events
     */
    private void logAuditEvent(String userId, String eventType, String dataType, String description) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setUserId(userId);
        entry.setEventType(eventType);
        entry.setDataType(dataType);
        entry.setDescription(description);
        entry.setTimestamp(LocalDateTime.now());
        entry.setIpAddress("127.0.0.1"); // In real implementation, get from request context
        
        auditLog.add(entry);
        
        // Keep audit log size manageable
        if (auditLog.size() > 10000) {
            auditLog.remove(0);
        }
    }
    
    /**
     * Add access control entry for a user
     */
    public void addAccessControlEntry(String userId, String dataType, Set<String> allowedOperations) {
        AccessControlEntry entry = new AccessControlEntry();
        entry.setUserId(userId);
        entry.setDataType(dataType);
        entry.setAllowedOperations(allowedOperations);
        entry.setCreatedAt(LocalDateTime.now());
        
        accessControlList.put(userId + ":" + dataType, entry);
    }
    
    /**
     * Get audit log entries
     */
    public List<AuditLogEntry> getAuditLog(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLog.stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .filter(entry -> entry.getTimestamp().isAfter(startTime) && entry.getTimestamp().isBefore(endTime))
                .toList();
    }
    
    /**
     * Generate secure random key
     */
    public String generateSecureKey() {
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return new String(Hex.encode(key));
    }
    
    // Data transfer objects
    public static class DataIngestionRequest {
        private String userId;
        private String dataType;
        private String operation;
        private byte[] data;
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public byte[] getData() { return data; }
        public void setData(byte[] data) { this.data = data; }
    }
    
    public static class DataRetrievalRequest {
        private String userId;
        private String dataType;
        private byte[] encryptedData;
        private String dataHash;
        private String hmacSignature;
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        
        public byte[] getEncryptedData() { return encryptedData; }
        public void setEncryptedData(byte[] encryptedData) { this.encryptedData = encryptedData; }
        
        public String getDataHash() { return dataHash; }
        public void setDataHash(String dataHash) { this.dataHash = dataHash; }
        
        public String getHmacSignature() { return hmacSignature; }
        public void setHmacSignature(String hmacSignature) { this.hmacSignature = hmacSignature; }
    }
    
    public static class SecuredData {
        private byte[] encryptedData;
        private String dataHash;
        private String hmacSignature;
        private LocalDateTime encryptionTimestamp;
        private int originalSize;
        
        // Getters and Setters
        public byte[] getEncryptedData() { return encryptedData; }
        public void setEncryptedData(byte[] encryptedData) { this.encryptedData = encryptedData; }
        
        public String getDataHash() { return dataHash; }
        public void setDataHash(String dataHash) { this.dataHash = dataHash; }
        
        public String getHmacSignature() { return hmacSignature; }
        public void setHmacSignature(String hmacSignature) { this.hmacSignature = hmacSignature; }
        
        public LocalDateTime getEncryptionTimestamp() { return encryptionTimestamp; }
        public void setEncryptionTimestamp(LocalDateTime encryptionTimestamp) { this.encryptionTimestamp = encryptionTimestamp; }
        
        public int getOriginalSize() { return originalSize; }
        public void setOriginalSize(int originalSize) { this.originalSize = originalSize; }
    }
    
    public static class AccessControlEntry {
        private String userId;
        private String dataType;
        private Set<String> allowedOperations;
        private LocalDateTime createdAt;
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        
        public Set<String> getAllowedOperations() { return allowedOperations; }
        public void setAllowedOperations(Set<String> allowedOperations) { this.allowedOperations = allowedOperations; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    public static class AuditLogEntry {
        private String userId;
        private String eventType;
        private String dataType;
        private String description;
        private LocalDateTime timestamp;
        private String ipAddress;
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    }
}