package com.zachholt.nightout.controllers;

import com.zachholt.nightout.models.User;
import com.zachholt.nightout.models.UserResponse;
import com.zachholt.nightout.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private final Long userId = 1L;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail(userEmail);
        testUser.setPassword("password123");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setProfileImage("https://example.com/test.jpg");
        testUser.setLatitude(40.7128);
        testUser.setLongitude(-74.0060);
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        when(userService.getUserById(userId)).thenReturn(testUser);

        ResponseEntity<?> response = userController.getUserById(userId);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UserResponse);
        UserResponse userResponse = (UserResponse) response.getBody();
        assertEquals(userId, userResponse.getId());
        assertEquals(testUser.getEmail(), userResponse.getEmail());
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ReturnsNotFound() {
        when(userService.getUserById(userId)).thenReturn(null);

        ResponseEntity<?> response = userController.getUserById(userId);

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void getCurrentUser_WhenUserExists_ReturnsUser() {
        when(userService.getUserByEmail(userEmail)).thenReturn(testUser);

        ResponseEntity<?> response = userController.getCurrentUser(userEmail);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UserResponse);
        UserResponse userResponse = (UserResponse) response.getBody();
        assertEquals(userEmail, userResponse.getEmail());
    }

    @Test
    void getCurrentUser_WhenUserDoesNotExist_ReturnsNotFound() {
        when(userService.getUserByEmail(userEmail)).thenReturn(null);

        ResponseEntity<?> response = userController.getCurrentUser(userEmail);

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void checkIn_WithValidCoordinates_ReturnsUpdatedUser() {
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        when(userService.updateUserLocation(userEmail, latitude, longitude)).thenReturn(testUser);

        ResponseEntity<?> response = userController.checkIn(userEmail, latitude, longitude, null);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UserResponse);
        UserResponse userResponse = (UserResponse) response.getBody();
        assertEquals(latitude, userResponse.getLatitude());
        assertEquals(longitude, userResponse.getLongitude());
    }

    @Test
    void checkIn_WithMissingCoordinates_HandleAsCheckout() {
        // When no coordinates are provided, it's treated as a checkout
        when(userService.updateUserLocation(userEmail, null, null)).thenReturn(testUser);
        
        ResponseEntity<?> response = userController.checkIn(userEmail, null, null, new HashMap<>());

        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void checkIn_WithCoordinatesInBody_ReturnsUpdatedUser() {
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("latitude", latitude);
        requestBody.put("longitude", longitude);

        when(userService.updateUserLocation(userEmail, latitude, longitude)).thenReturn(testUser);

        ResponseEntity<?> response = userController.checkIn(userEmail, null, null, requestBody);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UserResponse);
    }

    @Test
    void checkOut_WhenSuccessful_ReturnsUpdatedUser() {
        when(userService.updateUserLocation(userEmail, null, null)).thenReturn(testUser);

        ResponseEntity<?> response = userController.checkOut(userEmail);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UserResponse);
    }

    @Test
    void checkOut_WhenUserNotFound_ReturnsBadRequest() {
        when(userService.updateUserLocation(userEmail, null, null)).thenReturn(null);

        ResponseEntity<?> response = userController.checkOut(userEmail);

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void getUsersByLocation_WhenUsersFound_ReturnsUserList() {
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        List<User> users = Arrays.asList(testUser);

        when(userService.getUsersByLocation(latitude, longitude, null)).thenReturn(users);

        ResponseEntity<?> response = userController.getUsersByLocation(latitude, longitude);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
        List<?> userResponses = (List<?>) response.getBody();
        assertFalse(userResponses.isEmpty());
        assertTrue(userResponses.get(0) instanceof UserResponse);
    }

    @Test
    void getUsersAtLocation_WhenUsersFound_ReturnsUserList() {
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusInMeters = 100.0;
        List<User> users = Arrays.asList(testUser);

        when(userService.getUsersAtLocation(latitude, longitude, radiusInMeters)).thenReturn(users);

        ResponseEntity<?> response = userController.getUsersAtLocation(latitude, longitude, radiusInMeters);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
        List<?> userResponses = (List<?>) response.getBody();
        assertFalse(userResponses.isEmpty());
        assertTrue(userResponses.get(0) instanceof UserResponse);
    }
    
    @Test
    void getUsersAtLocation_WithNullRadius_ReturnsUserList() {
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        List<User> users = Arrays.asList(testUser);

        when(userService.getUsersAtLocation(latitude, longitude, null)).thenReturn(users);

        ResponseEntity<?> response = userController.getUsersAtLocation(latitude, longitude, null);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
        List<?> userResponses = (List<?>) response.getBody();
        assertFalse(userResponses.isEmpty());
        assertTrue(userResponses.get(0) instanceof UserResponse);
    }
} 