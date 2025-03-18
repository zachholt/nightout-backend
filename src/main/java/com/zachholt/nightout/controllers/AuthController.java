package com.zachholt.nightout.controllers;

import com.zachholt.nightout.models.User;
import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.UserResponse;
import com.zachholt.nightout.services.UserService;
import com.zachholt.nightout.services.CoordinateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private CoordinateService coordinateService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        if (registeredUser == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(new UserResponse(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody User loginRequest) {
        User user = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        Coordinate coordinate = coordinateService.getCurrentLocation(user.getId());
        return ResponseEntity.ok(new UserResponse(user, coordinate));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a real application, you would invalidate the session/token here
        return ResponseEntity.ok().build();
    }
} 