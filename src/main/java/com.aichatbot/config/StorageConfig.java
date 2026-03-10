package com.aichatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.aichatbot.service.storage.LocalStorageService;
import com.aichatbot.service.storage.S3StorageService;
import com.aichatbot.service.storage.StorageService;

@Configuration
public class StorageConfig {

    @Value("${storage.type:local}")
    private String storageType;

    @Bean
    @Primary
    public StorageService storageService(LocalStorageService localService, S3StorageService s3Service) {
        return switch (storageType.toLowerCase()) {
            case "s3" -> s3Service;
            case "local" -> localService;
            default -> throw new IllegalArgumentException("Unknown storage type: " + storageType);
        };
    }
}
