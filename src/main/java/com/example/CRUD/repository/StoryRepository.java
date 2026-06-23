package com.example.CRUD.repository;

import com.example.CRUD.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM Story s WHERE s.createdAt < :timestamp")
    void deleteByCreatedAtBefore(@Param("timestamp") LocalDateTime timestamp);

    @Query("""
        SELECT s FROM Story s
        WHERE s.createdAt >= :since
        AND (s.user.id = :userId OR s.user.id IN (
            SELECT f.following.id FROM Follow f
            WHERE f.follower.id = :userId
            AND f.followStatus = 'FOLLOWING'
        ))
        ORDER BY s.createdAt ASC
    """)
    List<Story> findActiveStoriesForUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
