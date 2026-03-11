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

    AnalysisResult analyzeImage(String imageBase64, String documentId);
    String chat(String userMessage, String analysisJson, List<ChatMessage> history);
}
