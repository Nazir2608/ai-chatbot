package com.aichatbot.service;

import com.aichatbot.dto.ChatResponse;
import com.aichatbot.model.ChatMessage;
import com.aichatbot.service.llm.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat Session Management Service
 * 
 * Manages conversation history per document.
 * In production, this should use Redis or a database.
 * For learning/MVP, we use in-memory ConcurrentHashMap.
 * 
 * Key insight: Each document gets its own chat history context,
 * ensuring questions about Document A don't leak info from Document B.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final DocumentService documentService;
    private final LlmService llmService;
    
    // In-memory session storage: documentId -> List of messages
    // TODO: Replace with Redis for production scalability
    private final Map<String, List<ChatMessage>> sessions = new ConcurrentHashMap();

    /**
     * Process chat message and return AI response
     * 
     * Flow:
     * 1. Load analysis.json for the document (context)
     * 2. Retrieve or create chat history for this session
     * 3. Call LLM with context + history + new question
     * 4. Store exchange in history
     * 5. Return response
     */
    public ChatResponse chat(String documentId, String userMessage) {
        log.info("Chat request for doc: {}, message: {}", documentId, 
                userMessage.length() > 50 ? userMessage.substring(0, 50) + "..." : userMessage);
        
        try {
            // Load document analysis (context)
            String analysisJson = documentService.loadAnalysis(documentId);
            
            // Get or create session history
            List<ChatMessage> history = sessions.computeIfAbsent(documentId, k -> new ArrayList<>());
            
            // Call LLM
            String aiResponse = llmService.chat(userMessage, analysisJson, history);
            
            // Store exchange
            history.add(createMessage("user", userMessage, documentId));
            history.add(createMessage("assistant", aiResponse, documentId));
            
            // Limit history size (prevent token overflow)
            if (history.size() > 20) {
                history = history.subList(history.size() - 20, history.size());
                sessions.put(documentId, history);
            }
            
            return ChatResponse.builder()
                    .response(aiResponse)
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("Chat error", e);
            return ChatResponse.builder()
                    .success(false)
                    .error("Failed to process message: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Clear chat history for a document
     */
    public void clearHistory(String documentId) {
        sessions.remove(documentId);
        log.info("Cleared chat history for doc: {}", documentId);
    }

    private ChatMessage createMessage(String role, String content, String documentId) {
        ChatMessage msg = new ChatMessage();
        msg.setRole(role);
        msg.setContent(content);
        msg.setDocumentId(documentId);
        return msg;
    }
}
