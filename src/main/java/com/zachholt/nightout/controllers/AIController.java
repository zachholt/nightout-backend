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
@RequestMapping("/api/ai")
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
     * Handle a chat request with non-streaming response
     * @param request The chat request
     * @return Chat completion response
     */
    @PostMapping("/chat")
    public Mono<ResponseEntity<String>> chat(@RequestBody ChatRequest request) {
        // Ensure streaming is disabled
        request.setStream(false);
        
        return aiService.chat(request)
                .map(response -> ResponseEntity.ok().body(response))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    System.err.println("Error in chat: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(500).body("Error: " + e.getMessage()));
                });
    }

    /**
     * Handle a chat request with streaming response using Server-Sent Events
     * @param request The chat request
     * @return Streaming chat response
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chatStream(@RequestBody ChatRequest request) {
        // Ensure streaming is enabled
        request.setStream(true);
        if (request.getStream_options() == null) {
            request.setStream_options(new ChatRequest.StreamOptions(true));
        }
        
        return aiService.chatStream(request)
                .onErrorResume(e -> {
                    System.err.println("Error in chat stream: " + e.getMessage());
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
     * Convenience endpoint for simple text messages
     * @param message Text message from user
     * @param location Optional location parameter (defaults to Boston if not provided)
     * @return Chat response as String
     */
    @PostMapping(value = "/chat/simple")
    public Mono<ResponseEntity<String>> simpleChat(@RequestBody String message, 
                                   @RequestParam(required = false) String location) {
        ChatRequest request = aiService.createContextualRequest(message, location, false);
        return aiService.chat(request)
                .map(response -> ResponseEntity.ok().body(response))
                .onErrorResume(e -> {
                    System.err.println("Error in simple chat: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(500)
                            .body("Error processing your request. Please try again."));
                });
    }

    /**
     * Convenience endpoint for contextual chats (non-streaming)
     * @param message Text message from user
     * @param location Optional location parameter
     * @return Chat response as String
     */
    @PostMapping(value = "/chat/contextual")
    public Mono<ResponseEntity<String>> contextualChat(@RequestBody String message,
                                      @RequestParam(required = false) String location) {
        ChatRequest request = aiService.createContextualRequest(message, location, false);
        return aiService.chat(request)
                .map(response -> ResponseEntity.ok().body(response))
                .onErrorResume(e -> {
                    System.err.println("Error in contextual chat: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(500)
                            .body("Error processing your request. Please try again."));
                });
    }

    /**
     * Convenience endpoint for streaming contextual chats
     * @param message Text message from user
     * @param location Optional location parameter
     * @return Streaming chat response
     */
    @PostMapping(value = "/chat/contextual/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> contextualChatStream(@RequestBody String message,
                                                 @RequestParam(required = false) String location) {
        ChatRequest request = aiService.createContextualRequest(message, location, true);
        return aiService.chatStream(request)
                .onErrorResume(e -> {
                    System.err.println("Error in contextual chat stream: " + e.getMessage());
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