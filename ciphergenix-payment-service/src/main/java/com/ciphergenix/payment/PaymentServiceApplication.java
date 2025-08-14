package com.ciphergenix.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CipherGenix Payment Service Application
 * 
 * Main entry point for the payment processing microservice with Stripe integration.
 * Provides secure payment processing, subscription management, and billing capabilities
 * for the CipherGenix AI Security Platform.
 * 
 * Features:
 * - Stripe payment processing integration
 * - Subscription billing management
 * - Payment method handling
 * - Invoice generation and management
 * - Webhook event processing
 * - PCI-compliant payment security
 * 
 * @author CipherGenix Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}