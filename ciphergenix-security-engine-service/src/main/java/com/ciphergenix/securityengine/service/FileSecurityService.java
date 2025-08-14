package com.ciphergenix.securityengine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * FileSecurityService
 * 
 * Enhanced file security service that provides:
 * - Secure file upload and storage
 * - AES-256 encryption/decryption
 * - Key management integration
 * - Access logging and audit trails
 * - Secure file retrieval and streaming
 */
@Service
public class FileSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(FileSecurityService.class);
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String SECURE_DIRECTORY = "secure-files";
    private static final String UPLOAD_DIRECTORY = "uploads";
    private static final int IV_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    @Autowired
    private KeyManagementService keyManagementService;
    
    @Autowired
    private SecurityAuditService auditService;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Upload and encrypt a file securely
     */
    public FileSecurityResult uploadSecureFile(MultipartFile file, String userId, Map<String, String> metadata) {
        try {
            logger.info("Starting secure file upload for user: {}, file: {}", userId, file.getOriginalFilename());
            
            // Validate file
            validateFile(file);
            
            // Generate unique file ID
            String fileId = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String encryptedFilename = "enc_" + fileId + "_" + originalFilename;
            
            // Create directories
            Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
            Path securePath = Paths.get(SECURE_DIRECTORY);
            Files.createDirectories(uploadPath);
            Files.createDirectories(securePath);
            
            // Save original file temporarily
            Path tempFilePath = uploadPath.resolve(fileId + "_temp_" + originalFilename);
            Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Generate encryption key
            String keyId = keyManagementService.generateKey("FILE_ENCRYPTION", userId);
            SecretKey secretKey = keyManagementService.getSecretKey(keyId);
            
            // Encrypt file
            Path encryptedFilePath = securePath.resolve(encryptedFilename);
            encryptFile(tempFilePath, encryptedFilePath, secretKey);
            
            // Generate file hash for integrity
            String fileHash = generateFileHash(tempFilePath);
            
            // Clean up temporary file
            Files.deleteIfExists(tempFilePath);
            
            // Create file metadata
            FileSecurityMetadata fileMetadata = new FileSecurityMetadata();
            fileMetadata.setFileId(fileId);
            fileMetadata.setOriginalFilename(originalFilename);
            fileMetadata.setEncryptedFilename(encryptedFilename);
            fileMetadata.setKeyId(keyId);
            fileMetadata.setFileHash(fileHash);
            fileMetadata.setFileSize(file.getSize());
            fileMetadata.setContentType(file.getContentType());
            fileMetadata.setUserId(userId);
            fileMetadata.setUploadedAt(LocalDateTime.now());
            fileMetadata.setMetadata(metadata);
            
            // Store metadata (in real implementation, this would go to database)
            storeFileMetadata(fileMetadata);
            
            // Log security event
            auditService.logSecurityEvent("FILE_UPLOAD", "INFO", userId, Map.of(
                "fileId", fileId,
                "filename", originalFilename,
                "size", String.valueOf(file.getSize()),
                "contentType", file.getContentType()
            ));
            
            logger.info("File uploaded and encrypted successfully: {}", fileId);
            
            return FileSecurityResult.success(fileId, fileMetadata);
            
        } catch (Exception e) {
            logger.error("Error uploading secure file", e);
            auditService.logSecurityEvent("FILE_UPLOAD_ERROR", "ERROR", userId, Map.of(
                "filename", file.getOriginalFilename(),
                "error", e.getMessage()
            ));
            return FileSecurityResult.error("Failed to upload file: " + e.getMessage());
        }
    }
    
    /**
     * Download and decrypt a file securely
     */
    public FileSecurityResult downloadSecureFile(String fileId, String userId) {
        try {
            logger.info("Starting secure file download for user: {}, fileId: {}", userId, fileId);
            
            // Get file metadata
            FileSecurityMetadata metadata = getFileMetadata(fileId);
            if (metadata == null) {
                return FileSecurityResult.error("File not found");
            }
            
            // Check access permissions
            if (!hasFileAccess(metadata, userId)) {
                auditService.logSecurityEvent("UNAUTHORIZED_FILE_ACCESS", "WARNING", userId, Map.of(
                    "fileId", fileId,
                    "owner", metadata.getUserId()
                ));
                return FileSecurityResult.error("Access denied");
            }
            
            // Get encryption key
            SecretKey secretKey = keyManagementService.getSecretKey(metadata.getKeyId());
            if (secretKey == null) {
                return FileSecurityResult.error("Encryption key not found");
            }
            
            // Decrypt file
            Path encryptedFilePath = Paths.get(SECURE_DIRECTORY, metadata.getEncryptedFilename());
            if (!Files.exists(encryptedFilePath)) {
                return FileSecurityResult.error("Encrypted file not found");
            }
            
            byte[] decryptedData = decryptFile(encryptedFilePath, secretKey);
            
            // Log access event
            auditService.logSecurityEvent("FILE_DOWNLOAD", "INFO", userId, Map.of(
                "fileId", fileId,
                "filename", metadata.getOriginalFilename()
            ));
            
            logger.info("File downloaded and decrypted successfully: {}", fileId);
            
            return FileSecurityResult.success(fileId, metadata, decryptedData);
            
        } catch (Exception e) {
            logger.error("Error downloading secure file: {}", fileId, e);
            auditService.logSecurityEvent("FILE_DOWNLOAD_ERROR", "ERROR", userId, Map.of(
                "fileId", fileId,
                "error", e.getMessage()
            ));
            return FileSecurityResult.error("Failed to download file: " + e.getMessage());
        }
    }
    
    /**
     * Delete a secure file
     */
    public FileSecurityResult deleteSecureFile(String fileId, String userId) {
        try {
            logger.info("Starting secure file deletion for user: {}, fileId: {}", userId, fileId);
            
            // Get file metadata
            FileSecurityMetadata metadata = getFileMetadata(fileId);
            if (metadata == null) {
                return FileSecurityResult.error("File not found");
            }
            
            // Check access permissions
            if (!hasFileAccess(metadata, userId)) {
                auditService.logSecurityEvent("UNAUTHORIZED_FILE_DELETE", "WARNING", userId, Map.of(
                    "fileId", fileId,
                    "owner", metadata.getUserId()
                ));
                return FileSecurityResult.error("Access denied");
            }
            
            // Delete encrypted file
            Path encryptedFilePath = Paths.get(SECURE_DIRECTORY, metadata.getEncryptedFilename());
            Files.deleteIfExists(encryptedFilePath);
            
            // Revoke encryption key
            keyManagementService.revokeKey(metadata.getKeyId());
            
            // Delete metadata
            deleteFileMetadata(fileId);
            
            // Log deletion event
            auditService.logSecurityEvent("FILE_DELETE", "INFO", userId, Map.of(
                "fileId", fileId,
                "filename", metadata.getOriginalFilename()
            ));
            
            logger.info("File deleted successfully: {}", fileId);
            
            return FileSecurityResult.success(fileId, metadata);
            
        } catch (Exception e) {
            logger.error("Error deleting secure file: {}", fileId, e);
            return FileSecurityResult.error("Failed to delete file: " + e.getMessage());
        }
    }
    
    /**
     * Encrypt file using AES-256-CBC
     */
    private void encryptFile(Path inputPath, Path outputPath, SecretKey secretKey) throws Exception {
        // Generate random IV
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Initialize cipher
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        
        try (FileInputStream fis = new FileInputStream(inputPath.toFile());
             FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            
            // Write IV at the beginning of the encrypted file
            fos.write(iv);
            
            // Encrypt file in chunks
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    fos.write(output);
                }
            }
            
            // Write final block
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                fos.write(finalBytes);
            }
        }
    }
    
    /**
     * Decrypt file from AES-256-CBC
     */
    private byte[] decryptFile(Path inputPath, SecretKey secretKey) throws Exception {
        try (FileInputStream fis = new FileInputStream(inputPath.toFile());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // Read IV from the beginning of the file
            byte[] iv = new byte[IV_LENGTH];
            if (fis.read(iv) != IV_LENGTH) {
                throw new IOException("Invalid IV in encrypted file");
            }
            
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // Decrypt file in chunks
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    baos.write(output);
                }
            }
            
            // Write final block
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                baos.write(finalBytes);
            }
            
            return baos.toByteArray();
        }
    }
    
    /**
     * Generate SHA-256 hash of file for integrity checking
     */
    private String generateFileHash(Path filePath) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        
        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        
        byte[] hashBytes = md.digest();
        return Base64.getEncoder().encodeToString(hashBytes);
    }
    
    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (file.getSize() > 100 * 1024 * 1024) { // 100MB limit
            throw new IllegalArgumentException("File size exceeds maximum limit");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }
        
        // Check for potentially dangerous file extensions
        String lowerFilename = filename.toLowerCase();
        String[] dangerousExtensions = {".exe", ".bat", ".cmd", ".scr", ".pif", ".vbs", ".js"};
        for (String ext : dangerousExtensions) {
            if (lowerFilename.endsWith(ext)) {
                throw new IllegalArgumentException("File type not allowed");
            }
        }
    }
    
    /**
     * Check if user has access to file
     */
    private boolean hasFileAccess(FileSecurityMetadata metadata, String userId) {
        // Owner has full access
        if (metadata.getUserId().equals(userId)) {
            return true;
        }
        
        // Additional access control logic could be implemented here
        // For example, checking shared permissions, role-based access, etc.
        
        return false;
    }
    
    /**
     * Store file metadata (placeholder - would use database in real implementation)
     */
    private void storeFileMetadata(FileSecurityMetadata metadata) {
        // In real implementation, this would save to database
        logger.debug("Storing file metadata for fileId: {}", metadata.getFileId());
    }
    
    /**
     * Get file metadata (placeholder - would use database in real implementation)
     */
    private FileSecurityMetadata getFileMetadata(String fileId) {
        // In real implementation, this would query database
        // For now, return null to simulate file not found
        return null;
    }
    
    /**
     * Delete file metadata (placeholder - would use database in real implementation)
     */
    private void deleteFileMetadata(String fileId) {
        // In real implementation, this would delete from database
        logger.debug("Deleting file metadata for fileId: {}", fileId);
    }
    
    /**
     * File Security Metadata
     */
    public static class FileSecurityMetadata {
        private String fileId;
        private String originalFilename;
        private String encryptedFilename;
        private String keyId;
        private String fileHash;
        private long fileSize;
        private String contentType;
        private String userId;
        private LocalDateTime uploadedAt;
        private Map<String, String> metadata;
        
        // Getters and setters
        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }
        
        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
        
        public String getEncryptedFilename() { return encryptedFilename; }
        public void setEncryptedFilename(String encryptedFilename) { this.encryptedFilename = encryptedFilename; }
        
        public String getKeyId() { return keyId; }
        public void setKeyId(String keyId) { this.keyId = keyId; }
        
        public String getFileHash() { return fileHash; }
        public void setFileHash(String fileHash) { this.fileHash = fileHash; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }
    
    /**
     * File Security Result
     */
    public static class FileSecurityResult {
        private boolean success;
        private String message;
        private String fileId;
        private FileSecurityMetadata metadata;
        private byte[] fileData;
        
        public static FileSecurityResult success(String fileId, FileSecurityMetadata metadata) {
            FileSecurityResult result = new FileSecurityResult();
            result.success = true;
            result.fileId = fileId;
            result.metadata = metadata;
            result.message = "Operation completed successfully";
            return result;
        }
        
        public static FileSecurityResult success(String fileId, FileSecurityMetadata metadata, byte[] fileData) {
            FileSecurityResult result = success(fileId, metadata);
            result.fileData = fileData;
            return result;
        }
        
        public static FileSecurityResult error(String message) {
            FileSecurityResult result = new FileSecurityResult();
            result.success = false;
            result.message = message;
            return result;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getFileId() { return fileId; }
        public FileSecurityMetadata getMetadata() { return metadata; }
        public byte[] getFileData() { return fileData; }
    }
}