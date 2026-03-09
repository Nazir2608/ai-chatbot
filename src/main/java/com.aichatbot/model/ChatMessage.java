package com.aichatbot.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Individual message in a chat conversation
 * role: "user" or "assistant"
 * content: The message text
 * timestamp: When sent
 * documentId: Which document this chat is about
 */
@Data
public class ChatMessage {
    private String role;
    private String content;
    private LocalDateTime timestamp;
    private String documentId;
    
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }
}
