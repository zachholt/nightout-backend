package com.zachholt.nightout.repos;

import com.zachholt.nightout.models.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CoordinateRepository extends JpaRepository<Coordinate, Long> {
    Optional<Coordinate> findByUserId(Long userId);
    void deleteByUserId(Long userId);
} 