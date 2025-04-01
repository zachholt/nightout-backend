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
    void whenConstructedFromUserWithCoordinate_thenFieldsMatch() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setProfileImage("https://example.com/test.jpg");
        
        Coordinate coordinate = new Coordinate(user, 40.7128, -74.0060);
        user.setCoordinate(coordinate);

        Coordinate userCoord = user.getCoordinate();
        Double lat = userCoord != null ? userCoord.getLatitude() : null;
        Double lng = userCoord != null ? userCoord.getLongitude() : null;

        UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getProfileImage(),
            lat,
            lng
        );

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
        assertEquals(user.getProfileImage(), response.getProfileImage());
        assertEquals(coordinate.getLatitude(), response.getLatitude());
        assertEquals(coordinate.getLongitude(), response.getLongitude());
    }
    
    @Test
    void whenConstructedFromUserWithoutCoordinate_thenLatLonAreNull() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now());
        user.setProfileImage("https://example.com/test.jpg");
        user.setCoordinate(null);

        Coordinate userCoord = user.getCoordinate();
        Double lat = userCoord != null ? userCoord.getLatitude() : null;
        Double lng = userCoord != null ? userCoord.getLongitude() : null;

        UserResponse response = new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getProfileImage(),
            lat,
            lng
        );

        assertNull(response.getLatitude());
        assertNull(response.getLongitude());
    }
} 