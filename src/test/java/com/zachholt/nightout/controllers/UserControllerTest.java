package com.zachholt.nightout.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zachholt.nightout.exceptions.GlobalExceptionHandler;
import com.zachholt.nightout.exceptions.ResourceNotFoundException;
import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.models.UserResponse;
import com.zachholt.nightout.services.CoordinateService;
import com.zachholt.nightout.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;
    private Coordinate testCoordinate;
    private final Long userId = 1L;
    private final String userEmail = "test@example.com";
    private final Double latitude = 40.7128;
    private final Double longitude = -74.0060;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail(userEmail);
        testUser.setPassword("password123");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setProfileImage("https://example.com/test.jpg");

        testCoordinate = new Coordinate(testUser, latitude, longitude);
        testCoordinate.setId(101L);
        testUser.setCoordinate(testCoordinate);
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() throws Exception {
        when(userService.getUserById(userId)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/{id}", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.email", is(userEmail)))
                .andExpect(jsonPath("$.latitude", is(latitude)))
                .andExpect(jsonPath("$.longitude", is(longitude)));
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ReturnsNotFound() throws Exception {
        when(userService.getUserById(userId)).thenReturn(null);

        mockMvc.perform(get("/api/users/{id}", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentUser_WhenUserExists_ReturnsUser() throws Exception {
        when(userService.getUserByEmail(userEmail)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/me")
                .param("email", userEmail)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is(userEmail)))
                .andExpect(jsonPath("$.latitude", is(latitude)))
                .andExpect(jsonPath("$.longitude", is(longitude)));
    }

    @Test
    void getCurrentUser_WhenUserDoesNotExist_ReturnsNotFound() throws Exception {
        when(userService.getUserByEmail(userEmail)).thenReturn(null);

        mockMvc.perform(get("/api/users/me")
                .param("email", userEmail)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkIn_WithValidCoordinates_ReturnsUpdatedUser() throws Exception {
        when(userService.updateUserLocation(userEmail, latitude, longitude)).thenReturn(testUser);

        mockMvc.perform(post("/api/users/checkin")
                .with(csrf())
                .param("email", userEmail)
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude", is(latitude)))
                .andExpect(jsonPath("$.longitude", is(longitude)));

        verify(userService, times(1)).updateUserLocation(userEmail, latitude, longitude);
    }

    @Test
    void checkIn_WithMissingLongitudeParam_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/checkin")
                .with(csrf())
                .param("email", userEmail)
                .param("latitude", String.valueOf(latitude))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Latitude and longitude are required")));

        verify(userService, never()).updateUserLocation(any(), any(), any());
    }

    @Test
    void checkIn_WithCoordinatesInBody_ReturnsUpdatedUser() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("latitude", latitude);
        requestBody.put("longitude", longitude);
        when(userService.updateUserLocation(userEmail, latitude, longitude)).thenReturn(testUser);

        mockMvc.perform(post("/api/users/checkin")
                .with(csrf())
                .param("email", userEmail)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude", is(latitude)))
                .andExpect(jsonPath("$.longitude", is(longitude)));

        verify(userService, times(1)).updateUserLocation(userEmail, latitude, longitude);
    }

    @Test
    void checkIn_WhenUserNotFound_ReturnsNotFound() throws Exception {
        when(userService.updateUserLocation(userEmail, latitude, longitude))
            .thenThrow(new ResourceNotFoundException("User not found with email: " + userEmail));

        mockMvc.perform(post("/api/users/checkin")
                .with(csrf())
                .param("email", userEmail)
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("User not found with email")));

        verify(userService, times(1)).updateUserLocation(userEmail, latitude, longitude);
    }

    @Test
    void checkOut_WhenSuccessful_ReturnsUpdatedUserWithNullCoords() throws Exception {
        testUser.setCoordinate(null);
        when(userService.updateUserLocation(userEmail, null, null)).thenReturn(testUser);

        mockMvc.perform(post("/api/users/checkout")
                .with(csrf())
                .param("email", userEmail)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").doesNotExist())
                .andExpect(jsonPath("$.longitude").doesNotExist());

        verify(userService, times(1)).updateUserLocation(userEmail, null, null);
    }

    @Test
    void checkOut_WhenUserNotFound_ReturnsNotFound() throws Exception {
        when(userService.updateUserLocation(userEmail, null, null))
             .thenThrow(new ResourceNotFoundException("User not found with email: " + userEmail));

        mockMvc.perform(post("/api/users/checkout")
                .with(csrf())
                .param("email", userEmail)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("User not found with email")));

        verify(userService, times(1)).updateUserLocation(userEmail, null, null);
    }

    @Test
    void getUsersNearby_WhenUsersFound_ReturnsUserList() throws Exception {
        Double radiusMeters = 2000.0;
        when(userService.getUsersByLocation(latitude, longitude, radiusMeters))
            .thenReturn(Collections.singletonList(testUser));

        mockMvc.perform(get("/api/users/nearby")
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(userId.intValue())))
                .andExpect(jsonPath("$[0].latitude", is(latitude)))
                .andExpect(jsonPath("$[0].longitude", is(longitude)));

        verify(userService, times(1)).getUsersByLocation(latitude, longitude, radiusMeters);
    }

    @Test
    void getUsersAtLocation_WhenUsersFound_ReturnsUserList() throws Exception {
        Double radiusInMeters = 50.0;
        when(userService.getUsersAtLocation(latitude, longitude, radiusInMeters))
            .thenReturn(Collections.singletonList(testUser));

        mockMvc.perform(get("/api/users/at-location")
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .param("radiusInMeters", String.valueOf(radiusInMeters))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(userId.intValue())))
                .andExpect(jsonPath("$[0].latitude", is(latitude)));

        verify(userService, times(1)).getUsersAtLocation(latitude, longitude, radiusInMeters);
    }

    @Test
    void getUsersAtLocation_WithNullRadius_ReturnsUserListUsingDefault() throws Exception {
        when(userService.getUsersAtLocation(latitude, longitude, null))
            .thenReturn(Collections.singletonList(testUser));

        mockMvc.perform(get("/api/users/at-location")
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(userId.intValue())));

        verify(userService, times(1)).getUsersAtLocation(latitude, longitude, null);
    }

    @Test
    void deleteUser_WhenUserExists_ReturnsSuccess() throws Exception {
        when(userService.deleteUser(userEmail)).thenReturn(true);

        mockMvc.perform(delete("/api/users/delete")
                .with(csrf())
                .param("email", userEmail)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User account deleted successfully")));

        verify(userService, times(1)).deleteUser(userEmail);
    }
    
    @Test
    void deleteUser_WhenUserNotFound_ReturnsNotFound() throws Exception {
        when(userService.deleteUser(userEmail)).thenReturn(false);

        mockMvc.perform(delete("/api/users/delete")
                .with(csrf())
                .param("email", userEmail)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(userEmail);
    }
    
    @Test
    void deleteUser_WithEmptyEmail_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/users/delete")
                .with(csrf())
                .param("email", "")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email is required")));

        verify(userService, never()).deleteUser(any());
    }
    
    @Test
    void deleteUser_WhenServiceThrowsException_ReturnsInternalServerError() throws Exception {
        String errorMessage = "Database error";
        when(userService.deleteUser(userEmail)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(delete("/api/users/delete")
                .with(csrf())
                .param("email", userEmail)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString(errorMessage)));

        verify(userService, times(1)).deleteUser(userEmail);
    }
} 