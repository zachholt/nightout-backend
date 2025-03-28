package com.zachholt.nightout.controllers;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Chat", description = "Chat API with NightOut AI assistant")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Stream chat endpoint that forwards the Server-Sent Events directly from the AI API
     */
    @Operation(
        summary = "Stream chat with AI assistant",
        description = "Get streaming response from AI assistant (Server-Sent Events)"
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
    
    private List<Map<String, Object>> convertToAiMessages(ChatRequest chatRequest) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // Add history messages
        if (chatRequest.getHistory() != null) {
            for (ChatMessage message : chatRequest.getHistory()) {
                Map<String, Object> aiMessage = new HashMap<>();
                aiMessage.put("role", message.isUser() ? "user" : "assistant");
                aiMessage.put("content", message.getText());
                messages.add(aiMessage);
            }
        }
        
        // Add current user message
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", chatRequest.getUserMessage());
        messages.add(userMessage);
        
        return messages;
    }

    /**
     * Endpoint for chat API that returns a text response
     */
    @Operation(
        summary = "Chat with AI assistant",
        description = "Get a response from the AI assistant based on the provided messages"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully returned chat response"
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request format"),
        @ApiResponse(responseCode = "500", description = "Server error or AI API unavailable")
    })
    @PostMapping("/chat")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<String> chat(@RequestBody ChatRequest chatRequest) {
        logger.info("Processing chat request");
        
        try {
            Map<String, Object> response = aiService.chatCompletion(chatRequest);
            
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    if (message != null && message.containsKey("content")) {
                        String content = (String) message.get("content");
                        return ResponseEntity.ok(content);
                    }
                }
            }
            
            String errorMessage = "I'm sorry, I couldn't process your request.";
            return ResponseEntity.internalServerError().body(errorMessage);
        } catch (Exception e) {
            logger.error("Error processing chat request", e);
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }
}