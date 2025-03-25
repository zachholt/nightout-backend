package com.zachholt.nightout.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.models.ChatRequest;
import com.zachholt.nightout.services.AiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@Tag(name = "Chat", description = "Chat API with NightOut AI assistant")
public class ChatController {

    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public ChatController(AiService aiService, ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    /**
     * Stream chat endpoint that forwards the Server-Sent Events directly from the AI API
     */
    @Operation(
        summary = "* Stream chat with AI assistant",
        description = "Get streaming response from AI assistant (Server-Sent Events). " +
                     "* This endpoint is currently experiencing issues."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully streamed chat response",
            content = @Content(mediaType = "text/event-stream")
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request format"),
        @ApiResponse(responseCode = "500", description = "Server error or AI API unavailable")
    })
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest chatRequest) {
        List<Map<String, Object>> messages = convertToAiMessages(chatRequest);
        
        // Just pass through the raw streaming response directly
        return aiService.streamChatCompletion(messages)
            .timeout(Duration.ofMinutes(2));
    }
    
    @Operation(
        summary = "* Chat with AI assistant",
        description = "Get a single response from AI assistant. " +
                     "* This endpoint is currently experiencing issues."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully returned chat response",
            content = @Content(schema = @Schema(implementation = ChatMessage.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request format"),
        @ApiResponse(responseCode = "500", description = "Server error or AI API unavailable")
    })
    @PostMapping
    public ChatMessage chat(@RequestBody ChatRequest chatRequest) {
        List<Map<String, Object>> messages = convertToAiMessages(chatRequest);
        Map<String, Object> response = aiService.chatCompletion(messages);
        
        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null && message.containsKey("content")) {
                    String content = (String) message.get("content");
                    return new ChatMessage(content, false);
                }
            }
        }
        
        return new ChatMessage("I'm sorry, I couldn't process your request.", false);
    }
    
    private List<Map<String, Object>> convertToAiMessages(ChatRequest chatRequest) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // Add history messages
        if (chatRequest.getHistory() != null) {
            for (ChatMessage message : chatRequest.getHistory()) {
                Map<String, Object> aiMessage = new HashMap<>();
                aiMessage.put("role", message.isUser() ? "user" : "assistant");
                
                List<Map<String, Object>> content = new ArrayList<>();
                Map<String, Object> textContent = new HashMap<>();
                textContent.put("type", "text");
                textContent.put("text", message.getText());
                content.add(textContent);
                
                aiMessage.put("content", content);
                messages.add(aiMessage);
            }
        }
        
        // Add current user message
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", chatRequest.getUserMessage());
        content.add(textContent);
        
        userMessage.put("content", content);
        messages.add(userMessage);
        
        return messages;
    }
} 