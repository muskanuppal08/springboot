package com.example.CRUD.service;


import com.example.CRUD.entity.Follow;
import com.example.CRUD.entity.FollowStatus;
import com.example.CRUD.entity.User;
import com.example.CRUD.repository.FollowRepository;
import com.example.CRUD.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FollowService {


    @Autowired
    FollowRepository followRepository;
    @Autowired
    UserRepository userRepository;

    public List<User> getFollowers(String username)
    {
        User user = userRepository.findByUsername(username);
        List<Follow> followers = followRepository.findByFollowingIdAndFollowStatus(user.getId() , FollowStatus.FOLLOWING) ;

        List<User> users = new ArrayList<>() ;

        for(Follow follower : followers)
        {
            users.add(follower.getFollower());
        }

        return users;

    }

    public List<User> getFollowing(String username)
    {
        User user = userRepository.findByUsername(username);
        List<Follow> followers = followRepository.findByFollowerIdAndFollowStatus(user.getId() , FollowStatus.FOLLOWING) ;

        List<User> users = new ArrayList<>() ;

        for(Follow follower : followers)
        {
            users.add(follower.getFollowing());
        }

        return users;
    }

    public void toggleFollow(long userId , String username){
        User follower = userRepository.findByUsername(username);
        User following = userRepository.findById(userId).get();

        Optional<Follow> isfollow = followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId());
        if(isfollow.isPresent()){
            followRepository.delete(isfollow.get());
            return ;
        }
        Follow follow = new Follow();
        follow.setFollowing(following);
        follow.setFollower(follower);
        // If profile is public (visible = true), directly set status to FOLLOWING.
        // Otherwise set to PENDING.
        if (following.isVisible()) {
            follow.setFollowStatus(FollowStatus.FOLLOWING);
        } else {
            follow.setFollowStatus(FollowStatus.PENDING);
        }
        followRepository.save(follow) ;

    }

    @org.springframework.transaction.annotation.Transactional
    public void acceptFollowRequest(long followerId, String followingUsername) {
        User following = userRepository.findByUsername(followingUsername);
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, following.getId())
                .orElseThrow(() -> new RuntimeException("Follow request not found"));

        follow.setFollowStatus(FollowStatus.FOLLOWING);
        followRepository.save(follow);
    }

    @org.springframework.transaction.annotation.Transactional
    public void rejectFollowRequest(long followerId, String followingUsername) {
        User following = userRepository.findByUsername(followingUsername);
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, following.getId())
                .orElseThrow(() -> new RuntimeException("Follow request not found"));

        followRepository.delete(follow);
    }

    public List<User> getPendingRequests(String username) {
        User user = userRepository.findByUsername(username);
        List<Follow> requests = followRepository.findByFollowingIdAndFollowStatus(user.getId(), FollowStatus.PENDING);

        List<User> followers = new ArrayList<>();
        for (Follow f : requests) {
            followers.add(f.getFollower());
        }
        return followers;
    }
}