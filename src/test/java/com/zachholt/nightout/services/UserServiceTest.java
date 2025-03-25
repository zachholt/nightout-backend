package com.zachholt.nightout.services;

import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @Mock
    private Environment environment;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final Long userId = 1L;
    private final String userEmail = "test@example.com";
    private final String password = "password123";
    private final String encodedPassword = "encoded_password123";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail(userEmail);
        testUser.setPassword(encodedPassword);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setProfileImage("https://example.com/test.jpg");
        testUser.setLatitude(40.7128);
        testUser.setLongitude(-74.0060);
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(userEmail, result.getEmail());
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ReturnsNull() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User result = userService.getUserById(userId);

        assertNull(result);
    }

    @Test
    void getUserByEmail_WhenUserExists_ReturnsUser() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        User result = userService.getUserByEmail(userEmail);

        assertNotNull(result);
        assertEquals(userEmail, result.getEmail());
    }

    @Test
    void getUserByEmail_WhenUserDoesNotExist_ReturnsNull() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        User result = userService.getUserByEmail(userEmail);

        assertNull(result);
    }

    @Test
    void registerUser_WithValidData_ReturnsUser() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        newUser.setPassword(password);

        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.registerUser(newUser);

        assertNotNull(result);
        assertEquals(newUser.getEmail(), result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ThrowsException() {
        User newUser = new User();
        newUser.setEmail(userEmail);
        newUser.setPassword(password);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> userService.registerUser(newUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticateUser_WithValidCredentials_ReturnsUser() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        User result = userService.authenticateUser(userEmail, password);

        assertNotNull(result);
        assertEquals(userEmail, result.getEmail());
    }

    @Test
    void authenticateUser_WithInvalidPassword_ReturnsNull() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        User result = userService.authenticateUser(userEmail, password);

        assertNull(result);
    }

    @Test
    void updateUserLocation_WhenUserExists_ReturnsUpdatedUser() {
        Double newLatitude = 41.8781;
        Double newLongitude = -87.6298;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUserLocation(userEmail, newLatitude, newLongitude);

        assertNotNull(result);
        assertEquals(newLatitude, result.getLatitude());
        assertEquals(newLongitude, result.getLongitude());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserLocation_WhenUserDoesNotExist_ReturnsNull() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        User result = userService.updateUserLocation(userEmail, 0.0, 0.0);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUsersByLocation_ReturnsNearbyUsers() {
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radius = 500.0;
        
        // Mock environment to return test profile
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(userRepository.findAllUsersForTesting(latitude, longitude, radius))
            .thenReturn(Arrays.asList(testUser));

        List<User> results = userService.getUsersByLocation(latitude, longitude, radius);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testUser.getId(), results.get(0).getId());
    }
} 