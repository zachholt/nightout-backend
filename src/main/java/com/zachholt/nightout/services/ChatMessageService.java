package com.zachholt.nightout.services;

import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.models.ChatMessageEntity;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repositories.ChatMessageRepository;
import com.zachholt.nightout.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Save a new chat message
     * 
     * @param content The message content
     * @param isUser Whether the message is from a user or the AI
     * @param sessionId The session ID (conversation ID)
     * @param email The user's email (optional, can be null for anonymous chats)
     * @return The saved ChatMessage
     */
    @Transactional
    public ChatMessage saveMessage(String content, boolean isUser, String sessionId, String email) {
        User user = null;
        if (email != null && !email.trim().isEmpty()) {
            Optional<User> userOptional = userRepository.findByEmail(email);
            user = userOptional.orElse(null);
        }
        
        // Create session ID if not provided
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        ChatMessageEntity entity = new ChatMessageEntity(user, sessionId, content, isUser);
        entity = chatMessageRepository.save(entity);
        
        return entity.toDto();
    }
    
    /**
     * Get all messages for a session
     * 
     * @param sessionId The session ID
     * @return List of ChatMessage from the session
     */
    public List<ChatMessage> getMessagesBySessionId(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(ChatMessageEntity::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all sessions for a user
     * 
     * @param email The user's email
     * @return List of session IDs
     */
    public List<String> getUserSessions(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return chatMessageRepository.findDistinctSessionIdsByUserId(user.getId());
        }
        return new ArrayList<>();
    }
    
    /**
     * Get the last message from each session for a user
     * 
     * @param email The user's email
     * @return Map of session ID to last message
     */
    public List<ChatMessage> getLastMessagesFromEachSession(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return chatMessageRepository.findLastMessageFromEachSessionByUserId(user.getId()).stream()
                    .map(ChatMessageEntity::toDto)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
    
    /**
     * Delete a chat session
     * 
     * @param sessionId The session ID to delete
     * @param email The user's email
     * @return true if deleted, false otherwise
     */
    @Transactional
    public boolean deleteSession(String sessionId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<ChatMessageEntity> messages = chatMessageRepository.findByUserAndSessionIdOrderByCreatedAtAsc(user, sessionId);
            if (!messages.isEmpty()) {
                chatMessageRepository.deleteAll(messages);
                return true;
            }
        }
        return false;
    }
} 