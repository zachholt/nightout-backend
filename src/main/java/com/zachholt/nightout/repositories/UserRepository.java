package com.zachholt.nightout.repositories;

import com.zachholt.nightout.models.User;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find a user by their email address
     * 
     * @param email The email address to search for
     * @return An Optional containing the User if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users who have coordinates set (non-null latitude and longitude)
     * 
     * @return List of users with coordinates
     */
    @Query("SELECT u FROM com.zachholt.nightout.models.User u WHERE u.latitude IS NOT NULL AND u.longitude IS NOT NULL")
    List<User> findByCoordinates();
    
    /**
     * Find users near specific coordinates within a radius using the Haversine formula
     * 
     * @param latitude Latitude of the location
     * @param longitude Longitude of the location
     * @param radiusInMeters Radius in meters around the location
     * @return List of users within the specified radius
     */
    @Query(value = 
        "SELECT * FROM users u " +
        "WHERE u.latitude IS NOT NULL AND u.longitude IS NOT NULL " +
        "AND (6371000 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * " +
        "cos(radians(u.longitude) - radians(:longitude)) + " +
        "sin(radians(:latitude)) * sin(radians(u.latitude)))) <= :radiusInMeters", 
        nativeQuery = true)
    List<User> findNearLocation(
        @Param("latitude") Double latitude, 
        @Param("longitude") Double longitude, 
        @Param("radiusInMeters") Double radiusInMeters);
} 