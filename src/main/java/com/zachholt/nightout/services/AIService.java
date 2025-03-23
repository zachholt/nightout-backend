package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.zachholt.nightout.models.ai.ChatRequest;
import com.zachholt.nightout.models.ai.ChatResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AIService {
    private final WebClient webClient;
    
    @Value("${ai.default.model}")
    private String defaultModel;

    public AIService(@Value("${ai.api.url}") String apiUrl, 
                     @Value("${ai.api.token}") String authToken) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", authToken)
                .build();
    }

    /**
     * Get a list of available models from the AI API
     * @return Flux of model information as String (can be improved with model class)
     */
    public Mono<String> getAvailableModels() {
        return webClient.get()
                .uri("/models")
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Send a chat request and get a non-streaming response
     * @param request The chat request
     * @return A Mono with the chat response
     */
    public Mono<String> chat(ChatRequest request) {
        // Set stream to false just to be safe
        request.setStream(false);
        
        // Set default model if not specified
        if (request.getModel() == null) {
            request.setModel(defaultModel);
        }
        
        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Send a chat request and get a streaming response
     * @param request The chat request
     * @return A Flux of chat responses for streaming
     */
    public Flux<ChatResponse> chatStream(ChatRequest request) {
        // Ensure that stream is true
        request.setStream(true);
        
        // Set default model if not specified
        if (request.getModel() == null) {
            request.setModel(defaultModel);
        }
        
        if (request.getStream_options() == null) {
            request.setStream_options(new ChatRequest.StreamOptions(true));
        }
        
        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(ChatResponse.class);
    }
} 