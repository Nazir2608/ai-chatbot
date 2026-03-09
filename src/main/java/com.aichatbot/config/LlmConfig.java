package com.aichatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.aichatbot.service.llm.LlmService;
import com.aichatbot.service.llm.OllamaService;
import com.aichatbot.service.llm.OpenAiService;

/**
 * LLM (Large Language Model) Configuration
 * 
 * Abstraction layer allowing seamless switching between:
 * - Ollama (local, private, free - requires GPU for good performance)
 * - OpenAI (cloud, GPT-4V for best handwriting understanding)
 * 
 * IMPORTANT: For handwritten notes with diagrams, you MUST use a Vision model.
 * Text-only models like GPT-3.5 or Gemma text variants will fail completely.
 */
@Configuration
public class LlmConfig {

    @Value("${llm.provider:ollama}")
    private String llmProvider;

    @Bean
    @Primary
    public LlmService llmService(OllamaService ollamaService, OpenAiService openAiService) {
        return switch (llmProvider.toLowerCase()) {
            case "openai" -> openAiService;
            case "ollama" -> ollamaService;
            default -> throw new IllegalArgumentException("Unknown LLM provider: " + llmProvider);
        };
    }
}
