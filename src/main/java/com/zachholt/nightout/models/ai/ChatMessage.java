package com.zachholt.nightout.models.ai;

import java.util.List;

public class ChatMessage {
    private String role;
    private List<MessageContent> content;

    public ChatMessage() {
    }

    public ChatMessage(String role, List<MessageContent> content) {
        this.role = role;
        this.content = content;
    }

    // Simple constructor for text-only messages
    public ChatMessage(String role, String text) {
        this.role = role;
        this.content = List.of(new MessageContent("text", text));
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<MessageContent> getContent() {
        return content;
    }

    public void setContent(List<MessageContent> content) {
        this.content = content;
    }

    public static class MessageContent {
        private String type;
        private String text;

        public MessageContent() {
        }

        public MessageContent(String type, String text) {
            this.type = type;
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
} 