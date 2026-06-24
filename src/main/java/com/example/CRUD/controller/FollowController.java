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

    @GetMapping("/requests")
    public List<User> getPendingRequests(Authentication authentication) {
        return followService.getPendingRequests(authentication.getName());
    }

    @PostMapping("/accept/{followerId}")
    public String acceptFollowRequest(@PathVariable Long followerId, Authentication authentication) {
        followService.acceptFollowRequest(followerId, authentication.getName());
        return "Follow request accepted";
    }

    @PostMapping("/reject/{followerId}")
    public String rejectFollowRequest(@PathVariable Long followerId, Authentication authentication) {
        followService.rejectFollowRequest(followerId, authentication.getName());
        return "Follow request rejected";
    }
}