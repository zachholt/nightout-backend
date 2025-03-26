package com.zachholt.nightout.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.models.ChatRequest;
import com.zachholt.nightout.services.AiService;
import com.zachholt.nightout.services.ChatMessageService;

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

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@Tag(name = "Chat", description = "Chat API with NightOut AI assistant")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private ChatMessageService chatMessageService;
    
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
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest chatRequest) {
        List<Map<String, Object>> messages = convertToAiMessages(chatRequest);
        logger.info("Processing stream chat request with message: {}", chatRequest.getUserMessage());
        
        // Save the user message
        chatMessageService.saveMessage(
            chatRequest.getUserMessage(), 
            true, 
            chatRequest.getSessionId(), 
            chatRequest.getUserEmail()
        );
        
        // Stream will be handled in the client - we don't save AI messages
        // directly as they come in chunks
        return aiService.streamChatCompletion(messages)
            .timeout(Duration.ofMinutes(2));
    }
    
    @Operation(
        summary = "Chat with AI assistant",
        description = "Get a single response from AI assistant"
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
        logger.info("Processing chat request with message: {}", chatRequest.getUserMessage());
        
        // Save the user message
        chatMessageService.saveMessage(
            chatRequest.getUserMessage(), 
            true, 
            chatRequest.getSessionId(), 
            chatRequest.getUserEmail()
        );
        
        Map<String, Object> response = aiService.chatCompletion(messages);
        
        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null && message.containsKey("content")) {
                    String content = (String) message.get("content");
                    
                    // Save the AI response
                    return chatMessageService.saveMessage(
                        content, 
                        false, 
                        chatRequest.getSessionId(), 
                        chatRequest.getUserEmail()
                    );
                }
            }
        }
        
        String errorMessage = "I'm sorry, I couldn't process your request.";
        // Save the error message
        return chatMessageService.saveMessage(
            errorMessage, 
            false, 
            chatRequest.getSessionId(), 
            chatRequest.getUserEmail()
        );
    }
    
    @Operation(
        summary = "Get chat history",
        description = "Get all messages for a specific session"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully returned chat history",
            content = @Content(schema = @Schema(implementation = ChatMessage.class))
        )
    })
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatMessageService.getMessagesBySessionId(sessionId));
    }
    
    @Operation(
        summary = "Get user chat sessions",
        description = "Get all chat sessions for a user"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully returned user sessions",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    @GetMapping("/sessions")
    public ResponseEntity<List<String>> getUserSessions(@RequestParam String email) {
        return ResponseEntity.ok(chatMessageService.getUserSessions(email));
    }
    
    @Operation(
        summary = "Delete chat session",
        description = "Delete all messages in a chat session"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully deleted session"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId, @RequestParam String email) {
        boolean deleted = chatMessageService.deleteSession(sessionId, email);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
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
} 