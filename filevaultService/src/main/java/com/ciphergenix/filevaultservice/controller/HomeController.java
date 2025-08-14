package com.ciphergenix.filevaultservice.controller;

import com.ciphergenix.filevaultservice.service.AccessLogService;
import com.ciphergenix.filevaultservice.service.FileEncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class HomeController {

    @Autowired
    private FileEncryptionService encryptionService;

    @Autowired
    private AccessLogService logService;


    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model, HttpServletRequest request) {
        try {
            Path uploadPath = Paths.get("uploads");
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Path encryptedPath = Paths.get("encrypted", "enc_" + file.getOriginalFilename());
            Files.createDirectories(encryptedPath.getParent());

            encryptionService.encryptFile(filePath, encryptedPath);

            model.addAttribute("message", "Encrypted and saved file: " + encryptedPath.getFileName());
            model.addAttribute("fileName", file.getOriginalFilename());

            System.out.println("File encrypted: " + encryptedPath.toAbsolutePath());

            logService.log(file.getOriginalFilename(), "UPLOAD", request);
            logService.log("enc_" + file.getOriginalFilename(), "ENCRYPT", request);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Encryption failed: " + e.getMessage());
        }
        return "index";
    }

    @GetMapping("/download/{fileName}")
    public void downloadDecryptedFile(@PathVariable String fileName, HttpServletResponse response, HttpServletRequest request) {
        Path encryptedPath = Paths.get("encrypted", "enc_" + fileName);

        if (!Files.exists(encryptedPath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (OutputStream os = response.getOutputStream()) {
            encryptionService.decryptFile(encryptedPath, os);
            os.flush();
            System.out.println("Decrypted file served: " + fileName);

            logService.log(fileName, "DOWNLOAD", request);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


}