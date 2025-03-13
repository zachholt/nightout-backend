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
        @RequestParam Double latitude,
        @RequestParam Double longitude) {
        
        User user = userService.updateUserLocation(email, latitude, longitude);
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
        return ResponseEntity.badRequest().body("User not found or check-in failed");
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