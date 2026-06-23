package com.example.CRUD.repository;

import com.example.CRUD.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    public List<Post> findAllByUserId(Long userId);

    @Query("""
        SELECT p FROM Post p
        WHERE 
            p.user.id IN (
                SELECT f.following.id FROM Follow f
                WHERE f.follower.id = :userId
                AND f.followStatus = 'FOLLOWING'
            )
        OR p.user.visible = true
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findFeed(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT p FROM Post p
        WHERE p.user.visible = true
        AND p.user.id <> :userId
        ORDER BY (SIZE(p.likes) + SIZE(p.comments)) DESC, p.createdAt DESC
    """)
    Page<Post> findExploreFeed(@Param("userId") Long userId, Pageable pageable);

    Page<Post> findByContentContainsIgnoreCase(String content , Pageable pageable);


}