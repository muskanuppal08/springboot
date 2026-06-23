package com.example.CRUD.service;

import com.example.CRUD.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StoryCleanupTask {

    private final StoryRepository storyRepository;

    // Runs once every hour
    @Scheduled(cron = "0 0 * * * *")
    public void deleteExpiredStories() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        storyRepository.deleteByCreatedAtBefore(cutoff);
    }
}
