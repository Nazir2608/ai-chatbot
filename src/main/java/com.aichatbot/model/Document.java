package com.aichatbot.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an uploaded handwritten note/document
 * 
 * documentId: Unique identifier (UUID)
 * originalFilename: Original name of uploaded file
 * storagePath: Path in storage (local or S3 key)
 * analysisJsonPath: Path to the processed VLM analysis result
 * pageCount: For PDFs, number of pages processed
 * uploadedAt: Timestamp
 */
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
