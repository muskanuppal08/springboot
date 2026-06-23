package com.example.CRUD.controller;
import com.example.CRUD.entity.User;
import com.example.CRUD.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @GetMapping("/followers")
    public List<User> getFollowers(Authentication authentication) {
        return followService.getFollowers(authentication.getName());
    }

    @GetMapping("/following")
    public List<User> getFollowing(Authentication authentication) {
        return followService.getFollowing(authentication.getName());
    }

    @PostMapping("/{userId}")
    public String toggleFollow(@PathVariable Long userId,
                               Authentication authentication) {

        String currentUsername = authentication.getName();
        followService.toggleFollow(userId, currentUsername);

        return "Follow status updated";
    }
}