package com.zachholt.nightout.services;

import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repositories.UserRepository;
import com.zachholt.nightout.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoordinateService coordinateService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private Environment environment;

    public User registerUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default values if not provided
        if (user.getProfileImage() == null || user.getProfileImage().trim().isEmpty()) {
            user.setProfileImage("https://example.com/default-profile.jpg");
        }
        
        // Default location (null coordinate) will be handled separately if needed
        // Removed direct lat/lng setting
        
        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Don't return the password
                user.setPassword(null);
                // Optionally fetch and set coordinate if needed immediately after auth
                // user.setCoordinate(coordinateService.getCurrentLocation(user.getId()));
                return user;
            }
        }
        return null;
    }

    public User saveUser(User user) {
        // Note: Saving user might cascade save coordinate if coordinate is set on user object
        return userRepository.save(user);
    }
    
    public User getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(null); // Don't return the password
            // Optionally fetch and set coordinate if needed
            // user.setCoordinate(coordinateService.getCurrentLocation(user.getId()));
            return user;
        }
        return null;
    }
    
    public User getUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(null); // Don't return the password
            // Optionally fetch and set coordinate if needed
            // user.setCoordinate(coordinateService.getCurrentLocation(user.getId()));
            return user;
        }
        return null;
    }
    
    /**
     * Updates or creates the user's location coordinates.
     * Delegates the core logic to CoordinateService.
     * Returns the updated User object (without password).
     * @throws ResourceNotFoundException if user with the given email is not found.
     */
    @Transactional
    public User updateUserLocation(String email, Double latitude, Double longitude) {
        // Fetch user or throw exception if not found
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        if (latitude != null && longitude != null) {
            // Update or create location via service (runs in its own transaction, but ok)
            coordinateService.updateLocation(user.getId(), latitude, longitude);
            // Re-fetch user to get updated coordinate link
            final Long userId = user.getId(); // Create a final variable for the lambda
            user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        } else {
            // Check-out: Leverage orphanRemoval by setting the coordinate to null on the User side
            // Fetch the current coordinate state first to avoid issues if already null
            Coordinate currentCoordinate = coordinateService.getCurrentLocation(user.getId());
            if (currentCoordinate != null) {
                 user.setCoordinate(null); // This should trigger orphanRemoval on commit
                 user = userRepository.save(user); // Save and refresh user
            }    
            // REMOVED: coordinateService.clearLocation(user.getId());
        }

        // REMOVED: user.setPassword(null); 
        // The User object returned will still have the password hash at this point.
        // Password nullification should happen in the Controller before sending the response.
        return user; 
    }
    
    /**
     * Find users near a given location within a radius.
     * Uses CoordinateService to find nearby coordinates and then maps back to users.
     */
    public List<User> getUsersByLocation(Double latitude, Double longitude, Double radiusInMeters) {
        // Convert radius from meters to kilometers for the service method
        double radiusInKm = radiusInMeters / 1000.0;
        List<Coordinate> nearbyCoordinates = coordinateService.getNearbyCoordinates(latitude, longitude, radiusInKm);

        return nearbyCoordinates.stream()
                .map(Coordinate::getUser) // Get the User from each Coordinate
                .peek(user -> user.setPassword(null)) // Ensure password is null
                .collect(Collectors.toList());
    }
    
    /**
     * Find users *at* a specific location (closer radius).
     * Reuses getUsersByLocation with a default radius if none provided.
     */
    public List<User> getUsersAtLocation(Double latitude, Double longitude, Double radiusInMeters) {
        // Default radius of 100 meters if not specified
        if (radiusInMeters == null) {
            radiusInMeters = 100.0;
        }
        // Delegate to the main getUsersByLocation method
        return getUsersByLocation(latitude, longitude, radiusInMeters);
    }
    
    /**
     * Deletes a user account and all associated data
     * @param email The email of the user to delete
     * @return true if user was deleted, false if user wasn't found
     */
    @Transactional
    public boolean deleteUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // First clear location if exists to handle orphaned coordinates
            if (user.getCoordinate() != null) {
                user.setCoordinate(null);
                userRepository.save(user);
            }
            
            // Then delete the user
            userRepository.deleteById(user.getId());
            return true;
        }
        return false;
    }
}
