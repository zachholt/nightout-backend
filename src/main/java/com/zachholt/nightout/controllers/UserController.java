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
                user.getCoordinates()
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
                user.getCoordinates()
            ));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@RequestParam String email, @RequestParam String coordinates) {
        User user = userService.updateUserCoordinates(email, coordinates);
        if (user != null) {
            return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getProfileImage(),
                user.getCoordinates()
            ));
        }
        return ResponseEntity.badRequest().body("User not found or check-in failed");
    }
    
    @GetMapping("/by-coordinates")
    public ResponseEntity<?> getUsersByCoordinates(@RequestParam String coordinates) {
        List<User> users = userService.getUsersByCoordinates(coordinates);
        List<UserResponse> userResponses = users.stream()
            .map(user -> new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getProfileImage(),
                user.getCoordinates()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }
} 