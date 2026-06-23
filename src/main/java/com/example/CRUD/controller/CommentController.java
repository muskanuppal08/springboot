package com.example.CRUD.controller;

import com.example.CRUD.entity.Comment;
import com.example.CRUD.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER' , 'ADMIN')")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> addComment(
            @RequestParam Long postId,
            @RequestBody String content,
            Authentication authentication) {

        String username = authentication.getName();
        Comment comment = commentService.addComment(username, postId, content);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long commentId,
            @RequestBody String content,
            Authentication authentication) {

        String username = authentication.getName();
        Comment updated = commentService.updateComment(commentId, username, content);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String username = authentication.getName();
        commentService.deleteComment(commentId, username);
        return ResponseEntity.ok("Comment deleted successfully");
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(
            @PathVariable Long postId) {

        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @GetMapping("/count/{postId}")
    public ResponseEntity<Long> countComments(
            @PathVariable Long postId) {

        return ResponseEntity.ok(commentService.countComments(postId));
    }
}