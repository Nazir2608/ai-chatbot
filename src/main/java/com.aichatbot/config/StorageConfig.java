package com.aichatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.aichatbot.service.storage.LocalStorageService;
import com.aichatbot.service.storage.S3StorageService;
import com.aichatbot.service.storage.StorageService;

/**
 * Storage Configuration
 * 
 * Implements the Strategy Pattern for storage backends.
 * Switch between Local and S3 storage by changing 'storage.type' in application.yml
 * No code changes required - purely configuration-driven.
 */
@Configuration
public class StorageConfig {

    @Value("${storage.type:local}")
    private String storageType;

    /**
     * Primary Storage Service Bean
     * Returns either Local or S3 implementation based on configuration
     */
    @Bean
    @Primary
    public StorageService storageService(
            LocalStorageService localService,
            S3StorageService s3Service) {
        
        return switch (storageType.toLowerCase()) {
            case "s3" -> s3Service;
            case "local" -> localService;
            default -> throw new IllegalArgumentException("Unknown storage type: " + storageType);
        };
    }
}
