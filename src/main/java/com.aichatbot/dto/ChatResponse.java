package com.aichatbot.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Outgoing chat response to frontend
 */
@Data
@Builder
public class ChatResponse {
    private String response;
    private boolean success;
    private String error;
}
