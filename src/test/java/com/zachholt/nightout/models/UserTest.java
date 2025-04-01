package com.zachholt.nightout.models;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private Validator validator;
    private User user;
    private Coordinate coordinate;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.now());
        user.setProfileImage("https://example.com/test.jpg");
        
        coordinate = new Coordinate(user, 40.7128, -74.0060);
        user.setCoordinate(coordinate);
    }

    @Test
    void whenAllFieldsAreValid_thenNoValidationViolations() {
        var violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenEmailIsInvalid_thenValidationViolations() {
        user.setEmail("invalid-email");
        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Invalid email address", violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailIsBlank_thenValidationViolations() {
        user.setEmail("");
        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void whenPasswordIsBlank_thenValidationViolations() {
        user.setPassword("");
        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Password is required", violations.iterator().next().getMessage());
    }

    @Test
    void whenCoordinateIsSet_thenGetterReturnsValue() {
        Coordinate newCoord = new Coordinate(user, 1.0, 1.0);
        user.setCoordinate(newCoord);
        assertNotNull(user.getCoordinate());
        assertEquals(1.0, user.getCoordinate().getLatitude());
        assertEquals(1.0, user.getCoordinate().getLongitude());
        assertEquals(user, user.getCoordinate().getUser());
    }

    @Test
    void whenCoordinateIsSetToNull_thenGetterReturnsNull() {
        user.setCoordinate(null);
        assertNull(user.getCoordinate());
    }

    @Test
    void whenSettingProfileImage_thenGetterReturnsValue() {
        String imageUrl = "https://example.com/new-image.jpg";
        user.setProfileImage(imageUrl);
        assertEquals(imageUrl, user.getProfileImage());
    }

    @Test
    void whenProfileImageNotSet_thenReturnsDefaultImage() {
        User newUser = new User();
        assertEquals("https://example.com/default-profile.jpg", newUser.getProfileImage());
    }

    @Test
    void whenCreatedAtSet_thenGetterReturnsValue() {
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        assertEquals(now, user.getCreatedAt());
    }
} 