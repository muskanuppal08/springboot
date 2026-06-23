package com.example.CRUD.controller;


import com.example.CRUD.auth.JwtUtil;
import com.example.CRUD.dto.LoginRequestDto;
import com.example.CRUD.dto.UserDto;
import com.example.CRUD.entity.Role;
import com.example.CRUD.entity.Token;
import com.example.CRUD.entity.User;
import com.example.CRUD.entity.VerficationStatus;
import com.example.CRUD.repository.TokenRepository;
import com.example.CRUD.repository.UserRepository;
import com.example.CRUD.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request) {

        User user  = userRepository.findByUsername(request.getUsername());
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            String token = jwtUtil.generateToken(
                    request.getUsername(),
                    "USER"
            );

            return ResponseEntity.ok(token);
        }

        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto userDto) {
        try{
            userService.createUser(userDto);
            return ResponseEntity.ok("User registered successfully");
        } catch (MessagingException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }


    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {

        boolean verified = userService.verifyToken(token);
        if (verified) {
            return ResponseEntity.ok("User is verified");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification token");
        }

    }
}