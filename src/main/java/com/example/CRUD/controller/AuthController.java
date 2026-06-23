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
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            if (user.getUserInfo().isTwoFactorEnabled()) {
                String tempToken = jwtUtil.generateToken(user.getUsername(), "2FA_PENDING");
                return ResponseEntity.ok("{\"status\": \"2FA_REQUIRED\", \"tempToken\": \"" + tempToken + "\"}");
            }

            String token = jwtUtil.generateToken(
                    request.getUsername(),
                    "USER"
            );

            return ResponseEntity.ok("{\"status\": \"SUCCESS\", \"token\": \"" + token + "\"}");
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

    @PostMapping("/2fa/login-verify")
    public ResponseEntity<?> loginVerify(@RequestBody java.util.Map<String, String> body) {
        String tempToken = body.get("tempToken");
        String code = body.get("code");

        if (tempToken == null || code == null) {
            return ResponseEntity.badRequest().body("Missing tempToken or code");
        }

        if (!jwtUtil.validateToken(tempToken)) {
            return ResponseEntity.status(401).body("Invalid or expired temporary token");
        }

        String username = jwtUtil.extractUsername(tempToken);
        String role = jwtUtil.extractRole(tempToken);

        if (!"2FA_PENDING".equals(role)) {
            return ResponseEntity.badRequest().body("Invalid token role");
        }

        boolean verified = userService.verify2fa(username, code);
        if (verified) {
            String token = jwtUtil.generateToken(username, "USER");
            return ResponseEntity.ok("{\"status\": \"SUCCESS\", \"token\": \"" + token + "\"}");
        }

        return ResponseEntity.status(401).body("Invalid 2FA code");
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setup2fa(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username = principal.getName();
        String secret = userService.generate2faSecret(username);
        String otpauthUri = "otpauth://totp/VibeNet:" + username + "?secret=" + secret + "&issuer=VibeNet";
        return ResponseEntity.ok(java.util.Map.of("secret", secret, "otpauthUri", otpauthUri));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enable2fa(java.security.Principal principal, @RequestBody java.util.Map<String, String> body) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username = principal.getName();
        String code = body.get("code");
        if (code == null) {
            return ResponseEntity.badRequest().body("Missing verification code");
        }
        boolean success = userService.enable2fa(username, code);
        if (success) {
            return ResponseEntity.ok("{\"status\": \"SUCCESS\"}");
        }
        return ResponseEntity.badRequest().body("Invalid 2FA code");
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2fa(java.security.Principal principal, @RequestBody java.util.Map<String, String> body) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username = principal.getName();
        String code = body.get("code");
        if (code == null) {
            return ResponseEntity.badRequest().body("Missing verification code");
        }
        boolean success = userService.disable2fa(username, code);
        if (success) {
            return ResponseEntity.ok("{\"status\": \"SUCCESS\"}");
        }
        return ResponseEntity.badRequest().body("Invalid 2FA code");
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestReset(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        try {
            userService.requestPasswordReset(email);
            return ResponseEntity.ok("{\"message\": \"Password reset link sent successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmReset(@RequestBody java.util.Map<String, String> body) {
        String token = body.get("token");
        String password = body.get("password");
        if (token == null || password == null) {
            return ResponseEntity.badRequest().body("Missing token or password");
        }
        boolean success = userService.confirmPasswordReset(token, password);
        if (success) {
            return ResponseEntity.ok("{\"message\": \"Password reset successfully\"}");
        }
        return ResponseEntity.badRequest().body("Invalid or expired reset token");
    }
}