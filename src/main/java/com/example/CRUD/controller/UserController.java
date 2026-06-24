package com.example.CRUD.controller;


import com.example.CRUD.dto.UserDto;
import com.example.CRUD.dto.UserResponseDto;
import com.example.CRUD.entity.User;
import com.example.CRUD.repository.UserRepository;
import com.example.CRUD.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;

    @GetMapping("")
    public List<UserResponseDto> getAllUsers(){
        return userService.getUsers()   ;
    }

    @GetMapping("/{id}")
    public UserResponseDto getUser(@PathVariable long id){
        return userService.getuser(id) ;
    }

    @PutMapping("")
    public UserResponseDto UpdateUser(@RequestBody UserDto userDto, Authentication authentication){
        return userService.updateUserPartialByUsername(authentication.getName(), userDto) ;
    }

    @DeleteMapping("/{id}")
    public UserResponseDto deleteUser(@PathVariable long id){
        return userService.deleteUser(id) ;
    }


    @GetMapping("/visible")
    public ResponseEntity<String> toggleVisible(Authentication authentication){
        String username = authentication.getName();
        userService.toggleVisible(username);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/block/{userId}")
    public ResponseEntity<String> toggleBlockUser(@PathVariable long userId, Authentication authentication) {
        String result = userService.toggleBlockUser(authentication.getName(), userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/mute/{userId}")
    public ResponseEntity<String> toggleMuteUser(@PathVariable long userId, Authentication authentication) {
        String result = userService.toggleMuteUser(authentication.getName(), userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<UserResponseDto>> getBlockedUsers(Authentication authentication) {
        List<UserResponseDto> blocked = userService.getBlockedUsers(authentication.getName());
        return ResponseEntity.ok(blocked);
    }

    @GetMapping("/muted")
    public ResponseEntity<List<UserResponseDto>> getMutedUsers(Authentication authentication) {
        List<UserResponseDto> muted = userService.getMutedUsers(authentication.getName());
        return ResponseEntity.ok(muted);
    }
}