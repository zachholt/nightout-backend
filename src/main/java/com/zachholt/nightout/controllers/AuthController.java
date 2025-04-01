package com.zachholt.nightout.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.models.UserResponse;
import com.zachholt.nightout.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication management API")
public class AuthController {

    @Autowired
    private UserService userService;

    // Helper method to create UserResponse, handling null coordinates
    // Duplicated from UserController, consider moving to a shared utility/mapper class
    private UserResponse createUserResponse(User user) {
        if (user == null) return null; // Handle null user case
        Coordinate coordinate = user.getCoordinate();
        Double lat = (coordinate != null) ? coordinate.getLatitude() : null;
        Double lng = (coordinate != null) ? coordinate.getLongitude() : null;
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getProfileImage(),
            lat, // Use extracted lat
            lng  // Use extracted lng
        );
    }

    @Operation(summary = "Login user", description = "Authenticate user with email and password")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody User loginRequest) {
        User user = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (user != null) {
            // Use helper method
            return ResponseEntity.ok(createUserResponse(user));
        }
        return ResponseEntity.badRequest().body("Invalid credentials");
    }

    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully registered",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid registration data or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User registerRequest) {
        if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Name is required for registration");
        }

        try {
            User user = userService.registerUser(registerRequest);
            // Use helper method
            return ResponseEntity.ok(createUserResponse(user));
        } catch (RuntimeException e) { // Catch potential runtime exceptions like "Email already exists"
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Logout user", description = "End user session")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully logged out"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a real application, you would invalidate the session/token here
        return ResponseEntity.ok().build();
    }
} 