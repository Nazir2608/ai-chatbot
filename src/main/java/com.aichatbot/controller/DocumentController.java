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

@Slf4j
@Controller
@RequiredArgsConstructor
public class DocumentController {

    private final StorageService storageService;

    @GetMapping("/document/{documentId}")
    public String viewDocument(@PathVariable String documentId, Model model) {
        if (!storageService.exists(documentId, "original.png") && !storageService.exists(documentId, "original.pdf")) {
            throw new RuntimeException("Document not found");
        }
        boolean isPdf = storageService.exists(documentId, "original.pdf");
        model.addAttribute("documentId", documentId);
        model.addAttribute("isPdf", isPdf);
        return "document";
    }

    @GetMapping("/api/documents/{documentId}/image")
    @ResponseBody
    public ResponseEntity<Resource> serveImage(@PathVariable String documentId) {
        if (storageService.exists(documentId, "original.png")) {
            Resource resource = storageService.loadAsResource(documentId, "original.png");
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
        }
        Resource resource = storageService.loadAsResource(documentId, "original.pdf");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(resource);
    }

    @GetMapping("/api/documents/{documentId}/analysis")
    @ResponseBody
    public ResponseEntity<Resource> serveAnalysis(@PathVariable String documentId) {
        Resource resource = storageService.loadAsResource(documentId, "analysis.json");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resource);
    }
}