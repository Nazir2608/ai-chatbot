package com.aichatbot.service.llm;

import com.aichatbot.model.AnalysisResult;
import com.aichatbot.model.ChatMessage;
import java.util.List;

/**
 * Strategy Interface for LLM Providers
 * 
 * Abstracts the interaction with AI models.
 * Implementations handle the specific API formats (Ollama vs OpenAI).
 * 
 * Two main operations:
 * 1. analyzeImage: Convert handwritten note image -> Structured JSON (AnalysisResult)
 * 2. chat: Answer questions based on the analysis context
 */
public interface LlmService {
    
    /**
     * Analyze an image using a Vision-Language Model
     * 
     * @param imageBase64 Base64 encoded image (PNG/JPEG)
     * @param documentId Document identifier for tracking
     * @return Structured analysis of the handwritten content
     */
    AnalysisResult analyzeImage(String imageBase64, String documentId);
    
    /**
     * Chat with the document context
     * 
     * @param userMessage Current question from user
     * @param analysisJson The structured analysis (context)
     * @param history Previous messages for context
     * @return AI response text
     */
    String chat(String userMessage, String analysisJson, List<ChatMessage> history);
}
