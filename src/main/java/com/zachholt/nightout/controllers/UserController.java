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
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CoordinateService coordinateService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Coordinate coordinate = coordinateService.getCurrentLocation(id);
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

    @GetMapping("/nearby")
    public ResponseEntity<List<Map<String, Object>>> getNearbyUsers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1.0") Double radius
    ) {
        List<Map<String, Object>> nearbyUsers = userService.getNearbyUsers(latitude, longitude, radius);
        return ResponseEntity.ok(nearbyUsers);
    }

    @PostMapping("/{id}/location")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Coordinate coordinate = coordinateService.updateLocation(id, latitude, longitude);
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("profileImage", user.getProfileImage());
        response.put("latitude", coordinate.getLatitude());
        response.put("longitude", coordinate.getLongitude());
        response.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/location")
    public ResponseEntity<Void> clearLocation(@PathVariable Long id) {
        coordinateService.clearLocation(id);
        return ResponseEntity.ok().build();
    }
} 