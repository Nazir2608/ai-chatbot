package com.aichatbot.controller;

import com.aichatbot.dto.ChatRequest;
import com.aichatbot.dto.ChatResponse;
import com.aichatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Send message and get AI response
     */
    @PostMapping("/{documentId}")
    public ResponseEntity<ChatResponse> chat(@PathVariable String documentId, @RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ChatResponse.builder()
                            .success(false)
                            .error("Message cannot be empty")
                            .build());
        }
        ChatResponse response = chatService.chat(documentId, request.getMessage());
        return ResponseEntity.ok(response);
    }

    /**
     * Clear chat history for a document
     */
    @PostMapping("/{documentId}/clear")
    public ResponseEntity<Void> clearHistory(@PathVariable String documentId) {
        chatService.clearHistory(documentId);
        return ResponseEntity.ok().build();
    }
}
