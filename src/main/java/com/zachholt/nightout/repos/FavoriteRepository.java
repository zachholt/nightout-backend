package com.zachholt.nightout.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zachholt.nightout.models.Favorite;
import com.zachholt.nightout.models.User;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserAndLocationId(User user, String locationId);
    void deleteByUserAndLocationId(User user, String locationId);
    boolean existsByUserAndLocationId(User user, String locationId);
} 