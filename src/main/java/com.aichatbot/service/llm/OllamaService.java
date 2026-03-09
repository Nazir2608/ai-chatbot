package com.aichatbot.service.llm;

import com.aichatbot.model.AnalysisResult;
import com.aichatbot.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Ollama Local LLM Implementation
 * 
 * Uses locally running Ollama instance (http://localhost:11434)
 * 
 * CRITICAL: Must use a VISION model (llava, moondream, cogvlm2).
 * Text-only models will fail to process images.
 * 
 * To setup:
 * 1. Install Ollama: https://ollama.ai
 * 2. Pull model: ollama pull llava
 * 3. Run: ollama serve
 */
@Slf4j
@Service
public class OllamaService implements LlmService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${llm.ollama.model:llava}")
    private String modelName;
    
    @Value("${llm.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    public OllamaService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(ollamaUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AnalysisResult analyzeImage(String imageBase64, String documentId) {
        log.info("Analyzing image with Ollama model: {} for doc: {}", modelName, documentId);
        
        // This is the MAGIC PROMPT that extracts semantic meaning, not just OCR
        String prompt = """
            You are an expert analyst of handwritten notes.
            Examine the attached image CAREFULLY.
            
            YOUR TASK:
            Extract EVERY meaningful element including:
            
            1. Handwritten text — transcribe EXACTLY. Mark unclear parts with [UNCLEAR].
            2. CROSSED-OUT TEXT — provide:
               - original_text
               - crossed_out_text
               - reason (if inferable)
            3. DIAGRAMS / FLOWCHARTS — describe:
               - All boxes(labels)
               - Arrows (direction, start→end)
               - Connections
            4. MARGIN ANNOTATIONS — note location (top-right, left-margin) & content
            5. ARROWS / CONNECTORS — list {from: "Box A", to: "Box B", direction: "right"}
            6. HIGHLIGHTS / UNDERLINED — note the text they cover
            
            OUTPUT STRICT JSON — NO EXPLANATIONS. Use this schema:
            {
              "documentId": "DOC_ID",
              "page": 1,
              "elements": [
                {"type": "text", "content": "...", "bbox": {"x":0,"y":0,"w":0,"h":0}},
                {"type": "crossedOut", "originalContent": "...", "crossedOutContent": "..."},
                {"type": "diagram", "description": "...", "elements": [...]},
                {"type": "marginNote", "location": "...", "content": "..."}
              ]
            }
            """.replace("DOC_ID", documentId);

        try {
            Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "prompt", prompt,
                "images", List.of(imageBase64),
                "stream", false,
                "format", "json"
            );

            String response = webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                        status -> status.isError(),
                        clientResponse -> Mono.error(new RuntimeException("Ollama API error"))
                    )
                    .bodyToMono(String.class)
                    .block();

            // Parse Ollama response - it returns JSON with a "response" field containing our JSON
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            String jsonContent = (String) responseMap.get("response");
            
            AnalysisResult result = objectMapper.readValue(jsonContent, AnalysisResult.class);
            log.info("Successfully analyzed image for doc: {}", documentId);
            return result;
            
        } catch (Exception e) {
            log.error("Failed to analyze image with Ollama", e);
            throw new RuntimeException("Image analysis failed", e);
        }
    }

    @Override
    public String chat(String userMessage, String analysisJson, List<ChatMessage> history) {
        log.info("Chatting with Ollama for context length: {} chars", analysisJson.length());
        
        String systemPrompt = """
            You are a helpful assistant for handwritten notes.
            Below is the structured understanding of the note the user is asking about.
            
            ANALYSIS_DATA: """ + analysisJson + """
            
            INSTRUCTIONS:
            - ANSWER ONLY using information from the ANALYSIS_DATA above.
            - If the data contains crossed-out text, say: "The original said X but it was crossed out and replaced with Y".
            - For diagrams, describe using the diagram objects.
            - If you cannot answer, say: "This information isn't in the note."
            - NEVER make up information not present in the analysis.
            """;

        try {
            // Build conversation history for context
            StringBuilder conversation = new StringBuilder(systemPrompt);
            for (ChatMessage msg : history) {
                conversation.append("\n").append(msg.getRole()).append(": ").append(msg.getContent());
            }
            conversation.append("\nuser: ").append(userMessage);
            conversation.append("\nassistant: ");

            Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "prompt", conversation.toString(),
                "stream", false
            );

            String response = webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            return (String) responseMap.get("response");
            
        } catch (Exception e) {
            log.error("Chat failed with Ollama", e);
            return "Sorry, I encountered an error processing your question.";
        }
    }
}
