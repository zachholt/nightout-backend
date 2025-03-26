package com.zachholt.nightout.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.models.ChatMessageEntity;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repositories.ChatMessageRepository;
import com.zachholt.nightout.repositories.UserRepository;

class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private ChatMessageService chatMessageService;
    
    private User testUser;
    private ChatMessageEntity testMessageEntity;
    private String testSessionId = "test-session-id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setCreatedAt(LocalDateTime.now());
        
        // Set up test message
        testMessageEntity = new ChatMessageEntity(testUser, testSessionId, "Hello", true);
        testMessageEntity.setId(1L);
        testMessageEntity.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void saveMessage_WithValidUser_SavesAndReturnsMessage() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenReturn(testMessageEntity);
        
        // Act
        ChatMessage result = chatMessageService.saveMessage("Hello", true, testSessionId, "test@example.com");
        
        // Assert
        assertNotNull(result);
        assertEquals("Hello", result.getText());
        assertTrue(result.isUser());
        verify(chatMessageRepository, times(1)).save(any(ChatMessageEntity.class));
    }
    
    @Test
    void saveMessage_WithoutUser_StillSavesMessage() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenReturn(testMessageEntity);
        
        // Act
        ChatMessage result = chatMessageService.saveMessage("Hello", true, testSessionId, "nonexistent@example.com");
        
        // Assert
        assertNotNull(result);
        assertEquals("Hello", result.getText());
        verify(chatMessageRepository, times(1)).save(any(ChatMessageEntity.class));
    }
    
    @Test
    void getMessagesBySessionId_ReturnsMessages() {
        // Arrange
        List<ChatMessageEntity> messageEntities = Arrays.asList(testMessageEntity);
        when(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(testSessionId)).thenReturn(messageEntities);
        
        // Act
        List<ChatMessage> result = chatMessageService.getMessagesBySessionId(testSessionId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getText());
    }
    
    @Test
    void getUserSessions_WithValidUser_ReturnsSessions() {
        // Arrange
        List<String> sessionIds = Arrays.asList(testSessionId);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.findDistinctSessionIdsByUserId(testUser.getId())).thenReturn(sessionIds);
        
        // Act
        List<String> result = chatMessageService.getUserSessions("test@example.com");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSessionId, result.get(0));
    }
    
    @Test
    void getUserSessions_WithInvalidUser_ReturnsEmptyList() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Act
        List<String> result = chatMessageService.getUserSessions("nonexistent@example.com");
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void deleteSession_WithValidUserAndSession_DeletesMessages() {
        // Arrange
        List<ChatMessageEntity> messageEntities = Arrays.asList(testMessageEntity);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.findByUserAndSessionIdOrderByCreatedAtAsc(testUser, testSessionId))
            .thenReturn(messageEntities);
        
        // Act
        boolean result = chatMessageService.deleteSession(testSessionId, "test@example.com");
        
        // Assert
        assertTrue(result);
        verify(chatMessageRepository, times(1)).deleteAll(messageEntities);
    }
    
    @Test
    void deleteSession_WithInvalidUserOrSession_ReturnsFalse() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.findByUserAndSessionIdOrderByCreatedAtAsc(testUser, "nonexistent-session"))
            .thenReturn(List.of());
        
        // Act
        boolean result = chatMessageService.deleteSession("nonexistent-session", "test@example.com");
        
        // Assert
        assertFalse(result);
        verify(chatMessageRepository, never()).deleteAll(anyList());
    }
} 