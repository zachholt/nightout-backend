package com.zachholt.nightout.repositories;

import com.zachholt.nightout.models.ChatMessageEntity;
import com.zachholt.nightout.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    /**
     * Find all messages for a specific session
     * 
     * @param sessionId The session identifier
     * @return List of messages in this session
     */
    List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    /**
     * Find all messages for a specific user
     * 
     * @param user The user
     * @return List of messages from this user
     */
    List<ChatMessageEntity> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find all messages for a specific user in a specific session
     * 
     * @param user The user
     * @param sessionId The session identifier
     * @return List of messages from this user in this session
     */
    List<ChatMessageEntity> findByUserAndSessionIdOrderByCreatedAtAsc(User user, String sessionId);
    
    /**
     * Get all unique session IDs for a user
     * 
     * @param userId The user ID
     * @return List of unique session IDs
     */
    @Query("SELECT DISTINCT c.sessionId FROM ChatMessageEntity c WHERE c.user.id = :userId ORDER BY MAX(c.createdAt) DESC")
    List<String> findDistinctSessionIdsByUserId(@Param("userId") Long userId);
    
    /**
     * Get the last message from each session for a user
     * 
     * @param userId The user ID
     * @return List of the last message from each session
     */
    @Query(value = "SELECT * FROM chat_messages c WHERE c.id IN " +
           "(SELECT MAX(cm.id) FROM chat_messages cm WHERE cm.user_id = :userId GROUP BY cm.session_id) " +
           "ORDER BY c.created_at DESC", nativeQuery = true)
    List<ChatMessageEntity> findLastMessageFromEachSessionByUserId(@Param("userId") Long userId);
} 