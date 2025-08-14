package com.ciphergenix.security.pipeline;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HmacSha256 {

    private final byte[] secret;

    public HmacSha256(byte[] secret) {
        this.secret = secret;
    }

    public String sign(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("HMAC generation failed", e);
        }
    }

    public boolean verify(String message, String signature) {
        return sign(message).equals(signature);
    }
}