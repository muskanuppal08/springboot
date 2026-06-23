package com.example.CRUD.repository;

import com.example.CRUD.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long> {

    @Query("""
        SELECT s FROM Streak s
        WHERE (s.user1.id = :u1 AND s.user2.id = :u2)
           OR (s.user1.id = :u2 AND s.user2.id = :u1)
    """)
    Optional<Streak> findByUsers(@Param("u1") Long user1, @Param("u2") Long user2);

    @Query("""
        SELECT s FROM Streak s
        WHERE s.user1.id = :userId OR s.user2.id = :userId
    """)
    List<Streak> findAllByUserId(@Param("userId") Long userId);
}
