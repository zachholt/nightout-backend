package com.zachholt.nightout.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zachholt.nightout.models.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
