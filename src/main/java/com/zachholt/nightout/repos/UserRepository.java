package com.zachholt.nightout.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.zachholt.nightout.models.User;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    
    // Find users within a bounding box of coordinates
    @Query("SELECT u FROM users u WHERE " +
           "u.latitude BETWEEN :minLat AND :maxLat AND " +
           "u.longitude BETWEEN :minLng AND :maxLng")
    List<User> findUsersWithinBounds(
        @Param("minLat") Double minLat, 
        @Param("maxLat") Double maxLat, 
        @Param("minLng") Double minLng, 
        @Param("maxLng") Double maxLng
    );
}
