package com.zachholt.nightout.repos;

import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByCreatedAtDesc(User user);
} 