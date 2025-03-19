package com.zachholt.nightout.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.zachholt.nightout.models.ChatMessage;
import com.zachholt.nightout.models.User;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByCreatedAtDesc(User user);
    List<ChatMessage> findTop10ByUserOrderByCreatedAtDesc(User user);
} 