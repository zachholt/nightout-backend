package com.zachholt.nightout.services;

import com.zachholt.nightout.models.Coordinate;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repos.CoordinateRepository;
import com.zachholt.nightout.repos.UserRepository;
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