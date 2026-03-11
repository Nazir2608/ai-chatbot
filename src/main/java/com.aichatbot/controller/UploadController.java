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

@Slf4j
@Controller
@RequiredArgsConstructor
public class UploadController {

    private final DocumentService documentService;

    @GetMapping("/")
    public String uploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file, @RequestParam(value = "storageType", required = false) String storageType, RedirectAttributes redirectAttrs) {
        if (file.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/";
        }
        try {
            log.info("Processing upload: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
            Document doc = documentService.processDocument(file);
            redirectAttrs.addFlashAttribute("success", "Document uploaded successfully! Pages: " + doc.getPageCount());
            return "redirect:/document/" + doc.getDocumentId();
        } catch (Exception e) {
            log.error("Upload failed", e);
            redirectAttrs.addFlashAttribute("error", "Upload failed: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/api/upload")
    @ResponseBody
    public Document handleApiUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        return documentService.processDocument(file);
    }
}
