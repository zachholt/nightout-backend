package com.zachholt.nightout.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.models.UserResponse;
import com.zachholt.nightout.services.UserService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
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
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestParam String email) {
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
    
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(
        @RequestParam String email, 
        @RequestParam(required = false) Double latitude,
        @RequestParam(required = false) Double longitude,
        @RequestBody(required = false) Map<String, Object> requestBody) {
        
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
    
    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@RequestParam String email) {
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
    
    @GetMapping("/by-coordinates")
    public ResponseEntity<?> getUsersByLocation(
        @RequestParam Double latitude,
        @RequestParam Double longitude,
        @RequestParam(required = false, defaultValue = "500") Double radiusInMeters) {
        
        List<User> users = userService.getUsersByLocation(latitude, longitude, radiusInMeters);
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