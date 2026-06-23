package com.example.CRUD.service;

import com.example.CRUD.entity.DirectMessage;
import com.example.CRUD.entity.Streak;
import com.example.CRUD.entity.User;
import com.example.CRUD.repository.DirectMessageRepository;
import com.example.CRUD.repository.StreakRepository;
import com.example.CRUD.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final DirectMessageRepository dmRepository;
    private final StreakRepository streakRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public DirectMessage sendMessage(String senderName, Long recipientId, String content, MultipartFile file, boolean disappearing) {
        User sender = userRepository.findByUsername(senderName);
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        DirectMessage dm = new DirectMessage();
        dm.setSender(sender);
        dm.setRecipient(recipient);
        dm.setContent(content);
        dm.setDisappearing(disappearing);

        if (file != null && !file.isEmpty()) {
            String url = fileUploadService.uploadFile(file);
            dm.setMediaUrl(url);
        }

        // Update streak logic
        updateStreak(sender, recipient);

        return dmRepository.save(dm);
    }

    @Transactional
    public List<DirectMessage> getChatHistory(String username, Long contactId) {
        User user = userRepository.findByUsername(username);
        User contact = userRepository.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        List<DirectMessage> history = dmRepository.findChatHistory(user.getId(), contact.getId());

        // For direct messages returned, if they are disappearing and sent to the current user (recipient),
        // we mark them as read or delete them immediately to guarantee "view-once" behavior.
        for (DirectMessage msg : history) {
            if (msg.isDisappearing() && msg.getRecipient().getId() == user.getId() && msg.getReadAt() == null) {
                msg.setReadAt(LocalDateTime.now());
                dmRepository.save(msg);
            }
        }

        return history;
    }

    public List<Streak> getStreaks(String username) {
        User user = userRepository.findByUsername(username);
        return streakRepository.findAllByUserId(user.getId());
    }

    private void updateStreak(User sender, User recipient) {
        // Find existing streak between the two users
        Optional<Streak> existingStreak = streakRepository.findByUsers(sender.getId(), recipient.getId());
        LocalDateTime now = LocalDateTime.now();

        if (existingStreak.isPresent()) {
            Streak streak = existingStreak.get();
            Duration duration = Duration.between(streak.getLastInteraction(), now);
            long hours = duration.toHours();

            if (hours >= 24 && hours < 48) {
                // Streak maintained and incremented (exactly one interaction per 24 hours)
                streak.setStreakCount(streak.getStreakCount() + 1);
                streak.setLastInteraction(now);
                streakRepository.save(streak);
            } else if (hours >= 48) {
                // Streak died, reset it
                streak.setStreakCount(1);
                streak.setLastInteraction(now);
                streakRepository.save(streak);
            } else {
                // Less than 24 hours, just update last interaction timestamp to prevent expiration
                streak.setLastInteraction(now);
                streakRepository.save(streak);
            }
        } else {
            // Create a brand new streak
            Streak newStreak = new Streak();
            // Arrange users consistently (user1 has lower ID to prevent duplicate permutations in unique key)
            if (sender.getId() < recipient.getId()) {
                newStreak.setUser1(sender);
                newStreak.setUser2(recipient);
            } else {
                newStreak.setUser1(recipient);
                newStreak.setUser2(sender);
            }
            newStreak.setStreakCount(1);
            newStreak.setLastInteraction(now);
            streakRepository.save(newStreak);
        }
    }
}
