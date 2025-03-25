package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

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
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Don't return the password
                user.setPassword(null);
                return user;
            }
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
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Don't return the password
            user.setPassword(null);
            return user;
        }
        return null;
    }
    
    // Update to use separate latitude and longitude fields
    public User updateUserLocation(String email, Double latitude, Double longitude) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
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
        // Get all users with coordinates set using the new repository method
        return userRepository.findByCoordinates().stream()
            .peek(user -> user.setPassword(null))  // Don't return passwords
            .collect(Collectors.toList());
    }
}
