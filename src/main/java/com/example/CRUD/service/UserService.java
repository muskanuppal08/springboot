package com.example.CRUD.service;


import com.example.CRUD.dto.UserDto;
import com.example.CRUD.dto.UserResponseDto;
import com.example.CRUD.entity.*;
import com.example.CRUD.auth.TotpUtil;
import com.example.CRUD.repository.TokenRepository;
import com.example.CRUD.repository.UserInfoRepository;
import com.example.CRUD.repository.UserRepository;
import com.example.CRUD.repository.PasswordResetTokenRepository;
import com.example.CRUD.repository.BlockRepository;
import com.example.CRUD.repository.MuteRepository;
import com.example.CRUD.repository.FollowRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private MuteRepository muteRepository;

    @Autowired
    private FollowRepository followRepository;



    private User toEntity(UserDto userDto){
        User user = new User();
        UserInfo userInfo = new UserInfo();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setEmail(userDto.getEmail());

        userInfo.setName(userDto.getName());
        userInfo.setPhone(userDto.getPhone());
        userInfo.setProfilePic(userDto.getProfilePic());
        userInfo.setBio(userDto.getBio());
        userInfo.setWebsite(userDto.getWebsite());
        userInfo.setHeaderPic(userDto.getHeaderPic());
        userInfo.setVerified(userDto.getVerified() != null && userDto.getVerified());
        userInfo.setTwoFactorEnabled(userDto.getTwoFactorEnabled() != null && userDto.getTwoFactorEnabled());
        userInfo.setUser(user);
        user.setUserInfo(userInfo);

        return user;
    }

    private UserDto toDto(User user){
        UserDto userDto = new UserDto();
        UserInfo userInfo = user.getUserInfo();
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setEmail(user.getEmail());
        userDto.setPhone(userInfo.getPhone());
        userDto.setProfilePic(userInfo.getProfilePic());
        userDto.setName(userInfo.getName());
        userDto.setBio(userInfo.getBio());
        userDto.setWebsite(userInfo.getWebsite());
        userDto.setHeaderPic(userInfo.getHeaderPic());
        userDto.setVerified(userInfo.isVerified());
        userDto.setTwoFactorEnabled(userInfo.isTwoFactorEnabled());

        return userDto;

    }

    private UserResponseDto toResponseDto(User user){
        UserResponseDto userDto = new UserResponseDto();
        UserInfo userInfo = user.getUserInfo();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setName(userInfo.getName());
        userDto.setPhone(userInfo.getPhone());
        userDto.setProfilePic(userInfo.getProfilePic());
        userDto.setBio(userInfo.getBio());
        userDto.setWebsite(userInfo.getWebsite());
        userDto.setHeaderPic(userInfo.getHeaderPic());
        userDto.setVerified(userInfo.isVerified());
        userDto.setTwoFactorEnabled(userInfo.isTwoFactorEnabled());
        userDto.setVisible(user.isVisible());
        userDto.setRole(userInfo.getRole());

        return userDto;
    }

    public UserResponseDto createUser(UserDto userDto) throws MessagingException {


        User user   = toEntity(userDto) ;
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVisible(false);

        user.getUserInfo().setRole(Role.ROLE_UNVERIFIED);

        Token token = new Token();
        token.setUser(user);
        UUID uuid = UUID.randomUUID();
        token.setToken(uuid.toString());

        try {
            sendVerificationEmail(user, uuid.toString());
        } catch (Exception e) {
            System.err.println("Email sending failed, but user registration is continuing: " + e.getMessage());
        }

        System.out.println("\n=== REGISTRATION SUCCESS ===");
        System.out.println("User: " + user.getUsername());
        System.out.println("Verification Link: http://localhost:8080/auth/verify?token=" + uuid.toString());
        System.out.println("============================\n");

        userRepository.save(user);
        userInfoRepository.save(user.getUserInfo());
        tokenRepository.save(token);

        return toResponseDto(user);

    }

    public List<UserResponseDto> getUsers() {

        List<User> users  = userRepository.findAll();
        List<UserResponseDto> response = new ArrayList<>();
        for (User user : users) {
            response.add(toResponseDto(user));
        }
        return response;
    }

    public UserResponseDto getuser(long id){
        User  user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return toResponseDto(user);

    }


    public UserResponseDto updateUserPartial(UserDto userDto) {

        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfo userInfo = user.getUserInfo();

        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }

        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        if (userDto.getName() != null) {
            userInfo.setName(userDto.getName());
        }

        if (userDto.getPhone() != null) {
            userInfo.setPhone(userDto.getPhone());
        }

        if (userDto.getProfilePic() != null) {
            userInfo.setProfilePic(userDto.getProfilePic());
        }

        if (userDto.getBio() != null) {
            userInfo.setBio(userDto.getBio());
        }

        if (userDto.getWebsite() != null) {
            userInfo.setWebsite(userDto.getWebsite());
        }

        if (userDto.getHeaderPic() != null) {
            userInfo.setHeaderPic(userDto.getHeaderPic());
        }

        userRepository.save(user);
        userInfoRepository.save(userInfo);

        return toResponseDto(user);
    }

    public UserResponseDto updateUserPartialByUsername(String username, UserDto userDto) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        userDto.setId(user.getId());
        return updateUserPartial(userDto);
    }

    @Transactional
    public boolean verifyToken(String tokenStr) {
        Token token = tokenRepository.findByToken(tokenStr);
        if (token == null) {
            return false;
        }
        User user = token.getUser();
        if (user == null) {
            return false;
        }
        user.getUserInfo().setRole(Role.ROLE_USER);
        user.setVisible(true);
        userRepository.save(user);
        userInfoRepository.save(user.getUserInfo());
        tokenRepository.delete(token);
        return true;
    }



    public UserResponseDto deleteUser(long id) {
        User  user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        return toResponseDto(user);
    }


    public void toggleVisible(String username){
        User user = userRepository.findByUsername(username) ;
        user.setVisible(!user.isVisible());
    }


    public void sendVerificationEmail(User user , String token)
            throws MessagingException {

        String verificationUrl =
                "http://localhost:8080/auth/verify?token=" + token;

        String htmlContent = """
            <html>
                <body>
                    <h2>Email Verification</h2>
                    <p>Please click the button below to verify your account:</p>
                    <a href="%s"
                       style="padding:10px 20px;
                              background-color:#4CAF50;
                              color:white;
                              text-decoration:none;
                              border-radius:5px;">
                       Verify Account
                    </a>
                </body>
            </html>
            """.formatted(verificationUrl);

        String email = user.getEmail() ;

        emailService.sendHtmlMail(email, "Verify Your Account", htmlContent);
    }

    @Transactional
    public void requestPasswordReset(String email) throws MessagingException {
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User with this email not found"));

        // Clear any old reset tokens for this user first
        resetTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId() == user.getId())
                .forEach(resetTokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(15));
        resetTokenRepository.save(resetToken);

        String resetUrl = "http://localhost:8080/index.html?reset-token=" + token;
        String htmlContent = """
            <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>We received a request to reset your password. Click the link below to verify and enter a new password:</p>
                    <a href="%s" style="padding:10px 20px; background-color:#F43F5E; color:white; text-decoration:none; border-radius:5px;">
                       Reset Password
                    </a>
                    <p>This link is valid for 15 minutes.</p>
                </body>
            </html>
            """.formatted(resetUrl);

        emailService.sendHtmlMail(email, "Reset Your Password", htmlContent);
    }

    @Transactional
    public boolean confirmPasswordReset(String tokenStr, String newPassword) {
        PasswordResetToken token = resetTokenRepository.findById(tokenStr).orElse(null);
        if (token == null || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            if (token != null) resetTokenRepository.delete(token);
            return false;
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokenRepository.delete(token);
        return true;
    }

    @Transactional
    public String generate2faSecret(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RuntimeException("User not found");

        String secret = TotpUtil.generateSecret();
        user.getUserInfo().setTwoFactorSecret(secret);
        userRepository.save(user);
        return secret;
    }

    @Transactional
    public boolean enable2fa(String username, String code) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getUserInfo().getTwoFactorSecret() == null) return false;

        boolean verified = TotpUtil.verifyCode(user.getUserInfo().getTwoFactorSecret(), code);
        if (verified) {
            user.getUserInfo().setTwoFactorEnabled(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean disable2fa(String username, String code) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getUserInfo().isTwoFactorEnabled()) return false;

        boolean verified = TotpUtil.verifyCode(user.getUserInfo().getTwoFactorSecret(), code);
        if (verified) {
            user.getUserInfo().setTwoFactorEnabled(false);
            user.getUserInfo().setTwoFactorSecret(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean verify2fa(String username, String code) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getUserInfo().isTwoFactorEnabled()) return false;
        return TotpUtil.verifyCode(user.getUserInfo().getTwoFactorSecret(), code);
    }

    @Transactional
    public String toggleBlockUser(String currentUsername, long targetUserId) {
        User blocker = userRepository.findByUsername(currentUsername);
        User blocked = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (blocker.getId() == blocked.getId()) {
            throw new RuntimeException("Cannot block yourself");
        }

        Optional<Block> existing = blockRepository.findByBlockerIdAndBlockedId(blocker.getId(), blocked.getId());
        if (existing.isPresent()) {
            blockRepository.delete(existing.get());
            return "Unblocked successfully";
        } else {
            // Delete existing follows
            followRepository.findByFollowerIdAndFollowingId(blocker.getId(), blocked.getId())
                    .ifPresent(followRepository::delete);
            followRepository.findByFollowerIdAndFollowingId(blocked.getId(), blocker.getId())
                    .ifPresent(followRepository::delete);

            Block block = new Block();
            block.setBlocker(blocker);
            block.setBlocked(blocked);
            blockRepository.save(block);
            return "Blocked successfully";
        }
    }

    @Transactional
    public String toggleMuteUser(String currentUsername, long targetUserId) {
        User muter = userRepository.findByUsername(currentUsername);
        User muted = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (muter.getId() == muted.getId()) {
            throw new RuntimeException("Cannot mute yourself");
        }

        Optional<Mute> existing = muteRepository.findByMuterIdAndMutedId(muter.getId(), muted.getId());
        if (existing.isPresent()) {
            muteRepository.delete(existing.get());
            return "Unmuted successfully";
        } else {
            Mute mute = new Mute();
            mute.setMuter(muter);
            mute.setMuted(muted);
            muteRepository.save(mute);
            return "Muted successfully";
        }
    }

    public List<UserResponseDto> getBlockedUsers(String username) {
        User user = userRepository.findByUsername(username);
        List<Block> blocks = blockRepository.findByBlockerId(user.getId());
        return blocks.stream()
                .map(b -> toResponseDto(b.getBlocked()))
                .toList();
    }

    public List<UserResponseDto> getMutedUsers(String username) {
        User user = userRepository.findByUsername(username);
        List<Mute> mutes = muteRepository.findByMuterId(user.getId());
        return mutes.stream()
                .map(m -> toResponseDto(m.getMuted()))
                .toList();
    }
}