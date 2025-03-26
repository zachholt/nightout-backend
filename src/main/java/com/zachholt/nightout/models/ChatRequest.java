package com.zachholt.nightout.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for chat interactions with the AI assistant")
public class ChatRequest {
    @Schema(description = "Current message from the user", example = "Tell me about good places to go out in Boston", required = true)
    private String userMessage;
    
    @Schema(description = "Previous messages in the conversation", nullable = true)
    private List<ChatMessage> history;
    
    @Schema(description = "Session identifier to group conversation messages", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;
    
    @Schema(description = "Email of the user making the request (optional for anonymous chats)", example = "user@example.com")
    private String userEmail;

    public ChatRequest() {
    }

    public ChatRequest(String userMessage, List<ChatMessage> history, String sessionId, String userEmail) {
        this.userMessage = userMessage;
        this.history = history;
        this.sessionId = sessionId;
        this.userEmail = userEmail;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public List<ChatMessage> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessage> history) {
        this.history = history;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
} 