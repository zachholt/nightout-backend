package com.zachholt.nightout.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zachholt.nightout.models.Favorite;
import com.zachholt.nightout.services.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
@Tag(name = "Favorites", description = "Favorite venues management API")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Operation(summary = "Get user's favorites", description = "Retrieve all favorite venues for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Favorites retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Favorite.class))),
        @ApiResponse(responseCode = "400", description = "Invalid user ID")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<List<Favorite>> getFavorites(
        @Parameter(description = "ID of the user") @PathVariable Long userId) {
        try {
            List<Favorite> favorites = favoriteService.getFavorites(userId);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Add favorite venue", description = "Add a new venue to user's favorites")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Favorite added successfully",
                    content = @Content(schema = @Schema(implementation = Favorite.class))),
        @ApiResponse(responseCode = "400", description = "Invalid favorite data or user ID")
    })
    @PostMapping
    public ResponseEntity<?> addFavorite(
        @Parameter(description = "Favorite venue details") @Valid @RequestBody Favorite favorite,
        @Parameter(description = "ID of the user") @RequestParam Long userId) {
        try {
            Favorite savedFavorite = favoriteService.addFavorite(userId, favorite);
            return ResponseEntity.ok(savedFavorite);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Remove favorite venue", description = "Remove a venue from user's favorites")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Favorite removed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID or location ID")
    })
    @DeleteMapping("/{userId}/{locationId}")
    public ResponseEntity<?> removeFavorite(
        @Parameter(description = "ID of the user") @PathVariable Long userId,
        @Parameter(description = "ID of the location") @PathVariable String locationId) {
        try {
            favoriteService.removeFavorite(userId, locationId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Check if venue is favorite", 
              description = "Check if a venue is in user's favorites")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Check completed successfully",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "400", description = "Invalid user ID or location ID")
    })
    @GetMapping("/{userId}/check/{locationId}")
    public ResponseEntity<Boolean> isFavorite(
        @Parameter(description = "ID of the user") @PathVariable Long userId,
        @Parameter(description = "ID of the location") @PathVariable String locationId) {
        try {
            boolean isFavorite = favoriteService.isFavorite(userId, locationId);
            return ResponseEntity.ok(isFavorite);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 