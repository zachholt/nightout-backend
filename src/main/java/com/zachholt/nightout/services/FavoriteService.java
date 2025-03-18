package com.zachholt.nightout.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zachholt.nightout.models.Favorite;
import com.zachholt.nightout.models.User;
import com.zachholt.nightout.repos.FavoriteRepository;
import com.zachholt.nightout.repos.UserRepository;

import java.util.List;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Favorite> getFavorites(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return favoriteRepository.findByUser(user);
    }

    public Favorite addFavorite(Long userId, Favorite favorite) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already favorited
        if (favoriteRepository.existsByUserAndLocationId(user, favorite.getLocationId())) {
            throw new RuntimeException("Location already favorited");
        }

        favorite.setUser(user);
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long userId, String locationId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        favoriteRepository.deleteByUserAndLocationId(user, locationId);
    }

    public boolean isFavorite(Long userId, String locationId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteRepository.existsByUserAndLocationId(user, locationId);
    }
} 