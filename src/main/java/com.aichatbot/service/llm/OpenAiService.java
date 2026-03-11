package com.aichatbot.service.llm;

import com.aichatbot.model.AnalysisResult;
import com.aichatbot.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Cloud LLM Implementation
 * 
 * Uses GPT-4 Vision (gpt-4o) for superior handwriting recognition.
 * Requires API key: OPENAI_API_KEY environment variable or llm.openai.api-key in config
 * 
 * Benefits over Ollama:
 * - Better handwriting accuracy
 * - Faster response (if you have good internet)
 * - No local GPU required
 * 
 * Cost: ~./setup-ai-chatbot.sh.01-0.02 per image analysis
 */
@Slf4j
@Service
public class OpenAiService implements LlmService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${llm.openai.api-key}")
    private String apiKey;
    
    @Value("${llm.openai.model:gpt-4o}")
    private String model;

    public OpenAiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AnalysisResult analyzeImage(String imageBase64, String documentId) {
        log.info("Analyzing image with OpenAI model: {} for doc: {}", model, documentId);
        
        String prompt = "Analyze this handwritten note image. Extract all text, diagrams, crossed-out corrections, and margin notes. Return as structured JSON with schema: {elements: [{type: 'text'|'crossedOut'|'diagram'|'marginNote', content: string, originalContent?: string, crossedOutContent?: string, location?: string}]}";

        try {
            List<Map<String, Object>> messages = List.of(Map.of("role", "user", "content",
                    List.of(Map.of("type", "text", "text", prompt),
                        Map.of("type", "image_url", "image_url", Map.of("url", "data:image/png;base64," + imageBase64)))));

            Map<String, Object> requestBody = Map.of("model", model,
                "messages", messages,
                "max_tokens", 4096,
                "response_format", Map.of("type", "json_object")
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse OpenAI response structure
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            
            // OpenAI returns the JSON directly in content
            AnalysisResult result = objectMapper.readValue(content, AnalysisResult.class);
            result.setDocumentId(documentId);
            
            log.info("Successfully analyzed image with OpenAI for doc: {}", documentId);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to analyze image with OpenAI", e);
            throw new RuntimeException("OpenAI analysis failed", e);
        }
    }

    @Override
    public String chat(String userMessage, String analysisJson, List<ChatMessage> history) {
        log.info("Chatting with OpenAI");

        List<Map<String, Object>> messages = new ArrayList<>();

        // System message with context
        messages.add(Map.of(
            "role", "system",
            "content", "You are analyzing handwritten notes. Use ONLY this data: " + analysisJson + 
                      ". If answering about crossed-out text, mention both old and new. If info is missing, say so."
        ));
        
        // Add history
        for (ChatMessage msg : history) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        
        // Add current message
        messages.add(Map.of("role", "user", "content", userMessage));

        try {
            Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o", // or gpt-3.5-turbo for chat to save costs
                "messages", messages,
                "max_tokens", 1000
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            
            return (String) message.get("content");
            
        } catch (Exception e) {
            log.error("Chat failed with OpenAI", e);
            return "Sorry, I encountered an error.";
        }
    }
}
