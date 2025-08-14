package com.ciphergenix.securityengine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Key Management Service
 * 
 * Handles cryptographic key generation, storage, rotation, and revocation
 */
@Service
public class KeyManagementService {

    private static final Logger logger = LoggerFactory.getLogger(KeyManagementService.class);
    
    // In-memory key storage (in production, this would be a secure key vault)
    private final Map<String, String> keyStore = new HashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a new encryption key
     */
    public String generateKey(String keyType, String owner) {
        try {
            String keyId = UUID.randomUUID().toString();
            
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();
            
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            keyStore.put(keyId, encodedKey);
            
            logger.info("Generated new key: {} for owner: {}", keyId, owner);
            return keyId;
            
        } catch (Exception e) {
            logger.error("Error generating key", e);
            throw new RuntimeException("Failed to generate key", e);
        }
    }

    /**
     * Get secret key by ID
     */
    public SecretKey getSecretKey(String keyId) {
        try {
            String encodedKey = keyStore.get(keyId);
            if (encodedKey == null) {
                logger.warn("Key not found: {}", keyId);
                return null;
            }
            
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            return new SecretKeySpec(decodedKey, "AES");
            
        } catch (Exception e) {
            logger.error("Error retrieving key: {}", keyId, e);
            return null;
        }
    }

    /**
     * Revoke a key
     */
    public void revokeKey(String keyId) {
        keyStore.remove(keyId);
        logger.info("Revoked key: {}", keyId);
    }
}