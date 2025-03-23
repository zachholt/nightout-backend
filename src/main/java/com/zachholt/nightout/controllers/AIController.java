package com.zachholt.nightout.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zachholt.nightout.models.ai.ChatRequest;
import com.zachholt.nightout.models.ai.ChatResponse;
import com.zachholt.nightout.services.AIService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v2/serve")
@CrossOrigin
public class AIController {

    private final AIService aiService;
    
    @Value("${ai.default.model}")
    private String defaultModel;

    @Autowired
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * Get all available AI models
     * @return List of models in JSON format
     */
    @GetMapping("/models")
    public Mono<ResponseEntity<String>> getModels() {
        return aiService.getAvailableModels()
                .map(response -> ResponseEntity.ok().body(response))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    System.err.println("Error fetching models: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body("Error: " + e.getMessage()));
                });
    }

    /**
     * Chat endpoint that supports both streaming and non-streaming
     * @param request The chat request
     * @return Streaming or non-streaming chat response
     */
    @PostMapping(value = "/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chatCompletions(@RequestBody ChatRequest request) {
        // Add location context if it's not already present
        aiService.addLocationContextToRequest(request);
        
        return aiService.chatStream(request)
                .onErrorResume(e -> {
                    System.err.println("Error in chat completions: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Create an error response object
                    ChatResponse errorResponse = new ChatResponse();
                    errorResponse.setId("error");
                    errorResponse.setObject("error");
                    errorResponse.setModel(request.getModel());
                    
                    return Flux.just(errorResponse);
                });
    }

    /**
     * Simple message endpoint (DEPRECATED - keeping for backward compatibility)
     * @param message Text message from user
     * @param location Optional location parameter (defaults to Boston if not provided)
     * @return Streaming chat response
     */
    @PostMapping(value = "/chat/message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> messageStream(
            @RequestBody String message,
            @RequestParam(required = false) String location) {
        
        // Create a contextual request with streaming enabled
        ChatRequest request = aiService.createContextualRequest(message, location, true);
        
        return aiService.chatStream(request)
                .onErrorResume(e -> {
                    System.err.println("Error in message stream: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Create an error response object
                    ChatResponse errorResponse = new ChatResponse();
                    errorResponse.setId("error");
                    errorResponse.setObject("error");
                    errorResponse.setModel(request.getModel());
                    
                    return Flux.just(errorResponse);
                });
    }
} 