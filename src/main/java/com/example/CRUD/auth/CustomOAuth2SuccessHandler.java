package com.example.CRUD.auth;

import com.example.CRUD.entity.Role;
import com.example.CRUD.entity.User;
import com.example.CRUD.entity.UserInfo;
import com.example.CRUD.repository.UserInfoRepository;
import com.example.CRUD.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String username = oauth2User.getAttribute("login"); // github username

        if (username == null) {
            if (email != null) {
                username = email.split("@")[0];
            } else {
                username = "oauth_user_" + UUID.randomUUID().toString().substring(0, 8);
            }
        }
        if (email == null) {
            email = username + "@oauth2.com";
        }
        if (name == null) {
            name = username;
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            // Register new OAuth2 user
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(UUID.randomUUID().toString()); // random dummy password
            user.setVisible(true);

            UserInfo info = new UserInfo();
            info.setName(name);
            info.setRole(Role.ROLE_USER);
            
            // Extract avatar image url
            String avatar = oauth2User.getAttribute("avatar_url"); // github
            if (avatar == null) {
                avatar = oauth2User.getAttribute("picture"); // google
            }
            info.setProfilePic(avatar != null ? avatar : "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80");
            info.setUser(user);
            user.setUserInfo(info);

            userRepository.save(user);
            userInfoRepository.save(info);
        }

        // Generate JWT
        String token = jwtUtil.generateToken(username, "USER");

        // Redirect back to frontend SPA
        String targetUrl = "/index.html?oauth_token=" + token + "&username=" + username;
        response.sendRedirect(targetUrl);
    }
}
