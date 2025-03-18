package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.repos.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CoordinateService coordinateService;

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
        return userRepository.findById(id).orElse(null);
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    // Update to use separate latitude and longitude fields
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
        // Convert radius from meters to degrees (approximate)
        double radiusInDegrees = radiusInMeters / 111000.0; // Rough conversion from meters to degrees
        
        // Calculate bounding box
        double minLat = latitude - radiusInDegrees;
        double maxLat = latitude + radiusInDegrees;
        double minLng = longitude - radiusInDegrees;
        double maxLng = longitude + radiusInDegrees;
        
        // Find users within the bounding box
        List<User> usersInBounds = userRepository.findUsersWithinBounds(minLat, maxLat, minLng, maxLng);
        
        // Further filter by exact distance (using Euclidean distance for simplicity)
        // In a real implementation, you would use the Haversine formula for accurate distance
        List<User> nearbyUsers = usersInBounds.stream()
            .filter(user -> {
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

    @Transactional
    public List<Map<String, Object>> getNearbyUsers(Double latitude, Double longitude, Double radius) {
        // Get all coordinates within radius
        List<Coordinate> nearbyCoordinates = coordinateService.getNearbyCoordinates(latitude, longitude, radius);
        
        // Map coordinates to user responses
        return nearbyCoordinates.stream().map(coordinate -> {
            User user = coordinate.getUser();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("profileImage", user.getProfileImage());
            response.put("latitude", coordinate.getLatitude());
            response.put("longitude", coordinate.getLongitude());
            response.put("createdAt", user.getCreatedAt());
            return response;
        }).collect(Collectors.toList());
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
            .map(user -> {
                if (updatedUser.getName() != null) {
                    user.setName(updatedUser.getName());
                }
                if (updatedUser.getEmail() != null) {
                    user.setEmail(updatedUser.getEmail());
                }
                if (updatedUser.getPassword() != null) {
                    user.setPassword(updatedUser.getPassword());
                }
                if (updatedUser.getProfileImage() != null) {
                    user.setProfileImage(updatedUser.getProfileImage());
                }
                return userRepository.save(user);
            })
            .orElse(null);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
