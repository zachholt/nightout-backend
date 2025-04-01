package com.zachholt.nightout.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.models.UserResponse;
import com.zachholt.nightout.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.zachholt.nightout.exceptions.ResourceNotFoundException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "Users", description = "User management API")
public class UserController {

    @Autowired
    private UserService userService;

    // Helper method to create UserResponse, handling null coordinates
    private UserResponse createUserResponse(User user) {
        Coordinate coordinate = user.getCoordinate();
        Double lat = (coordinate != null) ? coordinate.getLatitude() : null;
        Double lng = (coordinate != null) ? coordinate.getLongitude() : null;
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getProfileImage(),
            lat,
            lng
        );
    }

    @Operation(summary = "Get user by ID", description = "Retrieve user information by their ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@Parameter(description = "User ID") @PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(createUserResponse(user));
        }
        return ResponseEntity.notFound().build();
    }
    
    @Operation(summary = "Get current user", description = "Retrieve current user information by email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
        @Parameter(description = "User email") @RequestParam String email) {
        User user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(createUserResponse(user));
        }
        return ResponseEntity.notFound().build();
    }
    
    @Operation(summary = "Check in user", description = "Update user's location when checking in at a venue. Provide lat/lng as query params or in request body.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-in successful",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid check-in data (missing coordinates)"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(
        @Parameter(description = "User email") @RequestParam String email,
        @Parameter(description = "Latitude coordinate") @RequestParam(required = false) Double latitude,
        @Parameter(description = "Longitude coordinate") @RequestParam(required = false) Double longitude) {
        System.out.println("Check-in request received for email: " + email);
        System.out.println("Request parameters - latitude: " + latitude + ", longitude: " + longitude);

        boolean isCheckout = latitude == null && longitude == null;

        if (!isCheckout && (latitude == null || longitude == null)) {
            System.out.println("Missing latitude or longitude for check-in");
            return ResponseEntity.badRequest().body("Latitude and longitude are required for check-in");
        }

        User user = userService.updateUserLocation(email, latitude, longitude);

        if (isCheckout) {
            System.out.println("Check-out successful for user: " + user.getName());
        } else {
            System.out.println("Check-in successful for user: " + user.getName());
        }
        return ResponseEntity.ok(createUserResponse(user));
    }
    
    @Operation(summary = "Check out user", description = "Clear user's location when checking out")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-out successful",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(
        @Parameter(description = "User email") @RequestParam String email) {
        System.out.println("Check-out request received for email: " + email);
        
        User user = userService.updateUserLocation(email, null, null);
        System.out.println("Check-out successful for user: " + user.getName());
        return ResponseEntity.ok(createUserResponse(user));
    }
    
    @Operation(summary = "Find users nearby",
              description = "Find users near a specific coordinate point within an optional radius (default: 2km)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid coordinates")
    })
    @GetMapping("/nearby")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getUsersNearby(
        @Parameter(description = "Latitude coordinate") @RequestParam Double latitude,
        @Parameter(description = "Longitude coordinate") @RequestParam Double longitude,
        @Parameter(description = "Search radius in meters (default: 2000)") @RequestParam(required = false) Double radiusInMeters) {
        
        if (radiusInMeters == null) {
            radiusInMeters = 2000.0; 
        }
        
        List<User> users = userService.getUsersByLocation(latitude, longitude, radiusInMeters);
        List<UserResponse> userResponses = users.stream()
            .map(this::createUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }
    
    @Operation(summary = "Find users at a specific location",
              description = "Find users who are checked in very close to a specific location (default radius: 100m)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid coordinates")
    })
    @GetMapping("/at-location")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getUsersAtLocation(
        @Parameter(description = "Latitude coordinate") @RequestParam Double latitude,
        @Parameter(description = "Longitude coordinate") @RequestParam Double longitude,
        @Parameter(description = "Search radius in meters (default: 100)") @RequestParam(required = false) Double radiusInMeters) {
        
        List<User> users = userService.getUsersAtLocation(latitude, longitude, radiusInMeters);
        List<UserResponse> userResponses = users.stream()
            .map(this::createUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }
    
    @Operation(summary = "Delete user account", description = "Permanently delete a user account and all associated data")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User account deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Email parameter is missing"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Error deleting user account")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(
        @Parameter(description = "User email") @RequestParam String email) {
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        try {
            boolean deleted = userService.deleteUser(email);
            if (deleted) {
                return ResponseEntity.ok().body("User account deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting user account: " + e.getMessage());
        }
    }
} 