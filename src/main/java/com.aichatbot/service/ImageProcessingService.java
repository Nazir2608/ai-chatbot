package com.aichatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Image Pre-processing Service
 * 
 * Handles conversion and optimization before sending to VLM:
 * - PDF -> PNG conversion (300 DPI for readability)
 * - Image resizing (if too large for model context)
 * - Format normalization (always PNG)
 * 
 * Note: Advanced preprocessing (deskew, denoise) would use OpenCV here,
 * but we keep it simple for portability. For production, add OpenCV here.
 */
@Slf4j
@Service
public class ImageProcessingService {

    private static final int DPI = 300;
    private static final int MAX_WIDTH = 2048; // Most VLMs have context limits

    /**
     * Convert PDF input stream to list of Base64 PNG images (one per page)
     */
    public List<String> convertPdfToImages(InputStream pdfStream) throws IOException {
        List<String> base64Images = new ArrayList<>();
        
        try (PDDocument document = PDDocument.load(pdfStream)) {
            PDFRenderer renderer = new PDFRenderer(document);
            
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                log.debug("Rendering PDF page {} at {} DPI", i + 1, DPI);
                BufferedImage image = renderer.renderImageWithDPI(i, DPI);
                
                // Resize if too large (maintaining aspect ratio)
                BufferedImage resized = resizeIfNeeded(image);
                
                String base64 = imageToBase64(resized);
                base64Images.add(base64);
            }
        }
        
        log.info("Converted PDF to {} images", base64Images.size());
        return base64Images;
    }

    /**
     * Convert single image to Base64 PNG
     */
    public String processImage(InputStream imageStream) throws IOException {
        BufferedImage image = ImageIO.read(imageStream);
        if (image == null) {
            throw new IOException("Could not read image");
        }
        
        BufferedImage resized = resizeIfNeeded(image);
        return imageToBase64(resized);
    }

    private BufferedImage resizeIfNeeded(BufferedImage original) {
        if (original.getWidth() <= MAX_WIDTH) {
            return original;
        }
        
        double ratio = (double) MAX_WIDTH / original.getWidth();
        int newHeight = (int) (original.getHeight() * ratio);
        
        log.debug("Resizing image from {}x{} to {}x{}", 
                  original.getWidth(), original.getHeight(), MAX_WIDTH, newHeight);
        
        BufferedImage resized = new BufferedImage(MAX_WIDTH, newHeight, BufferedImage.TYPE_INT_RGB);
        resized.getGraphics().drawImage(
            original.getScaledInstance(MAX_WIDTH, newHeight, java.awt.Image.SCALE_SMOOTH), 
            0, 0, null
        );
        return resized;
    }

    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
