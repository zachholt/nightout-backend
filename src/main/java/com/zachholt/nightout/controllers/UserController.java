package com.zachholt.nightout.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getProfileImage(),
                user.getLatitude(),
                user.getLongitude()
            ));
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
            return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getProfileImage(),
                user.getLatitude(),
                user.getLongitude()
            ));
        }
        return ResponseEntity.notFound().build();
    }
    
    @Operation(summary = "Check in user", description = "Update user's location when checking in at a venue")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-in successful",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid check-in data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(
        @Parameter(description = "User email") @RequestParam String email,
        @Parameter(description = "Latitude coordinate") @RequestParam(required = false) Double latitude,
        @Parameter(description = "Longitude coordinate") @RequestParam(required = false) Double longitude,
        @Parameter(description = "Additional check-in data") @RequestBody(required = false) Map<String, Object> requestBody) {
        
        System.out.println("Check-in request received for email: " + email);
        System.out.println("Request parameters - latitude: " + latitude + ", longitude: " + longitude);
        System.out.println("Request body: " + requestBody);
        
        // If latitude and longitude are not provided as request parameters, try to get them from the request body
        if ((latitude == null || longitude == null) && requestBody != null) {
            System.out.println("Extracting coordinates from request body");
            
            if (requestBody.containsKey("latitude")) {
                try {
                    // Handle null value in the request body
                    Object latValue = requestBody.get("latitude");
                    if (latValue != null && !latValue.toString().equals("null")) {
                        latitude = Double.valueOf(latValue.toString());
                    } else {
                        latitude = null;
                    }
                    System.out.println("Extracted latitude: " + latitude);
                } catch (Exception e) {
                    System.out.println("Error extracting latitude: " + e.getMessage());
                }
            }
            
            if (requestBody.containsKey("longitude")) {
                try {
                    // Handle null value in the request body
                    Object longValue = requestBody.get("longitude");
                    if (longValue != null && !longValue.toString().equals("null")) {
                        longitude = Double.valueOf(longValue.toString());
                    } else {
                        longitude = null;
                    }
                    System.out.println("Extracted longitude: " + longitude);
                } catch (Exception e) {
                    System.out.println("Error extracting longitude: " + e.getMessage());
                }
            }
        }
        
        // If both latitude and longitude are null, this is a checkout operation
        boolean isCheckout = latitude == null && longitude == null;
        
        // Only validate coordinates for check-in, not for check-out
        if (!isCheckout && (latitude == null || longitude == null)) {
            System.out.println("Missing latitude or longitude for check-in");
            return ResponseEntity.badRequest().body("Latitude and longitude are required for check-in");
        }
        
        try {
            User user = userService.updateUserLocation(email, latitude, longitude);
            if (user != null) {
                if (isCheckout) {
                    System.out.println("Check-out successful for user: " + user.getName());
                } else {
                    System.out.println("Check-in successful for user: " + user.getName());
                }
                return ResponseEntity.ok(new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getCreatedAt(),
                    user.getProfileImage(),
                    user.getLatitude(),
                    user.getLongitude()
                ));
            } else {
                System.out.println("User not found for email: " + email);
                return ResponseEntity.badRequest().body("User not found or operation failed");
            }
        } catch (Exception e) {
            System.out.println("Error during operation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
    
    @Operation(summary = "Check out user", description = "Clear user's location when checking out")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check-out successful",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(
        @Parameter(description = "User email") @RequestParam String email) {
        System.out.println("Check-out request received for email: " + email);
        
        try {
            User user = userService.updateUserLocation(email, null, null);
            if (user != null) {
                System.out.println("Check-out successful for user: " + user.getName());
                return ResponseEntity.ok(new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getCreatedAt(),
                    user.getProfileImage(),
                    user.getLatitude(),
                    user.getLongitude()
                ));
            } else {
                System.out.println("User not found for email: " + email);
                return ResponseEntity.badRequest().body("User not found or check-out failed");
            }
        } catch (Exception e) {
            System.out.println("Error during check-out: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error during check-out");
        }
    }
    
    @Operation(summary = "Find users by location", 
              description = "Find users with location coordinates set")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid coordinates")
    })
    @GetMapping("/by-coordinates")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getUsersByLocation(
        @Parameter(description = "Latitude coordinate") @RequestParam Double latitude,
        @Parameter(description = "Longitude coordinate") @RequestParam Double longitude) {
        
        List<User> users = userService.getUsersByLocation(latitude, longitude, null);
        List<UserResponse> userResponses = users.stream()
            .map(user -> new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getProfileImage(),
                user.getLatitude(),
                user.getLongitude()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }
    
    @Operation(summary = "Find users at a specific location", 
              description = "Find users who are checked in at a specific location")
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
            .map(user -> new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getProfileImage(),
                user.getLatitude(),
                user.getLongitude()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }
} 