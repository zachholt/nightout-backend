package com.zachholt.nightout.controllers;

import com.zachholt.nightout.models.User;
import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.UserResponse;
import com.zachholt.nightout.services.UserService;
import com.zachholt.nightout.services.CoordinateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CoordinateService coordinateService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Coordinate coordinate = coordinateService.getCurrentLocation(id);
        return ResponseEntity.ok(new UserResponse(user, coordinate));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<UserResponse>> getNearbyUsers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1.0") Double radius
    ) {
        List<Coordinate> nearbyCoordinates = coordinateService.getNearbyCoordinates(latitude, longitude, radius);
        List<UserResponse> userResponses = nearbyCoordinates.stream()
            .map(coordinate -> new UserResponse(coordinate.getUser(), coordinate))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @PostMapping("/{id}/location")
    public ResponseEntity<UserResponse> updateLocation(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Coordinate coordinate = coordinateService.updateLocation(id, latitude, longitude);
        return ResponseEntity.ok(new UserResponse(user, coordinate));
    }

    @DeleteMapping("/{id}/location")
    public ResponseEntity<UserResponse> clearLocation(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        coordinateService.clearLocation(id);
        return ResponseEntity.ok(new UserResponse(user));
    }
} 