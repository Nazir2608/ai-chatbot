package com.aichatbot.controller;

import com.aichatbot.model.Document;
import com.aichatbot.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Upload Controller
 * 
 * Handles the initial ingestion of handwritten notes.
 * Supports both web form uploads and REST API uploads.
 * 
 * After upload, redirects to the document viewer/chat page.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class UploadController {

    private final DocumentService documentService;

    /**
     * Show upload form (Thymeleaf view)
     */
    @GetMapping("/")
    public String uploadForm() {
        return "upload";
    }

    /**
     * Handle file upload from web form
     */
    @PostMapping("/upload")
    public String handleUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "storageType", required = false) String storageType,
            RedirectAttributes redirectAttrs) {
        
        if (file.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/";
        }

        try {
            log.info("Processing upload: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
            
            // Process document (analysis happens here)
            Document doc = documentService.processDocument(file);
            
            redirectAttrs.addFlashAttribute("success", 
                "Document uploaded successfully! Pages: " + doc.getPageCount());
            
            // Redirect to document view/chat page
            return "redirect:/document/" + doc.getDocumentId();
            
        } catch (Exception e) {
            log.error("Upload failed", e);
            redirectAttrs.addFlashAttribute("error", "Upload failed: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * REST API endpoint for programmatic uploads
     * Returns JSON with documentId for API clients
     */
    @PostMapping("/api/upload")
    @ResponseBody
    public Document handleApiUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        return documentService.processDocument(file);
    }
}
