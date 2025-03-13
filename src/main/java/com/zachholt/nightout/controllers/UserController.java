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
                    latitude = Double.valueOf(requestBody.get("latitude").toString());
                    System.out.println("Extracted latitude: " + latitude);
                } catch (Exception e) {
                    System.out.println("Error extracting latitude: " + e.getMessage());
                }
            }
            
            if (requestBody.containsKey("longitude")) {
                try {
                    longitude = Double.valueOf(requestBody.get("longitude").toString());
                    System.out.println("Extracted longitude: " + longitude);
                } catch (Exception e) {
                    System.out.println("Error extracting longitude: " + e.getMessage());
                }
            }
        }
        
        // Validate that we have both latitude and longitude
        if (latitude == null || longitude == null) {
            System.out.println("Missing latitude or longitude");
            return ResponseEntity.badRequest().body("Latitude and longitude are required for check-in");
        }
        
        try {
            User user = userService.updateUserLocation(email, latitude, longitude);
            if (user != null) {
                System.out.println("Check-in successful for user: " + user.getName());
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
                return ResponseEntity.badRequest().body("User not found or check-in failed");
            }
        } catch (Exception e) {
            System.out.println("Error during check-in: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error during check-in");
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