package com.zachholt.nightout.controllers;

import com.zachholt.nightout.models.ChatRequest;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.services.AIChatService;
import com.zachholt.nightout.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final AIChatService aiChatService;
    private final UserService userService;

    @Autowired
    public ChatController(AIChatService aiChatService, UserService userService) {
        this.aiChatService = aiChatService;
        this.userService = userService;
    }

    @PostMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(
            Authentication authentication,
            @RequestBody ChatRequest chatRequest
    ) {
        User user = userService.getCurrentUser(authentication);
        
        // Use provided location or fall back to user's stored location
        Double latitude = chatRequest.getLatitude() != null ? chatRequest.getLatitude() : user.getLatitude();
        Double longitude = chatRequest.getLongitude() != null ? chatRequest.getLongitude() : user.getLongitude();
        
        return aiChatService.generateResponse(
            user,
            chatRequest.getMessage(),
            latitude,
            longitude
        );
    }
}

class ChatRequest {
    private String message;
    private Double latitude;
    private Double longitude;

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
} 