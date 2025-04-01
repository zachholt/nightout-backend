package com.zachholt.nightout.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zachholt.nightout.exceptions.GlobalExceptionHandler;
import com.zachholt.nightout.models.Favorite;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.services.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

// Imports for MockMvc, matchers, security
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters
@Import(GlobalExceptionHandler.class)   // Import exception handler
public class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FavoriteService favoriteService;

    private User testUser;
    private Favorite testFavorite1;
    private Favorite testFavorite2;
    private final Long userId = 1L;
    private final String locationId1 = "loc123";
    private final String locationId2 = "loc456";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testFavorite1 = new Favorite();
        testFavorite1.setId(101L);
        testFavorite1.setLocationId(locationId1);
        testFavorite1.setLatitude(40.0);
        testFavorite1.setLongitude(-70.0);
        testFavorite1.setUser(testUser);
        testFavorite1.setCreatedAt(LocalDateTime.now());

        testFavorite2 = new Favorite();
        testFavorite2.setId(102L);
        testFavorite2.setLocationId(locationId2);
        testFavorite2.setLatitude(41.0);
        testFavorite2.setLongitude(-71.0);
        testFavorite2.setUser(testUser);
        testFavorite2.setCreatedAt(LocalDateTime.now());
    }

    // --- Tests for GET /api/favorites/{userId} ---
    @Test
    void getFavorites_UserExists_ReturnsFavoritesList() throws Exception {
        when(favoriteService.getFavorites(userId)).thenReturn(Arrays.asList(testFavorite1, testFavorite2));

        mockMvc.perform(get("/api/favorites/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].locationId", is(locationId1)))
                .andExpect(jsonPath("$[1].locationId", is(locationId2)));

        verify(favoriteService, times(1)).getFavorites(userId);
    }

    @Test
    void getFavorites_UserNotFound_ReturnsBadRequest() throws Exception {
        when(favoriteService.getFavorites(userId)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/favorites/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Handled by GlobalExceptionHandler for RuntimeException
                // .andExpect(jsonPath("$.error", containsString("User not found"))); // Can add check for specific message if needed

        verify(favoriteService, times(1)).getFavorites(userId);
    }

    // --- Tests for POST /api/favorites?userId={userId} ---
    @Test
    void addFavorite_ValidData_ReturnsSavedFavorite() throws Exception {
        Favorite newFavoriteInput = new Favorite(); // Input doesn't have user or ID
        newFavoriteInput.setLocationId("newLoc");
        newFavoriteInput.setLatitude(42.0);
        newFavoriteInput.setLongitude(-72.0);
        
        // Service returns the fully formed object
        Favorite savedFavorite = new Favorite();
        savedFavorite.setId(103L);
        savedFavorite.setLocationId("newLoc");
        savedFavorite.setLatitude(42.0);
        savedFavorite.setLongitude(-72.0);
        savedFavorite.setUser(testUser); // User gets set by service
        savedFavorite.setCreatedAt(LocalDateTime.now());

        when(favoriteService.addFavorite(eq(userId), any(Favorite.class))).thenReturn(savedFavorite);

        mockMvc.perform(post("/api/favorites")
                .param("userId", String.valueOf(userId))
                .with(csrf()) // Add CSRF
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newFavoriteInput))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedFavorite.getId().intValue())))
                .andExpect(jsonPath("$.locationId", is("newLoc")));
                // Note: Response might not include the full User object depending on serialization

        verify(favoriteService, times(1)).addFavorite(eq(userId), any(Favorite.class));
    }
    
    @Test
    void addFavorite_AlreadyExists_ReturnsBadRequest() throws Exception {
        Favorite existingFavoriteInput = new Favorite();
        existingFavoriteInput.setLocationId(locationId1);
        
        when(favoriteService.addFavorite(eq(userId), any(Favorite.class)))
            .thenThrow(new RuntimeException("Location already favorited"));

        mockMvc.perform(post("/api/favorites")
                .param("userId", String.valueOf(userId))
                .with(csrf()) // Add CSRF
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingFavoriteInput))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Location already favorited")));

        verify(favoriteService, times(1)).addFavorite(eq(userId), any(Favorite.class));
    }

    // --- Tests for DELETE /api/favorites/{userId}/{locationId} ---
    @Test
    void removeFavorite_Exists_ReturnsOk() throws Exception {
        doNothing().when(favoriteService).removeFavorite(userId, locationId1);

        mockMvc.perform(delete("/api/favorites/{userId}/{locationId}", userId, locationId1)
                .with(csrf())) // Add CSRF
                .andExpect(status().isOk());
                // No body expected for successful delete

        verify(favoriteService, times(1)).removeFavorite(userId, locationId1);
    }

    @Test
    void removeFavorite_UserNotFound_ReturnsBadRequest() throws Exception {
        doThrow(new RuntimeException("User not found")).when(favoriteService).removeFavorite(userId, locationId1);

        mockMvc.perform(delete("/api/favorites/{userId}/{locationId}", userId, locationId1)
                .with(csrf())) // Add CSRF
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("User not found")));

        verify(favoriteService, times(1)).removeFavorite(userId, locationId1);
    }

    // --- Tests for GET /api/favorites/{userId}/check/{locationId} ---
    @Test
    void isFavorite_True_ReturnsTrue() throws Exception {
        when(favoriteService.isFavorite(userId, locationId1)).thenReturn(true);

        mockMvc.perform(get("/api/favorites/{userId}/check/{locationId}", userId, locationId1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(favoriteService, times(1)).isFavorite(userId, locationId1);
    }

    @Test
    void isFavorite_False_ReturnsFalse() throws Exception {
        when(favoriteService.isFavorite(userId, locationId1)).thenReturn(false);

        mockMvc.perform(get("/api/favorites/{userId}/check/{locationId}", userId, locationId1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));

        verify(favoriteService, times(1)).isFavorite(userId, locationId1);
    }

    @Test
    void isFavorite_UserNotFound_ReturnsBadRequest() throws Exception {
        when(favoriteService.isFavorite(userId, locationId1)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/favorites/{userId}/check/{locationId}", userId, locationId1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("User not found")));

        verify(favoriteService, times(1)).isFavorite(userId, locationId1);
    }
} 