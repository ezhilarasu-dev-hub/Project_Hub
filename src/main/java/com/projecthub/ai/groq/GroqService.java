package com.projecthub.ai.groq;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    private static final Logger logger = LoggerFactory.getLogger(GroqService.class);

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final RestClient restClient;

    public GroqService(
            @Value("${groq.api.key:}") String apiKey,
            @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}") String apiUrl,
            @Value("${groq.model:qwen/qwen3.8-27b}") String model) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiUrl = apiUrl;
        this.model = model;
        this.restClient = RestClient.builder().build();
    }

    public boolean isApiKeyConfigured() {
        return StringUtils.hasText(apiKey) && !apiKey.equalsIgnoreCase("your_groq_api_key_here");
    }

    public String generateChatCompletion(String systemPrompt, String userMessage) {
        if (!isApiKeyConfigured()) {
            logger.warn("GROQ_API_KEY is not configured or contains placeholder. AI chat completion cannot contact Groq.");
            return null;
        }

        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (StringUtils.hasText(systemPrompt)) {
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestPayload = Map.of(
                    "model", model,
                    "messages", messages,
                    "temperature", 0.2,
                    "max_tokens", 800
            );

            GroqApiResponse response = restClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(GroqApiResponse.class);

            if (response != null && response.choices != null && !response.choices.isEmpty()) {
                return response.choices.get(0).message.content;
            } else {
                logger.error("Groq API returned an empty choices list.");
                return "The AI assistant was unable to generate a response at this time. Please try again.";
            }

        } catch (Exception ex) {
            logger.error("Error communicating with Groq API: {}", ex.getMessage());
            return "Unable to reach Groq AI service (" + ex.getClass().getSimpleName() + "). Please verify backend network connectivity and your GROQ_API_KEY.";
        }
    }

    // Static nested DTOs for Groq OpenAI-compatible format
    public static class GroqApiResponse {
        public List<Choice> choices;
    }

    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String role;
        public String content;
    }
}
