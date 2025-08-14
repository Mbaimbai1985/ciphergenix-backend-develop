package com.ciphergenix.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private HealthIndicator[] healthIndicators;
    
    /**
     * Basic health check endpoint
     */
    @GetMapping("/basic")
    public ResponseEntity<Map<String, Object>> basicHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "CipherGenix AI Security Service");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Detailed health check endpoint
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "CipherGenix AI Security Service");
        health.put("timestamp", System.currentTimeMillis());
        
        // Check database connectivity
        Map<String, Object> database = checkDatabaseHealth();
        health.put("database", database);
        
        // Check ML components
        Map<String, Object> mlComponents = checkMLComponentsHealth();
        health.put("mlComponents", mlComponents);
        
        // Check security components
        Map<String, Object> security = checkSecurityHealth();
        health.put("security", security);
        
        // Overall status
        boolean allHealthy = "UP".equals(database.get("status")) && 
                           "UP".equals(mlComponents.get("status")) && 
                           "UP".equals(security.get("status"));
        
        health.put("status", allHealthy ? "UP" : "DOWN");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Check database health
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                dbHealth.put("status", "UP");
                dbHealth.put("message", "Database connection successful");
                dbHealth.put("url", connection.getMetaData().getURL());
            } else {
                dbHealth.put("status", "DOWN");
                dbHealth.put("message", "Database connection invalid");
            }
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("message", "Database connection failed: " + e.getMessage());
        }
        
        return dbHealth;
    }
    
    /**
     * Check ML components health
     */
    private Map<String, Object> checkMLComponentsHealth() {
        Map<String, Object> mlHealth = new HashMap<>();
        
        try {
            // Check if ML libraries are available
            Class.forName("org.apache.commons.math3.linear.RealMatrix");
            Class.forName("org.deeplearning4j.nn.multilayer.MultiLayerNetwork");
            
            mlHealth.put("status", "UP");
            mlHealth.put("message", "ML libraries loaded successfully");
            mlHealth.put("components", new String[]{"Apache Commons Math", "DeepLearning4J"});
        } catch (ClassNotFoundException e) {
            mlHealth.put("status", "DOWN");
            mlHealth.put("message", "ML libraries not available: " + e.getMessage());
        }
        
        return mlHealth;
    }
    
    /**
     * Check security components health
     */
    private Map<String, Object> checkSecurityHealth() {
        Map<String, Object> securityHealth = new HashMap<>();
        
        try {
            // Check if security libraries are available
            Class.forName("javax.crypto.Cipher");
            Class.forName("java.security.MessageDigest");
            
            securityHealth.put("status", "UP");
            securityHealth.put("message", "Security libraries loaded successfully");
            securityHealth.put("components", new String[]{"JCE", "MessageDigest"});
        } catch (ClassNotFoundException e) {
            securityHealth.put("status", "DOWN");
            securityHealth.put("message", "Security libraries not available: " + e.getMessage());
        }
        
        return securityHealth;
    }
    
    /**
     * System information endpoint
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> systemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        // JVM information
        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("jvm", Map.of(
            "version", System.getProperty("java.version"),
            "vendor", System.getProperty("java.vendor"),
            "maxMemory", runtime.maxMemory(),
            "totalMemory", runtime.totalMemory(),
            "freeMemory", runtime.freeMemory(),
            "availableProcessors", runtime.availableProcessors()
        ));
        
        // System information
        systemInfo.put("system", Map.of(
            "os", System.getProperty("os.name"),
            "osVersion", System.getProperty("os.version"),
            "architecture", System.getProperty("os.arch"),
            "userHome", System.getProperty("user.home"),
            "userDir", System.getProperty("user.dir")
        ));
        
        // Application information
        systemInfo.put("application", Map.of(
            "name", "CipherGenix AI Security Service",
            "version", "1.0.0",
            "startTime", System.currentTimeMillis()
        ));
        
        return ResponseEntity.ok(systemInfo);
    }
}