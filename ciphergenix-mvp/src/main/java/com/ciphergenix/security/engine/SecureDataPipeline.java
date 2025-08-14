package com.ciphergenix.security.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SecureDataPipeline {
    
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;
    
    private final EncryptionHandler encryptionHandler;
    private final IntegrityChecker integrityChecker;
    private final RoleBasedAccessControl accessController;
    private final AuditLogger auditLogger;
    
    @Value("${security.encryption.master-key:#{null}}")
    private String masterKeyConfig;
    
    public SecureDataPipeline() {
        this.encryptionHandler = new EncryptionHandler();
        this.integrityChecker = new IntegrityChecker();
        this.accessController = new RoleBasedAccessControl();
        this.auditLogger = new AuditLogger();
    }
    
    public SecuredData secureDataIngestion(DataIngestionRequest request) {
        log.info("Starting secure data ingestion for source: {}", request.getDataSourceId());
        
        try {
            // 1. Access control enforcement
            if (!accessController.checkAccess(request.getUserId(), request.getDataSourceId(), "WRITE")) {
                auditLogger.logAccessDenied(request.getUserId(), request.getDataSourceId());
                throw new SecurityException("Access denied for user: " + request.getUserId());
            }
            
            // 2. Data encryption at rest and in transit
            EncryptedData encryptedData = encryptionHandler.encrypt(request.getData());
            
            // 3. Integrity verification
            String integrityHash = integrityChecker.generateHash(encryptedData);
            
            // 4. Audit logging
            auditLogger.logDataIngestion(request.getUserId(), request.getDataSourceId(), integrityHash);
            
            return SecuredData.builder()
                .dataId(UUID.randomUUID().toString())
                .encryptedData(encryptedData)
                .integrityHash(integrityHash)
                .metadata(createMetadata(request))
                .timestamp(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error in secure data ingestion", e);
            auditLogger.logError("DATA_INGESTION_ERROR", request.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to secure data ingestion", e);
        }
    }
    
    public byte[] retrieveSecuredData(DataRetrievalRequest request) {
        log.info("Retrieving secured data: {}", request.getDataId());
        
        try {
            // 1. Access control check
            if (!accessController.checkAccess(request.getUserId(), request.getDataId(), "READ")) {
                auditLogger.logAccessDenied(request.getUserId(), request.getDataId());
                throw new SecurityException("Access denied for user: " + request.getUserId());
            }
            
            // 2. Retrieve encrypted data
            SecuredData securedData = retrieveFromStorage(request.getDataId());
            
            // 3. Verify integrity
            if (!integrityChecker.verifyIntegrity(securedData.getEncryptedData(), securedData.getIntegrityHash())) {
                auditLogger.logIntegrityViolation(request.getDataId());
                throw new SecurityException("Data integrity check failed");
            }
            
            // 4. Decrypt data
            byte[] decryptedData = encryptionHandler.decrypt(securedData.getEncryptedData());
            
            // 5. Audit successful retrieval
            auditLogger.logDataRetrieval(request.getUserId(), request.getDataId());
            
            return decryptedData;
            
        } catch (Exception e) {
            log.error("Error retrieving secured data", e);
            auditLogger.logError("DATA_RETRIEVAL_ERROR", request.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to retrieve secured data", e);
        }
    }
    
    private Map<String, String> createMetadata(DataIngestionRequest request) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("sourceId", request.getDataSourceId());
        metadata.put("userId", request.getUserId());
        metadata.put("ingestionTime", LocalDateTime.now().toString());
        metadata.put("dataType", request.getDataType());
        return metadata;
    }
    
    private SecuredData retrieveFromStorage(String dataId) {
        // In production, this would retrieve from actual storage
        // For demo, return a mock object
        return SecuredData.builder()
            .dataId(dataId)
            .encryptedData(new EncryptedData(new byte[0], new byte[0]))
            .integrityHash("mock-hash")
            .build();
    }
    
    // Inner class for encryption handling
    private class EncryptionHandler {
        private SecretKey masterKey;
        
        public EncryptionHandler() {
            initializeMasterKey();
        }
        
        private void initializeMasterKey() {
            try {
                if (masterKeyConfig != null && !masterKeyConfig.isEmpty()) {
                    // Load from configuration
                    byte[] keyBytes = Base64.getDecoder().decode(masterKeyConfig);
                    masterKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
                } else {
                    // Generate new key for demo
                    KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
                    keyGen.init(KEY_SIZE);
                    masterKey = keyGen.generateKey();
                    log.warn("Generated new master key - in production, load from secure storage");
                }
            } catch (Exception e) {
                log.error("Failed to initialize master key", e);
                throw new RuntimeException("Encryption initialization failed", e);
            }
        }
        
        public EncryptedData encrypt(byte[] data) throws Exception {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            
            // Generate random IV
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, ivSpec);
            byte[] encryptedData = cipher.doFinal(data);
            
            return new EncryptedData(encryptedData, iv);
        }
        
        public byte[] decrypt(EncryptedData encryptedData) throws Exception {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(encryptedData.getIv());
            
            cipher.init(Cipher.DECRYPT_MODE, masterKey, ivSpec);
            return cipher.doFinal(encryptedData.getCiphertext());
        }
    }
    
    // Inner class for integrity checking
    private class IntegrityChecker {
        private SecretKey hmacKey;
        
        public IntegrityChecker() {
            initializeHmacKey();
        }
        
        private void initializeHmacKey() {
            try {
                // In production, load from secure storage
                KeyGenerator keyGen = KeyGenerator.getInstance(HMAC_ALGORITHM);
                keyGen.init(256);
                hmacKey = keyGen.generateKey();
            } catch (Exception e) {
                log.error("Failed to initialize HMAC key", e);
            }
        }
        
        public String generateHash(EncryptedData data) throws Exception {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hmacKey);
            
            mac.update(data.getCiphertext());
            mac.update(data.getIv());
            
            byte[] hash = mac.doFinal();
            return Base64.getEncoder().encodeToString(hash);
        }
        
        public boolean verifyIntegrity(EncryptedData data, String expectedHash) {
            try {
                String actualHash = generateHash(data);
                return MessageDigest.isEqual(
                    expectedHash.getBytes(StandardCharsets.UTF_8),
                    actualHash.getBytes(StandardCharsets.UTF_8)
                );
            } catch (Exception e) {
                log.error("Integrity verification failed", e);
                return false;
            }
        }
    }
    
    // Inner class for role-based access control
    private class RoleBasedAccessControl {
        private final Map<String, Set<Permission>> userPermissions = new ConcurrentHashMap<>();
        
        public RoleBasedAccessControl() {
            // Initialize with default permissions
            initializeDefaultPermissions();
        }
        
        private void initializeDefaultPermissions() {
            // Demo permissions - in production, load from database
            Set<Permission> adminPermissions = new HashSet<>();
            adminPermissions.add(new Permission("*", "READ"));
            adminPermissions.add(new Permission("*", "WRITE"));
            userPermissions.put("admin", adminPermissions);
            
            Set<Permission> userPermissions = new HashSet<>();
            userPermissions.add(new Permission("public-*", "READ"));
            this.userPermissions.put("user", userPermissions);
        }
        
        public boolean checkAccess(String userId, String resourceId, String action) {
            Set<Permission> permissions = userPermissions.get(userId);
            if (permissions == null) {
                return false;
            }
            
            return permissions.stream()
                .anyMatch(p -> p.matches(resourceId, action));
        }
    }
    
    // Inner class for audit logging
    private class AuditLogger {
        private final List<AuditEntry> auditLog = new ArrayList<>();
        
        public void logDataIngestion(String userId, String dataSourceId, String hash) {
            AuditEntry entry = AuditEntry.builder()
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .action("DATA_INGESTION")
                .resourceId(dataSourceId)
                .details("Hash: " + hash)
                .build();
            auditLog.add(entry);
            log.info("Audit: {}", entry);
        }
        
        public void logDataRetrieval(String userId, String dataId) {
            AuditEntry entry = AuditEntry.builder()
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .action("DATA_RETRIEVAL")
                .resourceId(dataId)
                .build();
            auditLog.add(entry);
            log.info("Audit: {}", entry);
        }
        
        public void logAccessDenied(String userId, String resourceId) {
            AuditEntry entry = AuditEntry.builder()
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .action("ACCESS_DENIED")
                .resourceId(resourceId)
                .build();
            auditLog.add(entry);
            log.warn("Audit: {}", entry);
        }
        
        public void logIntegrityViolation(String dataId) {
            AuditEntry entry = AuditEntry.builder()
                .timestamp(LocalDateTime.now())
                .action("INTEGRITY_VIOLATION")
                .resourceId(dataId)
                .build();
            auditLog.add(entry);
            log.error("Audit: {}", entry);
        }
        
        public void logError(String errorType, String userId, String details) {
            AuditEntry entry = AuditEntry.builder()
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .action(errorType)
                .details(details)
                .build();
            auditLog.add(entry);
            log.error("Audit: {}", entry);
        }
    }
    
    // Data classes
    public static class DataIngestionRequest {
        private String userId;
        private String dataSourceId;
        private byte[] data;
        private String dataType;
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getDataSourceId() { return dataSourceId; }
        public void setDataSourceId(String dataSourceId) { this.dataSourceId = dataSourceId; }
        public byte[] getData() { return data; }
        public void setData(byte[] data) { this.data = data; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
    }
    
    public static class DataRetrievalRequest {
        private String userId;
        private String dataId;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getDataId() { return dataId; }
        public void setDataId(String dataId) { this.dataId = dataId; }
    }
    
    public static class SecuredData {
        private String dataId;
        private EncryptedData encryptedData;
        private String integrityHash;
        private Map<String, String> metadata;
        private LocalDateTime timestamp;
        
        public static SecuredDataBuilder builder() {
            return new SecuredDataBuilder();
        }
        
        public String getDataId() { return dataId; }
        public EncryptedData getEncryptedData() { return encryptedData; }
        public String getIntegrityHash() { return integrityHash; }
        
        public static class SecuredDataBuilder {
            private SecuredData data = new SecuredData();
            
            public SecuredDataBuilder dataId(String dataId) {
                data.dataId = dataId;
                return this;
            }
            
            public SecuredDataBuilder encryptedData(EncryptedData encryptedData) {
                data.encryptedData = encryptedData;
                return this;
            }
            
            public SecuredDataBuilder integrityHash(String hash) {
                data.integrityHash = hash;
                return this;
            }
            
            public SecuredDataBuilder metadata(Map<String, String> metadata) {
                data.metadata = metadata;
                return this;
            }
            
            public SecuredDataBuilder timestamp(LocalDateTime timestamp) {
                data.timestamp = timestamp;
                return this;
            }
            
            public SecuredData build() {
                return data;
            }
        }
    }
    
    public static class EncryptedData {
        private final byte[] ciphertext;
        private final byte[] iv;
        
        public EncryptedData(byte[] ciphertext, byte[] iv) {
            this.ciphertext = ciphertext;
            this.iv = iv;
        }
        
        public byte[] getCiphertext() { return ciphertext; }
        public byte[] getIv() { return iv; }
    }
    
    private static class Permission {
        private final String resourcePattern;
        private final String action;
        
        public Permission(String resourcePattern, String action) {
            this.resourcePattern = resourcePattern;
            this.action = action;
        }
        
        public boolean matches(String resourceId, String requestedAction) {
            boolean resourceMatch = resourcePattern.equals("*") || 
                                  resourceId.matches(resourcePattern.replace("*", ".*"));
            return resourceMatch && action.equals(requestedAction);
        }
    }
    
    private static class AuditEntry {
        private LocalDateTime timestamp;
        private String userId;
        private String action;
        private String resourceId;
        private String details;
        
        public static AuditEntryBuilder builder() {
            return new AuditEntryBuilder();
        }
        
        @Override
        public String toString() {
            return String.format("[%s] User: %s, Action: %s, Resource: %s, Details: %s",
                timestamp, userId, action, resourceId, details);
        }
        
        public static class AuditEntryBuilder {
            private AuditEntry entry = new AuditEntry();
            
            public AuditEntryBuilder timestamp(LocalDateTime timestamp) {
                entry.timestamp = timestamp;
                return this;
            }
            
            public AuditEntryBuilder userId(String userId) {
                entry.userId = userId;
                return this;
            }
            
            public AuditEntryBuilder action(String action) {
                entry.action = action;
                return this;
            }
            
            public AuditEntryBuilder resourceId(String resourceId) {
                entry.resourceId = resourceId;
                return this;
            }
            
            public AuditEntryBuilder details(String details) {
                entry.details = details;
                return this;
            }
            
            public AuditEntry build() {
                return entry;
            }
        }
    }
}