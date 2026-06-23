package com.example.CRUD.controller;

import com.example.CRUD.entity.Story;
import com.example.CRUD.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/story")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class StoryController {

    private final StoryService storyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Story> createStory(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String username = authentication.getName();
        Story story = storyService.createStory(username, file);
        return ResponseEntity.ok(story);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Story>> getActiveStories(Authentication authentication) {
        String username = authentication.getName();
        List<Story> stories = storyService.getActiveStories(username);
        return ResponseEntity.ok(stories);
    }
}
