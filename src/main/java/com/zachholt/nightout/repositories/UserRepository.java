package com.zachholt.nightout.repositories;

import com.zachholt.nightout.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find a user by their email address
     * 
     * @param email The email address to search for
     * @return An Optional containing the User if found, or empty if not found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find users within a specified radius of a location using PostgreSQL's spatial functions
     * Only used in production environments with PostgreSQL
     * 
     * @param latitude The latitude of the center point
     * @param longitude The longitude of the center point
     * @param radiusInMeters The radius in meters from the center point
     * @return A list of Users within the specified radius
     */
    @Query(value = "SELECT * FROM users WHERE " +
           "ST_DWithin(ST_MakePoint(longitude, latitude)::geography, " +
           "ST_MakePoint(:longitude, :latitude)::geography, :radius)", 
           nativeQuery = true)
    List<User> findByLocationWithinRadius(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radius") Double radiusInMeters
    );
    
    /**
     * Alternative implementation for test environments that don't support spatial queries
     * Used in test and local profiles with H2 database
     * 
     * @param latitude The latitude of the center point
     * @param longitude The longitude of the center point
     * @param radiusInMeters The radius in meters from the center point
     * @return A list of all Users as a simplified test implementation
     */
    @Query("SELECT u FROM com.zachholt.nightout.models.User u")
    List<User> findAllUsersForTesting(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radius") Double radiusInMeters
    );
} 