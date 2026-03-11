package com.aichatbot.service;

import com.aichatbot.model.AnalysisResult;
import com.aichatbot.model.Document;
import com.aichatbot.service.llm.LlmService;
import com.aichatbot.service.storage.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final StorageService storageService;
    private final ImageProcessingService imageProcessingService;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    private static final String ORIGINAL_FILENAME = "original";
    private static final String ANALYSIS_FILENAME = "analysis.json";

    public Document processDocument(MultipartFile file) {
        Document doc = new Document();
        doc.setOriginalFilename(file.getOriginalFilename());
        try {
            // 1. Determine file type and store original
            String extension = getExtension(file.getOriginalFilename());
            boolean isPdf = extension.equalsIgnoreCase("pdf");
            
            String originalName = ORIGINAL_FILENAME + "." + extension;
            storageService.store(file, doc.getDocumentId(), originalName);
            log.info("Stored original file for doc: {}", doc.getDocumentId());
            // 2. Convert to images (PDF->pages or Image->base64)
            List<String> base64Images;
            if (isPdf) {
                try (InputStream is = file.getInputStream()) {
                    base64Images = imageProcessingService.convertPdfToImages(is);
                }
                doc.setPageCount(base64Images.size());
            } else {
                try (InputStream is = file.getInputStream()) {
                    String base64 = imageProcessingService.processImage(is);
                    base64Images = List.of(base64);
                    doc.setPageCount(1);
                }
            }
            // 3. Analyze first page with VLM (for multi-page, loop here)
            // For MVP, we analyze page 1. Extend to merge multi-page analyses for prod.
            log.info("Sending to VLM for analysis...");
            AnalysisResult analysis = llmService.analyzeImage(base64Images.get(0), doc.getDocumentId());
            // 4. Persist analysis JSON
            String analysisJson = objectMapper.writeValueAsString(analysis);
            storageService.storeText(analysisJson, doc.getDocumentId(), ANALYSIS_FILENAME);
            log.info("Document processing complete: {}", doc.getDocumentId());
            return doc;
        } catch (Exception e) {
            log.error("Failed to process document", e);
            // Cleanup on failure
            storageService.deleteAll(doc.getDocumentId());
            throw new RuntimeException("Document processing failed: " + e.getMessage(), e);
        }
    }

    public String loadAnalysis(String documentId) {
        try (InputStream is = storageService.load(documentId, ANALYSIS_FILENAME)) {
            return new String(is.readAllBytes());
        } catch (Exception e) {
            log.error("Failed to load analysis for doc: {}", documentId);
            throw new RuntimeException("Analysis not found");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
