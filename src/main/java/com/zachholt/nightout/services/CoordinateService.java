package com.zachholt.nightout.services;

import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repositories.CoordinateRepository;
import com.zachholt.nightout.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoordinateService {
    @Autowired
    private CoordinateRepository coordinateRepository;

    @Autowired
    private UserRepository userRepository;

    public Coordinate getCurrentLocation(Long userId) {
        return coordinateRepository.findByUserId(userId)
            .orElse(null);
    }

    @Transactional
    public Coordinate updateLocation(Long userId, Double latitude, Double longitude) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete existing coordinate if it exists
        coordinateRepository.deleteByUserId(userId);

        // Create new coordinate
        Coordinate coordinate = new Coordinate();
        coordinate.setUser(user);
        coordinate.setLatitude(latitude);
        coordinate.setLongitude(longitude);

        return coordinateRepository.save(coordinate);
    }

    @Transactional
    public void clearLocation(Long userId) {
        coordinateRepository.deleteByUserId(userId);
    }
} 