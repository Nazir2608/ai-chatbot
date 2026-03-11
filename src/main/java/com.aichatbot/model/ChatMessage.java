package com.aichatbot.model;

import lombok.Data;
import java.time.LocalDateTime;

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
