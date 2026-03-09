package com.aichatbot.controller;

import com.aichatbot.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Document View Controller
 * 
 * Serves the main interface:
 * - Left side: Image viewer with overlays (diagrams, crossed-out text highlights)
 * - Right side: Chat interface
 * 
 * Also serves images from storage (local or S3) via /api/documents/{id}/image
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final StorageService storageService;

    /**
     * Display document viewer page
     */
    @GetMapping("/document/{documentId}")
    public String viewDocument(@PathVariable String documentId, Model model) {
        // Verify document exists
        if (!storageService.exists(documentId, "original.png") && 
            !storageService.exists(documentId, "original.pdf")) {
            throw new RuntimeException("Document not found");
        }
        
        model.addAttribute("documentId", documentId);
        return "document"; // Renders document.html
    }

    /**
     * Serve original image/PDF for display in viewer
     */
    @GetMapping("/api/documents/{documentId}/image")
    @ResponseBody
    public ResponseEntity<Resource> serveImage(@PathVariable String documentId) {
        // Try PNG first (converted from PDF or uploaded as image)
        if (storageService.exists(documentId, "original.png")) {
            Resource resource = storageService.loadAsResource(documentId, "original.png");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        }
        
        // Fallback to original extension
        Resource resource = storageService.loadAsResource(documentId, "original");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Serve analysis.json for frontend overlays (optional advanced feature)
     */
    @GetMapping("/api/documents/{documentId}/analysis")
    @ResponseBody
    public ResponseEntity<Resource> serveAnalysis(@PathVariable String documentId) {
        Resource resource = storageService.loadAsResource(documentId, "analysis.json");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resource);
    }
}
