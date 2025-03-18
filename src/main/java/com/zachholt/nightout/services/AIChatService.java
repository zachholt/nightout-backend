package com.zachholt.nightout.services;

import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repos.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class AIChatService {
    private final WebClient webClient;
    private final ChatMessageRepository chatMessageRepository;
    private final String aiEndpoint = "https://lisa-rest-2067001295.us-east-1.elb.amazonaws.com/v2/serve/chat/completions";

    @Autowired
    public AIChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.webClient = WebClient.builder()
                .baseUrl(aiEndpoint)
                .build();
    }

    public Flux<String> generateResponse(User user, String userMessage, Double latitude, Double longitude) {
        // Save user message
        ChatMessage userChatMessage = new ChatMessage();
        userChatMessage.setRole("user");
        userChatMessage.setContent(userMessage);
        userChatMessage.setUser(user);
        chatMessageRepository.save(userChatMessage);

        // Get conversation history
        List<ChatMessage> history = chatMessageRepository.findByUserOrderByCreatedAtDesc(user);
        List<Map<String, String>> messages = new ArrayList<>();

        // Add system message with location context
        messages.add(Map.of(
            "role", "system",
            "content", String.format(
                "You are a helpful AI assistant that provides personalized night out recommendations. " +
                "The user is located at latitude: %f, longitude: %f. " +
                "Consider this location context when making suggestions. " +
                "Be friendly and conversational while providing specific, detailed recommendations.",
                latitude, longitude
            )
        ));

        // Add conversation history (limited to last 10 messages)
        for (int i = Math.min(history.size() - 1, 9); i >= 0; i--) {
            ChatMessage msg = history.get(i);
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        // Add current user message
        messages.add(Map.of("role", "user", "content", userMessage));

        // Prepare request body
        Map<String, Object> requestBody = Map.of(
            "model", "mistral-vllm",
            "temperature", 0.7,
            "top_p", 0.01,
            "stream", true,
            "messages", messages
        );

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(chunk -> {
                    // Process and store AI response chunks
                    // Note: In a production environment, you'd want to properly parse the SSE chunks
                    if (chunk.contains("content")) {
                        String content = chunk.split("\"content\":\"")[1].split("\"")[0];
                        return content;
                    }
                    return "";
                })
                .filter(content -> !content.isEmpty());
    }
} 