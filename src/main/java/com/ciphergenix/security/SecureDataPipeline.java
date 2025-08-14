package com.ciphergenix.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SecureDataPipeline {

	private final SecureRandom secureRandom = new SecureRandom();
	private final byte[] encryptionKey = new byte[16];
	private final byte[] hmacKey = new byte[32];

	public SecureDataPipeline() {
		secureRandom.nextBytes(encryptionKey);
		secureRandom.nextBytes(hmacKey);
	}

	public static class SecuredData {
		public final String ciphertextBase64;
		public final String ivBase64;
		public final String hmacBase64;

		public SecuredData(String ciphertextBase64, String ivBase64, String hmacBase64) {
			this.ciphertextBase64 = ciphertextBase64;
			this.ivBase64 = ivBase64;
			this.hmacBase64 = hmacBase64;
		}
	}

	public SecuredData secureDataIngestion(String plaintext, String role) throws Exception {
		// Access control enforcement (placeholder RBAC)
		if (!"INGESTOR".equals(role)) {
			throw new SecurityException("Access denied");
		}

		byte[] iv = new byte[12];
		secureRandom.nextBytes(iv);

		SecretKey aesKey = new SecretKeySpec(encryptionKey, "AES");
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
		byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKey hKey = new SecretKeySpec(hmacKey, "HmacSHA256");
		mac.init(hKey);
		byte[] hmac = mac.doFinal(ByteBuffer.allocate(iv.length + ciphertext.length).put(iv).put(ciphertext).array());

		return new SecuredData(
				Base64.getEncoder().encodeToString(ciphertext),
				Base64.getEncoder().encodeToString(iv),
				Base64.getEncoder().encodeToString(hmac)
		);
	}
}