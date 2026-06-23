package com.example.CRUD.repository;

import com.example.CRUD.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    public List<Comment> findAllByUserId(Long userId);

    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    long countByPostId(Long postId);

    List<Comment> findByPostId(Long postId);
}