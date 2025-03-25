package com.zachholt.nightout.repositories;

import com.zachholt.nightout.models.User;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Test version of UserRepository that doesn't use PostgreSQL specific spatial functions.
 * This repository is used only in test and local profiles.
 */
@Repository
@Profile({"test", "local"})
public interface TestUserRepository extends JpaRepository<User, Long> {
    /**
     * Find a user by their email address
     * 
     * @param email The email address to search for
     * @return An Optional containing the User if found, or empty if not found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * A simplified version of findByLocationWithinRadius for H2 testing.
     * In a real testing scenario, you would use a database that supports geospatial queries
     * or implement a custom solution for testing this functionality.
     * 
     * This method just returns all users as a workaround for testing.
     */
    @Query("SELECT u FROM com.zachholt.nightout.models.User u")
    List<User> findByLocationWithinRadius(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radius") Double radiusInMeters
    );
    
    /**
     * Alternative implementation for test environments that don't support spatial queries
     * Used in test and local profiles with H2 database
     */
    @Query("SELECT u FROM com.zachholt.nightout.models.User u")
    List<User> findAllUsersForTesting(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radius") Double radiusInMeters
    );
} 