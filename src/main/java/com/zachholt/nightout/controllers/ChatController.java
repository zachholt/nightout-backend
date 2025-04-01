package com.zachholt.nightout.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.services.AiService;
import com.zachholt.nightout.services.UserService;
//import com.zachholt.nightout.services.ChatMessageService;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;
import java.lang.StringBuilder;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Chat", description = "Chat API with NightOut AI assistant")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private AiService aiService;
    
    @Autowired
    private UserService userService;
    
    //@Autowired
    //private ChatMessageService chatMessageService;
    
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
    // @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // public Flux<String> streamChat(@RequestBody ChatRequest chatRequest) {
    //     List<Map<String, Object>> messages = convertToAiMessages(chatRequest);
    //     logger.info("Processing stream chat request with message: {}", chatRequest.getUserMessage());
        
    //     // Save the user message
    //     chatMessageService.saveMessage(
    //         chatRequest.getUserMessage(), 
    //         true, 
    //         chatRequest.getSessionId(), 
    //         chatRequest.getUserEmail()
    //     );
        
    //     // Stream will be handled in the client - we don't save AI messages
    //     // directly as they come in chunks
    //     return aiService.streamChatCompletion(messages)
    //         .timeout(Duration.ofMinutes(2));
    // }
    
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
        
        // Add current user message, enriched with location information if available
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        // Enhance message with location context if available
        String messageContent = chatRequest.getUserMessage();
        
        // Check if message is asking about where people are
        boolean isAskingAboutCrowds = messageContent.toLowerCase().contains("where's everyone at") || 
                                      messageContent.toLowerCase().contains("where is everyone") ||
                                      messageContent.toLowerCase().contains("crowd") ||
                                      messageContent.toLowerCase().contains("busy place") ||
                                      messageContent.toLowerCase().contains("popular spot");
        
        StringBuilder contextBuilder = new StringBuilder();
        
        // Add location context if available
        if (chatRequest.getLatitude() != null && chatRequest.getLongitude() != null) {
            if (chatRequest.getLocationName() != null && !chatRequest.getLocationName().isEmpty()) {
                contextBuilder.append("My current location is near ")
                              .append(chatRequest.getLocationName())
                              .append(" (coordinates: ")
                              .append(chatRequest.getLatitude())
                              .append(", ")
                              .append(chatRequest.getLongitude())
                              .append("). ");
            } else {
                contextBuilder.append("My current coordinates are: ")
                              .append(chatRequest.getLatitude())
                              .append(", ")
                              .append(chatRequest.getLongitude())
                              .append(". ");
            }
            
            // Add crowd information if the user is asking about it
            if (isAskingAboutCrowds) {
                try {
                    // Default radius of 2km
                    double radiusInMeters = 2000.0;
                    
                    // Get users in the area
                    Collection<?> usersNearby = userService.getUsersByLocation(
                        chatRequest.getLatitude(), 
                        chatRequest.getLongitude(), 
                        radiusInMeters
                    );
                    
                    int userCount = usersNearby != null ? usersNearby.size() : 0;
                    
                    // Get current time
                    LocalDateTime now = LocalDateTime.now();
                    String dayOfWeek = now.getDayOfWeek().toString();
                    String timeOfDay = now.format(DateTimeFormatter.ofPattern("HH:mm"));
                    
                    contextBuilder.append("There are currently ")
                                  .append(userCount)
                                  .append(" people using NightOut near me. ")
                                  .append("It's ")
                                  .append(dayOfWeek)
                                  .append(" at ")
                                  .append(timeOfDay)
                                  .append(". ");
                                  
                    if (userCount > 0) {
                        contextBuilder.append("Popular locations right now include: ");
                        // Note: This is where you would add actual location data if available
                        contextBuilder.append("local bars, restaurants, and entertainment venues based on the time and day. ");
                    }
                } catch (Exception e) {
                    logger.error("Error fetching user density data", e);
                }
            }
        }
        
        // Add time context
        LocalDateTime now = LocalDateTime.now();
        String dayOfWeek = now.getDayOfWeek().toString();
        String timeOfDay = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        contextBuilder.append("It's currently ")
                      .append(dayOfWeek)
                      .append(" at ")
                      .append(timeOfDay)
                      .append(". ");
        
        // Add context to the beginning of the message
        messageContent = contextBuilder.toString() + messageContent;
        logger.info("Enhanced user message with context: {}", messageContent);
        
        userMessage.put("content", messageContent);
        messages.add(userMessage);
        
        return messages;
    }

    /**
     * Endpoint for mistral-vllm model that returns formatted response
     */
    @Operation(
        summary = "Chat with Mistral VLLM model",
        description = "Get a single response from Mistral VLLM assistant with standardized output format"
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
    public ResponseEntity<String> mistralChat(@RequestBody ChatRequest chatRequest) {
        logger.info("Processing chat request with message: {}", chatRequest.getUserMessage());
        
        // // Save the user message
        // chatMessageService.saveMessage(
        //     chatRequest.getUserMessage(), 
        //     true, 
        //     chatRequest.getSessionId(), 
        //     chatRequest.getUserEmail()
        // );
        
        Map<String, Object> response = aiService.chatCompletion(chatRequest);
        
        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null && message.containsKey("content")) {
                    String content = (String) message.get("content");
                    
                    // Save the AI response
                    return ResponseEntity.ok(content);
                }
            }
        }
        
        String errorMessage = "I'm sorry, I couldn't process your request.";
        // Save the error message
        return ResponseEntity.internalServerError().body(errorMessage);
    }
}