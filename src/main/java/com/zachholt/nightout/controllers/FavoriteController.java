package com.zachholt.nightout.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zachholt.nightout.models.Favorite;
import com.zachholt.nightout.services.FavoriteService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Favorite>> getFavorites(@PathVariable Long userId) {
        try {
            List<Favorite> favorites = favoriteService.getFavorites(userId);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(@Valid @RequestBody Favorite favorite, @RequestParam Long userId) {
        try {
            Favorite savedFavorite = favoriteService.addFavorite(userId, favorite);
            return ResponseEntity.ok(savedFavorite);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/{locationId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long userId, @PathVariable String locationId) {
        try {
            favoriteService.removeFavorite(userId, locationId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/check/{locationId}")
    public ResponseEntity<Boolean> isFavorite(@PathVariable Long userId, @PathVariable String locationId) {
        try {
            boolean isFavorite = favoriteService.isFavorite(userId, locationId);
            return ResponseEntity.ok(isFavorite);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 