package com.example.CRUD.service;

import com.example.CRUD.entity.Story;
import com.example.CRUD.entity.User;
import com.example.CRUD.repository.StoryRepository;
import com.example.CRUD.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;

    public Story createStory(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Media file is required for stories");
        }

        String mediaUrl = fileUploadService.uploadFile(file);
        Story story = new Story();
        story.setUser(user);
        story.setMediaUrl(mediaUrl);

        return storyRepository.save(story);
    }

    public List<Story> getActiveStories(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return storyRepository.findActiveStoriesForUser(user.getId(), since);
    }
}
