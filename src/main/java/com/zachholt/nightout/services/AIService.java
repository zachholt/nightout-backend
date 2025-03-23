package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.zachholt.nightout.models.ai.ChatMessage;
import com.zachholt.nightout.models.ai.ChatRequest;
import com.zachholt.nightout.models.ai.ChatResponse;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLException;

@Service
public class AIService {
    private final WebClient webClient;
    
    @Value("${ai.default.model}")
    private String defaultModel;

    public AIService(@Value("${ai.api.url}") String apiUrl, 
                     @Value("${ai.api.token}") String authToken) {
        // Configure HTTP client with SSL context that trusts all certificates
        // WARNING: This should be replaced with proper certificate handling in production
        HttpClient httpClient;
        
        try {
            // Create an SSL context that trusts all certificates
            SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
            
            // Configure HTTP client with the SSL context
            httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext));
                
        } catch (Exception e) {
            System.err.println("Error setting up SSL context: " + e.getMessage());
            e.printStackTrace();
            // Fallback to non-secure client
            httpClient = HttpClient.create();
        }
        
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", authToken)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
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
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    System.err.println("Error in models request: " + e.getMessage());
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
        // Set default model if not specified
        if (request.getModel() == null) {
            request.setModel(defaultModel);
        }
        
        // These are the exact paths used by the GenAI API
        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(error -> {
                            System.err.println("API Error: " + error);
                            return Mono.error(new RuntimeException("API Error: " + error));
                        })
                )
                .bodyToFlux(ChatResponse.class)
                .doOnError(e -> {
                    System.err.println("Error in streaming chat request: " + e.getMessage());
                    e.printStackTrace();
                })
                .onErrorResume(e -> {
                    // Create an error response object
                    ChatResponse errorResponse = new ChatResponse();
                    errorResponse.setId("error");
                    errorResponse.setObject("error");
                    errorResponse.setModel(request.getModel());
                    
                    return Flux.just(errorResponse);
                });
    }
    
    /**
     * Add location context to chat requests
     * @param request The chat request to enhance with location context
     */
    public void addLocationContextToRequest(ChatRequest request) {
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
        request.setStream(true); // Always enable streaming
        request.setTop_p(0.01);
        request.setMessages(messages);
        request.setStop(List.of("\\nUser:", "\\n User:", "User:", "User"));
        request.setStream_options(new ChatRequest.StreamOptions(true));
        
        return request;
    }
} 