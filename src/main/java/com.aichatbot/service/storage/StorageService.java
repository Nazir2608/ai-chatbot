package com.aichatbot.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface StorageService {
    String store(MultipartFile file, String documentId, String filename);
    void storeText(String content, String documentId, String filename);
    InputStream load(String documentId, String filename);
    Resource loadAsResource(String documentId, String filename);
    boolean exists(String documentId, String filename);
    void deleteAll(String documentId);
}
