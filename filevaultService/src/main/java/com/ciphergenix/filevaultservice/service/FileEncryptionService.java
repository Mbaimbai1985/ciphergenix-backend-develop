package com.ciphergenix.filevaultservice.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class FileEncryptionService {

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public FileEncryptionService() throws Exception {
        // Generate AES-256 Key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // Use 128 if system doesn't support 256-bit
        this.secretKey = keyGen.generateKey();
    }

    public void encryptFile(Path inputPath, Path outputPath) throws Exception {
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        try (FileInputStream fis = new FileInputStream(inputPath.toFile());
             FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {

            // Write IV at start of file
            fos.write(iv);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) fos.write(output);
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) fos.write(finalBytes);
        }
    }

    public void decryptFile(Path inputPath, OutputStream outputStream) throws Exception {
        byte[] iv = new byte[16];

        try (FileInputStream fis = new FileInputStream(inputPath.toFile())) {
            // Read IV
            if (fis.read(iv) != 16) {
                throw new IOException("Invalid IV in file.");
            }

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    outputStream.write(output);
                }
            }

            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                outputStream.write(finalBytes);
            }
        }
    }



}