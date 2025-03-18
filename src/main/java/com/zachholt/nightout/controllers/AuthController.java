package com.zachholt.nightout.controllers;

import com.zachholt.nightout.models.User;
import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.services.UserService;
import com.zachholt.nightout.services.CoordinateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private CoordinateService coordinateService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        if (registeredUser == null) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", registeredUser.getId());
        response.put("name", registeredUser.getName());
        response.put("email", registeredUser.getEmail());
        response.put("profileImage", registeredUser.getProfileImage());
        response.put("latitude", null);
        response.put("longitude", null);
        response.put("createdAt", registeredUser.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        User user = userService.authenticateUser(credentials.get("email"), credentials.get("password"));
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        Coordinate coordinate = coordinateService.getCurrentLocation(user.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("profileImage", user.getProfileImage());
        response.put("latitude", coordinate != null ? coordinate.getLatitude() : null);
        response.put("longitude", coordinate != null ? coordinate.getLongitude() : null);
        response.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a real application, you would invalidate the session/token here
        return ResponseEntity.ok().build();
    }
} 