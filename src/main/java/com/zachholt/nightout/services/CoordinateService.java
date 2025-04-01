package com.zachholt.nightout.services;

import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repositories.CoordinateRepository;
import com.zachholt.nightout.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;

@Service
public class CoordinateService {
    @Autowired
    private CoordinateRepository coordinateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    public Coordinate getCurrentLocation(Long userId) {
        return coordinateRepository.findByUserId(userId)
            .orElse(null);
    }

    @Transactional
    public Coordinate updateLocation(Long userId, Double latitude, Double longitude) {
        // Find the user first to ensure they exist
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Try to find existing coordinate for this user
        Coordinate coordinate = coordinateRepository.findByUserId(userId)
            .orElse(null); // Find existing or return null

        if (coordinate != null) {
            // Update existing coordinate
            coordinate.setLatitude(latitude);
            coordinate.setLongitude(longitude);
            // Note: createdAt timestamp remains from the initial creation
        } else {
            // Create a new coordinate if one doesn't exist
            coordinate = new Coordinate();
            coordinate.setUser(user);
            coordinate.setLatitude(latitude);
            coordinate.setLongitude(longitude);
            // createdAt is set automatically by the Coordinate constructor/persistence logic
        }

        // Save the updated or new coordinate
        return coordinateRepository.save(coordinate);
    }

    @Transactional
    public void clearLocation(Long userId) {
        coordinateRepository.deleteByUserId(userId);
    }

    @SuppressWarnings("unchecked")
    public List<Coordinate> getNearbyCoordinates(Double latitude, Double longitude, Double radius) {
        // Haversine formula to calculate distance between points
        String sql = """
            SELECT c.* FROM coordinates c
            WHERE (
                6371 * acos(
                    cos(radians(?1)) * cos(radians(c.latitude)) *
                    cos(radians(c.longitude) - radians(?2)) +
                    sin(radians(?1)) * sin(radians(c.latitude))
                )
            ) <= ?3
        """;

        Query query = entityManager.createNativeQuery(sql, Coordinate.class)
            .setParameter(1, latitude)
            .setParameter(2, longitude)
            .setParameter(3, radius);

        return query.getResultList();
    }
} 