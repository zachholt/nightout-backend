package com.zachholt.nightout.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.services.ChatService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long userId) {
        try {
            List<ChatMessage> history = chatService.getChatHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}/recent")
    public ResponseEntity<List<ChatMessage>> getRecentChats(@PathVariable Long userId) {
        try {
            List<ChatMessage> recentChats = chatService.getRecentChats(userId);
            return ResponseEntity.ok(recentChats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/message")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long userId,
            @Valid @RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            Double latitude = (Double) request.get("latitude");
            Double longitude = (Double) request.get("longitude");

            ChatMessage response = chatService.sendMessage(userId, message, latitude, longitude);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 