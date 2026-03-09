package com.aichatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI Chatbot Application for Handwritten Notes
 * 
 * Core Concept: This is NOT an OCR application. It uses Vision-Language Models (VLM)
 * like LLaVA to understand the semantic content of handwritten notes including
 * diagrams, arrows, crossed-out text, and margin annotations.
 * 
 * Architecture:
 * - Storage Layer: Abstracted (Local filesystem vs S3) via Strategy Pattern
 * - LLM Layer: Switchable between Ollama (local) and OpenAI/Anthropic (cloud)
 * - Processing: Image -> Preprocessing -> VLM Analysis -> Structured JSON -> Chat Context
 */
@SpringBootApplication
public class AiChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiChatbotApplication.class, args);
    }
}
