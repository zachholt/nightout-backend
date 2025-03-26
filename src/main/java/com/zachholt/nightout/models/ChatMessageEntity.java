package com.zachholt.nightout.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "chat_messages")
@Schema(description = "Represents a persisted chat message in the database")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Schema(description = "Unique identifier for the message", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Schema(description = "User associated with this message (can be null for system messages)")
    private User user;

    @Column(name = "session_id", nullable = false)
    @Schema(description = "Session identifier to group conversation messages", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "The content of the message", example = "Hi there! I'm your NightOut assistant.")
    private String content;

    @Column(name = "is_user", nullable = false)
    @Schema(description = "Indicates whether the message is from the user (true) or the AI assistant (false)", example = "false")
    private boolean isUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Timestamp when the message was created", example = "2024-03-15T10:30:00")
    private LocalDateTime createdAt;

    // Constructors
    public ChatMessageEntity() {
    }

    public ChatMessageEntity(User user, String sessionId, String content, boolean isUser) {
        this.user = user;
        this.sessionId = sessionId;
        this.content = content;
        this.isUser = isUser;
    }

    // Convert to DTO
    public ChatMessage toDto() {
        ChatMessage dto = new ChatMessage(content, isUser);
        dto.setId(id.toString());
        dto.setTimestamp(createdAt);
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean isUser) {
        this.isUser = isUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 