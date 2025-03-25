package com.zachholt.nightout.models;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserResponseTest {

    @Test
    void whenConstructedWithValidData_thenAllFieldsAreSet() {
        Long id = 1L;
        String name = "Test User";
        String email = "test@example.com";
        LocalDateTime createdAt = LocalDateTime.now();
        String profileImage = "https://example.com/test.jpg";
        Double latitude = 40.7128;
        Double longitude = -74.0060;

        UserResponse response = new UserResponse(
            id, name, email, createdAt, profileImage, latitude, longitude
        );

        assertEquals(id, response.getId());
        assertEquals(name, response.getName());
        assertEquals(email, response.getEmail());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(profileImage, response.getProfileImage());
        assertEquals(latitude, response.getLatitude());
        assertEquals(longitude, response.getLongitude());
    }

    @Test
    void whenConstructedWithNullOptionalFields_thenFieldsAreNull() {
        Long id = 1L;
        String name = "Test User";
        String email = "test@example.com";
        LocalDateTime createdAt = LocalDateTime.now();

        UserResponse response = new UserResponse(
            id, name, email, createdAt, null, null, null
        );

        assertEquals(id, response.getId());
        assertEquals(name, response.getName());
        assertEquals(email, response.getEmail());
        assertEquals(createdAt, response.getCreatedAt());
        assertNull(response.getProfileImage());
        assertNull(response.getLatitude());
        assertNull(response.getLongitude());
    }

    @Test
    void whenConstructedFromUser_thenFieldsMatch() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setProfileImage("https://example.com/test.jpg");
        user.setLatitude(40.7128);
        user.setLongitude(-74.0060);

        UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getProfileImage(),
            user.getLatitude(),
            user.getLongitude()
        );

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
        assertEquals(user.getProfileImage(), response.getProfileImage());
        assertEquals(user.getLatitude(), response.getLatitude());
        assertEquals(user.getLongitude(), response.getLongitude());
    }
} 