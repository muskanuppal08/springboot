package com.example.CRUD.controller;

import com.example.CRUD.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/toggle")
    public ResponseEntity<String> toggleLike(
            @RequestParam Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        String response = likeService.toggleLike(username, postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/{postId}")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikeCount(postId));
    }

    @GetMapping("/is-liked")
    public ResponseEntity<Boolean> isLiked(
            @RequestParam Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        boolean liked = likeService.isLiked(username, postId);
        return ResponseEntity.ok(liked);
    }
}