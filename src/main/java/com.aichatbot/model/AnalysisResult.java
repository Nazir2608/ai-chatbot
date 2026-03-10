package com.aichatbot.model;

import lombok.Data;
import java.util.List;

/**
 * Structured representation of VLM (Vision Language Model) analysis
 * 
 * This is the CRITICAL data structure. Instead of raw OCR text, we store
 * semantic understanding including:
 * - crossedOut: Text that was crossed out and replaced (crucial for understanding corrections)
 * - diagrams: Flowcharts, boxes, arrows
 * - marginNotes: Annotations in margins
 * - arrows: Directional relationships between elements
 * 
 * This JSON is the ONLY context provided to the chatbot. The chatbot never sees the raw image.
 */
@Data
public class AnalysisResult {
    private String documentId;
    private int page;
    private List<Element> elements;
    
    @Data
    public static class Element {
        private String type; // "text", "crossedOut", "diagram", "marginNote", "arrow", "highlight"
        private String content;
        private String originalContent; // For crossedOut type
        private String crossedOutContent; // For crossedOut type
        private String description; // For diagrams
        private Object location; // "top-right", "left-margin", etc.
        private BoundingBox bbox; // x, y, width, height for UI overlay
        
        // Diagram-specific
        private List<DiagramElement> diagramElements;
    }
    
    @Data
    public static class BoundingBox {
        private int x;
        private int y;
        private int width;
        private int height;
    }
    
    @Data
    public static class DiagramElement {
        private String type; // "box", "arrow"
        private String label;
        private int x;
        private int y;
        private String direction; // for arrows: "A->B"
    }
}
