package com.zachholt.nightout.models;

import java.util.List;

public class ChatRequest {
    private String userMessage;
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