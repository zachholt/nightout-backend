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
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handle a chat request with non-streaming response
     * @param request The chat request
     * @return Chat completion response
     */
    @PostMapping("/chat")
    public Mono<ResponseEntity<String>> chat(@RequestBody ChatRequest request) {
        return aiService.chat(request)
                .map(response -> ResponseEntity.ok().body(response))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handle a chat request with streaming response using Server-Sent Events
     * @param request The chat request
     * @return Streaming chat response
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chatStream(@RequestBody ChatRequest request) {
        return aiService.chatStream(request);
    }

    /**
     * Convenience endpoint for simple text messages
     * @param message Text message from user
     * @return Streaming chat response
     */
    @PostMapping(value = "/chat/simple", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> simpleChatStream(@RequestBody String message) {
        ChatRequest request = ChatRequest.createSimpleRequest(defaultModel, message, true);
        return aiService.chatStream(request);
    }
} 