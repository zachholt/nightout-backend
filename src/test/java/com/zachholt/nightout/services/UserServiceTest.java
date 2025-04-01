package com.zachholt.nightout.services;

import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repositories.UserRepository;
import com.zachholt.nightout.services.CoordinateService;
import com.zachholt.nightout.exceptions.ResourceNotFoundException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoordinateService coordinateService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @Mock
    private Environment environment;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Coordinate testCoordinate;
    private final Long userId = 1L;
    private final String userEmail = "test@example.com";
    private final String password = "password123";
    private final String encodedPassword = "encoded_password123";
    private final Double latitude = 40.7128;
    private final Double longitude = -74.0060;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail(userEmail);
        testUser.setPassword(encodedPassword);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setProfileImage("https://example.com/test.jpg");

        testCoordinate = new Coordinate(testUser, latitude, longitude);
        testCoordinate.setId(101L);
        testUser.setCoordinate(testCoordinate);
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(userEmail, result.getEmail());
        assertNull(result.getPassword());
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
        assertNull(result.getPassword());
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
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        User result = userService.registerUser(newUser);

        assertNotNull(result);
        assertEquals(newUser.getEmail(), result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        verify(userRepository).save(any(User.class));
        assertNull(result.getCoordinate());
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
        assertNull(result.getPassword());
    }

    @Test
    void authenticateUser_WithInvalidPassword_ReturnsNull() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        User result = userService.authenticateUser(userEmail, password);

        assertNull(result);
    }

    @Test
    void updateUserLocation_WhenUserExistsAndCoordsProvided_CallsCoordinateServiceUpdate() {
        Double newLatitude = 41.8781;
        Double newLongitude = -87.6298;
        testUser.setCoordinate(null);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        
        Coordinate updatedCoord = new Coordinate(testUser, newLatitude, newLongitude);
        when(coordinateService.updateLocation(userId, newLatitude, newLongitude)).thenReturn(updatedCoord);

        User userAfterUpdate = new User();
        userAfterUpdate.setId(userId);
        userAfterUpdate.setEmail(userEmail);
        userAfterUpdate.setCoordinate(updatedCoord);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userAfterUpdate));

        User result = userService.updateUserLocation(userEmail, newLatitude, newLongitude);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertNotNull(result.getCoordinate(), "Coordinate should not be null after update");
        assertEquals(newLatitude, result.getCoordinate().getLatitude());
        assertEquals(newLongitude, result.getCoordinate().getLongitude());
        verify(userRepository).findByEmail(userEmail);
        verify(coordinateService).updateLocation(userId, newLatitude, newLongitude);
        verify(userRepository).findById(userId);
        verify(coordinateService, never()).clearLocation(anyLong());
    }

    @Test
    void updateUserLocation_WhenUserExistsAndCoordsNull_CallsCoordinateServiceClear() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser)); 
        when(coordinateService.getCurrentLocation(userId)).thenReturn(testCoordinate);
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setCoordinate(null);
            return userToSave;
        });

        User result = userService.updateUserLocation(userEmail, null, null);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertNull(result.getCoordinate(), "Coordinate should be null after checkout");
        
        verify(userRepository).findByEmail(userEmail);
        verify(coordinateService).getCurrentLocation(userId);
        verify(userRepository).save(any(User.class));
        verify(coordinateService, never()).clearLocation(anyLong());
        verify(coordinateService, never()).updateLocation(anyLong(), anyDouble(), anyDouble());
    }

    @Test
    void updateUserLocation_WhenUserDoesNotExist_ThrowsRuntimeException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
             userService.updateUserLocation(userEmail, latitude, longitude);
        });
        
        assertEquals("User not found with email: " + userEmail, exception.getMessage());
        verify(coordinateService, never()).updateLocation(anyLong(), anyDouble(), anyDouble());
        verify(coordinateService, never()).clearLocation(anyLong());
    }

    @Test
    void getUsersByLocation_CallsCoordinateServiceAndMapsResult() {
        Double searchLat = 40.7;
        Double searchLng = -74.0;
        Double radiusMeters = 2000.0;
        Double expectedRadiusKm = 2.0;

        when(coordinateService.getNearbyCoordinates(searchLat, searchLng, expectedRadiusKm))
            .thenReturn(Collections.singletonList(testCoordinate));

        List<User> results = userService.getUsersByLocation(searchLat, searchLng, radiusMeters);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testUser.getId(), results.get(0).getId());
        assertNull(results.get(0).getPassword(), "Password should be null in response");
        verify(coordinateService, times(1)).getNearbyCoordinates(searchLat, searchLng, expectedRadiusKm);
    }

    @Test
    void getUsersAtLocation_CallsGetUsersByLocationWithRadius() {
        Double searchLat = 40.7;
        Double searchLng = -74.0;
        Double radiusMeters = 50.0;
        Double expectedRadiusKm = 0.05;

        when(coordinateService.getNearbyCoordinates(searchLat, searchLng, expectedRadiusKm))
             .thenReturn(Collections.singletonList(testCoordinate));

        List<User> results = userService.getUsersAtLocation(searchLat, searchLng, radiusMeters);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testUser.getId(), results.get(0).getId());
        assertNull(results.get(0).getPassword());
        verify(coordinateService, times(1)).getNearbyCoordinates(searchLat, searchLng, expectedRadiusKm);
    }

    @Test
    void getUsersAtLocation_WithNullRadius_UsesDefaultRadius() {
        Double searchLat = 40.7;
        Double searchLng = -74.0;
        Double defaultRadiusMeters = 100.0;
        Double expectedRadiusKm = 0.1;

        when(coordinateService.getNearbyCoordinates(searchLat, searchLng, expectedRadiusKm))
            .thenReturn(Collections.singletonList(testCoordinate));

        List<User> results = userService.getUsersAtLocation(searchLat, searchLng, null);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testUser.getId(), results.get(0).getId());
        assertNull(results.get(0).getPassword());
        verify(coordinateService, times(1)).getNearbyCoordinates(searchLat, searchLng, expectedRadiusKm);
    }

    @Test
    void deleteUser_WhenUserExists_DeletesUserAndReturnsTrue() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(userRepository).deleteById(userId);

        boolean result = userService.deleteUser(userEmail);

        assertTrue(result);
        verify(userRepository).findByEmail(userEmail);
        verify(userRepository).save(testUser);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WhenUserExistsWithoutCoordinate_DeletesUserAndReturnsTrue() {
        User userWithoutCoord = new User();
        userWithoutCoord.setId(userId);
        userWithoutCoord.setEmail(userEmail);
        userWithoutCoord.setCoordinate(null);
        
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(userWithoutCoord));
        doNothing().when(userRepository).deleteById(userId);

        boolean result = userService.deleteUser(userEmail);

        assertTrue(result);
        verify(userRepository).findByEmail(userEmail);
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ReturnsFalse() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        boolean result = userService.deleteUser(userEmail);

        assertFalse(result);
        verify(userRepository).findByEmail(userEmail);
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).deleteById(anyLong());
    }
} 