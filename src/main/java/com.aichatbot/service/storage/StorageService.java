package com.aichatbot.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

/**
 * Strategy Interface for Storage
 * 
 * Implementations: LocalStorageService (dev), S3StorageService (prod)
 * 
 * All document operations go through this interface, ensuring the rest of the
 * application is agnostic to where files are stored.
 */
public interface StorageService {
    
    /**
     * Store a file and return its storage identifier/path
     */
    String store(MultipartFile file, String documentId, String filename);
    
    /**
     * Store text content (like analysis.json) directly
     */
    void storeText(String content, String documentId, String filename);
    
    /**
     * Load file as InputStream
     */
    InputStream load(String documentId, String filename);
    
    /**
     * Load as Spring Resource (for serving via HTTP)
     */
    Resource loadAsResource(String documentId, String filename);
    
    /**
     * Check if file exists
     */
    boolean exists(String documentId, String filename);
    
    /**
     * Delete all files for a document
     */
    void deleteAll(String documentId);
}
