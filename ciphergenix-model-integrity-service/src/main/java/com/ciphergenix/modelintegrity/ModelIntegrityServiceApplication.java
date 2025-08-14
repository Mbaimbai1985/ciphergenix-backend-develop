package com.ciphergenix.modelintegrity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableKafka
@EnableAsync
@EnableScheduling
public class ModelIntegrityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelIntegrityServiceApplication.class, args);
    }
}