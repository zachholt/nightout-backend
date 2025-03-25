package com.zachholt.nightout.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for chat interactions with the AI assistant")
public class ChatRequest {
    @Schema(description = "Current message from the user", example = "Tell me about good places to go out in Boston", required = true)
    private String userMessage;
    
    @Schema(description = "Previous messages in the conversation", nullable = true)
    private List<ChatMessage> history;

    public ChatRequest() {
    }

    public ChatRequest(String userMessage, List<ChatMessage> history) {
        this.userMessage = userMessage;
        this.history = history;
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
} 