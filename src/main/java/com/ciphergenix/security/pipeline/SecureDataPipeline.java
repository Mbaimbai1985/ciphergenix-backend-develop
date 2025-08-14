package com.ciphergenix.security.pipeline;

import org.springframework.stereotype.Service;

@Service
public class SecureDataPipeline {

    private final AESEncryption encryptionHandler;
    private final HmacSha256 integrityChecker;
    private final RoleBasedAccessControl accessControl;

    public SecureDataPipeline() {
        // In real scenario keys should be loaded from secure vault
        this.encryptionHandler = AESEncryption.withRandomKey();
        this.integrityChecker = new HmacSha256("secret-key-placeholder".getBytes());
        this.accessControl = new RoleBasedAccessControl();
    }

    public SecuredData secureDataIngestion(String data, org.springframework.security.core.Authentication auth) {
        if (!accessControl.hasRole(auth, "ADMIN")) {
            throw new SecurityException("Unauthorized access");
        }

        String encrypted = encryptionHandler.encrypt(data);
        String signature = integrityChecker.sign(encrypted);
        return new SecuredData(encrypted, signature);
    }

    public String verifyAndDecrypt(SecuredData securedData) {
        if (!integrityChecker.verify(securedData.encryptedPayload(), securedData.signature())) {
            throw new SecurityException("Data integrity check failed");
        }
        return encryptionHandler.decrypt(securedData.encryptedPayload());
    }

    public record SecuredData(String encryptedPayload, String signature) {}
}