package com.aichatbot.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    @Value("${storage.local.root:./uploads}")
    private String rootPath;

    private Path getDocumentPath(String documentId) {
        return Paths.get(rootPath, documentId);
    }

    @Override
    public String store(MultipartFile file, String documentId, String filename) {
        try {
            Path docPath = getDocumentPath(documentId);
            Files.createDirectories(docPath);
            Path targetPath = docPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file locally: {}", targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file locally", e);
        }
    }

    @Override
    public void storeText(String content, String documentId, String filename) {
        try {
            Path docPath = getDocumentPath(documentId);
            Files.createDirectories(docPath);
            Path targetPath = docPath.resolve(filename);
            Files.writeString(targetPath, content);
            log.info("Stored text locally: {}", targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store text locally", e);
        }
    }

    @Override
    public InputStream load(String documentId, String filename) {
        try {
            return Files.newInputStream(getDocumentPath(documentId).resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file", e);
        }
    }

    @Override
    public Resource loadAsResource(String documentId, String filename) {
        return new FileSystemResource(getDocumentPath(documentId).resolve(filename));
    }

    @Override
    public boolean exists(String documentId, String filename) {
        return Files.exists(getDocumentPath(documentId).resolve(filename));
    }

    @Override
    public void deleteAll(String documentId) {
        try {
            Path docPath = getDocumentPath(documentId);
            if (Files.exists(docPath)) {
                Files.walk(docPath)
                    .sorted((a, b) -> -a.compareTo(b)) // Delete children first
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("Failed to delete: {}", p);
                        }
                    });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete document", e);
        }
    }
}
