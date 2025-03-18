package com.zachholt.nightout.controllers;

import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.services.CoordinateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coordinates")
public class CoordinateController {
    @Autowired
    private CoordinateService coordinateService;

    @GetMapping("/{userId}")
    public ResponseEntity<Coordinate> getCurrentLocation(@PathVariable Long userId) {
        Coordinate coordinate = coordinateService.getCurrentLocation(userId);
        return coordinate != null
            ? ResponseEntity.ok(coordinate)
            : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Coordinate> updateLocation(
        @RequestParam Long userId,
        @RequestParam Double latitude,
        @RequestParam Double longitude
    ) {
        Coordinate coordinate = coordinateService.updateLocation(userId, latitude, longitude);
        return ResponseEntity.ok(coordinate);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearLocation(@PathVariable Long userId) {
        coordinateService.clearLocation(userId);
        return ResponseEntity.ok().build();
    }
} 