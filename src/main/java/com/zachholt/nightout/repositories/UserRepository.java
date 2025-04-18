package com.zachholt.nightout.repositories;

import com.zachholt.nightout.models.User;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
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
} 