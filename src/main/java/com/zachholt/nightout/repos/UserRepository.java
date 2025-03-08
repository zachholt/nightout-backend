package com.zachholt.nightout.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zachholt.nightout.models.User;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    
    // Find users by exact coordinates
    List<User> findByCoordinates(String coordinates);
}
