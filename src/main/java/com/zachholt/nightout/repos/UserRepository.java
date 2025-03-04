package com.zachholt.nightout.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zachholt.nightout.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
