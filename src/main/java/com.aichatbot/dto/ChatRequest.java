package com.aichatbot.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String documentId;
}
