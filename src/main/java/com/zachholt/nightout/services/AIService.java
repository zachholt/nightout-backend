package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.zachholt.nightout.models.ai.ChatMessage;
import com.zachholt.nightout.models.ai.ChatRequest;
import com.zachholt.nightout.models.ai.ChatResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

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
        
        // Add location context to the request
        addLocationContextToRequest(request);
        
        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    System.err.println("Error in chat request: " + e.getMessage());
                    e.printStackTrace();
                    return Mono.just("Error: " + e.getMessage());
                });
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
        
        // Add location context to the request
        addLocationContextToRequest(request);
        
        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(ChatResponse.class)
                .onErrorResume(e -> {
                    System.err.println("Error in streaming chat request: " + e.getMessage());
                    e.printStackTrace();
                    return Flux.empty();
                });
    }
    
    /**
     * Add location context to chat requests
     * @param request The chat request to enhance with location context
     */
    private void addLocationContextToRequest(ChatRequest request) {
        List<ChatMessage> messages = request.getMessages();
        
        if (messages == null) {
            messages = new ArrayList<>();
            request.setMessages(messages);
        }
        
        // If there's no system message, add one with location context
        boolean hasSystemMessage = messages.stream()
                .anyMatch(msg -> "system".equals(msg.getRole()));
        
        if (!hasSystemMessage) {
            List<ChatMessage> newMessages = new ArrayList<>();
            
            // Add system message with location context
            ChatMessage systemMessage = new ChatMessage(
                "system", 
                "You are a helpful assistant for the NightOut application. " + 
                "Provide specific recommendations about Boston, Massachusetts when asked about places, " +
                "restaurants, events, activities, or nightlife. Include details like addresses, " +
                "pricing, and why they're recommended."
            );
            
            newMessages.add(systemMessage);
            newMessages.addAll(messages);
            request.setMessages(newMessages);
        }
    }
    
    /**
     * Create a chat request with location context for a simple message
     * @param userMessage The user's message
     * @param location Optional location (defaults to Boston, MA if not provided)
     * @param stream Whether to enable streaming
     * @return A prepared chat request with location context
     */
    public ChatRequest createContextualRequest(String userMessage, String location, boolean stream) {
        String locationContext = location != null ? location : "Boston, Massachusetts";
        
        List<ChatMessage> messages = new ArrayList<>();
        
        // System message with location context
        messages.add(new ChatMessage(
            "system", 
            "You are a helpful assistant for the NightOut application. " +
            "Provide specific recommendations about " + locationContext + " when asked about places, " +
            "restaurants, events, activities, or nightlife. Include details like addresses, " +
            "pricing, and why they're recommended."
        ));
        
        // User message
        messages.add(new ChatMessage("user", userMessage));
        
        // Create request
        ChatRequest request = new ChatRequest();
        request.setModel(defaultModel);
        request.setStream(stream);
        request.setTop_p(0.01);
        request.setMessages(messages);
        request.setStop(List.of("\\nUser:", "\\n User:", "User:", "User"));
        
        if (stream) {
            request.setStream_options(new ChatRequest.StreamOptions(true));
        }
        
        return request;
    }
} 