package com.zachholt.nightout.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a single message in a chat conversation")
public class ChatMessage {
    @Schema(description = "Unique identifier for the message", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "The content of the message", example = "Hi there! I'm your NightOut assistant.")
    private String text;
    
    @Schema(description = "Indicates whether the message is from the user (true) or the AI assistant (false)", example = "false")
    private boolean isUser;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the message was created", example = "2023-05-15T12:00:00")
    private LocalDateTime timestamp;

    public ChatMessage() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String text, boolean isUser) {
        this();
        this.text = text;
        this.isUser = isUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean isUser) {
        this.isUser = isUser;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
} 