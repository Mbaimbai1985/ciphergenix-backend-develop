package com.ciphergenix.securityengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * CipherGenix Security Engine Service
 * 
 * Provides comprehensive security operations including:
 * - Cryptographic operations (encryption, decryption, hashing)
 * - Key management and rotation
 * - Digital signatures and verification
 * - Secure file operations
 * - Access control and authorization
 * - Security auditing and event logging
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableKafka
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class SecurityEngineServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityEngineServiceApplication.class, args);
    }
}