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
import java.util.ArrayList;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CoordinateService coordinateService;

    public User registerUser(User user) {
        // Check if user with email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
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
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Don't return the password
            user.setPassword(null);
            return user;
        }
        return null;
    }
    
    public User getUserByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Don't return the password
            user.setPassword(null);
            return user;
        }
        return null;
    }
    
    @Transactional
    public User updateUserLocation(String email, Double latitude, Double longitude) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (latitude != null && longitude != null) {
                coordinateService.updateLocation(user.getId(), latitude, longitude);
            } else {
                coordinateService.clearLocation(user.getId());
            }
            // Don't return the password
            user.setPassword(null);
            return user;
        }
        return null;
    }
    
    public List<Map<String, Object>> getUsersByLocation(Double latitude, Double longitude, Double radiusInMeters) {
        List<Map<String, Object>> userResponses = new ArrayList<>();
        List<Coordinate> nearbyCoordinates = coordinateService.getNearbyCoordinates(latitude, longitude, radiusInMeters);

        for (Coordinate coordinate : nearbyCoordinates) {
            User user = coordinate.getUser();
            // Don't return the password
            user.setPassword(null);

            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("name", user.getName());
            userResponse.put("email", user.getEmail());
            userResponse.put("profileImage", user.getProfileImage());
            userResponse.put("latitude", coordinate.getLatitude());
            userResponse.put("longitude", coordinate.getLongitude());
            userResponse.put("createdAt", user.getCreatedAt());

            userResponses.add(userResponse);
        }

        return userResponses;
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
