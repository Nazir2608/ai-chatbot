package com.aichatbot.dto;

import lombok.Data;

/**
 * Incoming chat message from frontend
 */
@Data
public class ChatRequest {
    private String message;
    private String documentId;
}
