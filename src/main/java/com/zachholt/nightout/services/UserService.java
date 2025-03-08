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
        
        if (user.getCoordinates() == null || user.getCoordinates().trim().isEmpty()) {
            user.setCoordinates("0,0");
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
    
    public User updateUserCoordinates(String email, String coordinates) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setCoordinates(coordinates);
            user = userRepository.save(user);
            // Don't return the password
            user.setPassword(null);
            return user;
        }
        return null;
    }
    
    // Find users at exact coordinates
    public List<User> getUsersByCoordinates(String coordinates) {
        List<User> users = userRepository.findByCoordinates(coordinates);
        // Don't return passwords
        return users.stream()
            .peek(user -> user.setPassword(null))
            .collect(Collectors.toList());
    }
}
