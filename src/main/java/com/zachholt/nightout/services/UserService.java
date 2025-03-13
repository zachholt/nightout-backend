package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repos.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }
        
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default values if not provided
        if (user.getProfileImage() == null || user.getProfileImage().trim().isEmpty()) {
            user.setProfileImage("https://example.com/default-profile.jpg");
        }
        
        // Set default location if not provided
        if (user.getLatitude() == null) {
            user.setLatitude(0.0);
        }
        
        if (user.getLongitude() == null) {
            user.setLongitude(0.0);
        }
        
        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            // Don't return the password
            user.setPassword(null);
            return user;
        }
        return null;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public User getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Don't return the password
            user.setPassword(null);
            return user;
        }
        return null;
    }
    
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            // Don't return the password
            user.setPassword(null);
            return user;
        }
        return null;
    }
    
    // Update to use separate latitude and longitude
    public User updateUserLocation(String email, Double latitude, Double longitude) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setLatitude(latitude);
            user.setLongitude(longitude);
            user = userRepository.save(user);
            // Don't return the password
            user.setPassword(null);
            return user;
        }
        return null;
    }
    
    // Find users near a location within a radius
    public List<User> getUsersByLocation(Double latitude, Double longitude, Double radiusInMeters) {
        // For now, we'll implement a simple version that just returns all users
        // In a real implementation, you would use a spatial query or calculate distance
        List<User> users = userRepository.findAll();
        
        // Filter users by distance (simple Euclidean distance for now)
        // In a real implementation, you would use the Haversine formula for accurate distance
        double radiusInDegrees = radiusInMeters / 111000.0; // Rough conversion from meters to degrees
        
        List<User> nearbyUsers = users.stream()
            .filter(user -> {
                if (user.getLatitude() == null || user.getLongitude() == null) {
                    return false;
                }
                
                double distance = Math.sqrt(
                    Math.pow(user.getLatitude() - latitude, 2) + 
                    Math.pow(user.getLongitude() - longitude, 2)
                );
                
                return distance <= radiusInDegrees;
            })
            .peek(user -> user.setPassword(null))
            .collect(Collectors.toList());
            
        return nearbyUsers;
    }
}
