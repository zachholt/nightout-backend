package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repos.ChatMessageRepository;
import com.zachholt.nightout.repos.UserRepository;
import org.springframework.http.*;
import java.util.*;

@Service
public class ChatService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    private final String AI_API_URL = "https://lisa-rest-2067001295.us-east-1.elb.amazonaws.com/v2/serve/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<ChatMessage> getChatHistory(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return chatMessageRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<ChatMessage> getRecentChats(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return chatMessageRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    public ChatMessage sendMessage(Long userId, String message, Double latitude, Double longitude) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Prepare AI request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> aiRequest = new HashMap<>();
        aiRequest.put("model", "mistral-vllm");
        aiRequest.put("temperature", 0.7);
        aiRequest.put("top_p", 0.01);
        aiRequest.put("stream", true);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // System message to provide context
        messages.add(Map.of(
            "role", "system",
            "content", String.format(
                "You are a helpful AI assistant for suggesting night out activities. " +
                "The user is located at latitude: %f, longitude: %f. " +
                "Consider this location context when making suggestions. " +
                "Be friendly and conversational while providing specific, actionable suggestions.",
                latitude, longitude
            )
        ));
        
        // User message
        messages.add(Map.of("role", "user", "content", message));
        
        aiRequest.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(aiRequest, headers);

        // Call AI API
        ResponseEntity<String> response = restTemplate.exchange(
            AI_API_URL,
            HttpMethod.POST,
            entity,
            String.class
        );

        String aiResponse = response.getBody();

        // Create and save chat message
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(user);
        chatMessage.setMessage(message);
        chatMessage.setAiResponse(aiResponse);
        chatMessage.setLatitude(latitude);
        chatMessage.setLongitude(longitude);

        return chatMessageRepository.save(chatMessage);
    }
} 