package com.aichatbot.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Document {
    private String documentId;
    private String originalFilename;
    private String storagePath;
    private String analysisJsonPath;
    private int pageCount;
    private LocalDateTime uploadedAt;
    
    public Document() {
        this.documentId = UUID.randomUUID().toString();
        this.uploadedAt = LocalDateTime.now();
    }
}
