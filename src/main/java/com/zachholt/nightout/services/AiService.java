package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final WebClient webClient;
    
    @Value("${ai.api.url}")
    private String aiApiUrl;
    
    @Value("${ai.api.token}")
    private String aiApiToken;
    
    public AiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(aiApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", aiApiToken)
                .build();
    }
    
    /**
     * Stream chat completion from AI API. Returns the raw streaming response as a Flux of strings.
     * Each string represents a chunk of the stream in SSE format.
     */
    public Flux<String> streamChatCompletion(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = Map.of(
            "model", "claude-3-7",
            "top_p", 0.01,
            "stream", true,
            "stream_options", Map.of("include_usage", true),
            "messages", messages,
            "stop", List.of("\nUser:", "\n User:", "User:", "User")
        );
        
        // Return the raw text stream directly without transformation
        return webClient.post()
                .uri("/v2/serve/chat/completions")
                .body(BodyInserters.fromValue(requestBody))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);
    }
    
    public Map<String, Object> chatCompletion(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = Map.of(
            "model", "claude-3-7",
            "top_p", 0.01,
            "stream", false,
            "messages", messages,
            "stop", List.of("\nUser:", "\n User:", "User:", "User")
        );
        
        return webClient.post()
                .uri("/v2/serve/chat/completions")
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
} 