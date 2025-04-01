package com.zachholt.nightout.controllers;

import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zachholt.nightout.models.ChatMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRequest {
    private String model;
    private Double temperature;
    private Double topP;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private Integer maxTokens;
    private Integer n;
    private List<String> stop = new ArrayList<>();
    private Boolean stream = false;
    private Integer seed;
    private List<Message> messages = new ArrayList<>();
    
    // Fields from original ChatRequest if any
    private String userMessage;
    private String sessionId;
    private String userEmail;
    private List<ChatMessage> history;
    
    // Location fields for context-aware recommendations
    private Double latitude;
    private Double longitude;
    private String locationName;
    
    // Nested class for messages
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
        
        public Message() {}
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        @Override
        public String toString() {
            return "Message{" +
                    "role='" + role + '\'' +
                    ", content='" + content + '\'' +
                    '}';
        }
    }
    
    // Getters and Setters
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    @JsonProperty("top_p")
    public Double getTopP() {
        return topP;
    }
    
    @JsonProperty("top_p")
    public void setTopP(Double topP) {
        this.topP = topP;
    }
    
    @JsonProperty("frequency_penalty")
    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }
    
    @JsonProperty("frequency_penalty")
    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }
    
    @JsonProperty("presence_penalty")
    public Double getPresencePenalty() {
        return presencePenalty;
    }
    
    @JsonProperty("presence_penalty")
    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }
    
    @JsonProperty("max_tokens")
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    @JsonProperty("max_tokens")
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Integer getN() {
        return n;
    }
    
    public void setN(Integer n) {
        this.n = n;
    }
    
    public List<String> getStop() {
        return stop;
    }
    
    public void setStop(List<String> stop) {
        this.stop = stop;
    }
    
    public Boolean getStream() {
        return stream;
    }
    
    public void setStream(Boolean stream) {
        this.stream = stream;
    }
    
    public Integer getSeed() {
        return seed;
    }
    
    public void setSeed(Integer seed) {
        this.seed = seed;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    // Getters and setters for original ChatRequest fields
    
    public String getUserMessage() {
        return userMessage;
    }
    
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
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
    
    public List<ChatMessage> getHistory() {
        return history;
    }
    
    public void setHistory(List<ChatMessage> history) {
        this.history = history;
    }
    
    // Getters and setters for location fields
    
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
    
    public String getLocationName() {
        return locationName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    // Helper method to extract user message from messages list
    public String extractUserMessage() {
        if (messages != null && !messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            if ("user".equals(lastMessage.getRole())) {
                return lastMessage.getContent();
            }
        }
        return userMessage;
    }
    
    @Override
    public String toString() {
        return "ChatRequest{" +
                "model='" + model + '\'' +
                ", temperature=" + temperature +
                ", topP=" + topP +
                ", frequencyPenalty=" + frequencyPenalty +
                ", presencePenalty=" + presencePenalty +
                ", maxTokens=" + maxTokens +
                ", n=" + n +
                ", stop=" + stop +
                ", stream=" + stream +
                ", seed=" + seed +
                ", messages=" + messages +
                ", userMessage='" + userMessage + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", locationName='" + locationName + '\'' +
                '}';
    }
}
