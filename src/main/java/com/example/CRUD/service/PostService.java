package com.example.CRUD.service;


import com.example.CRUD.dto.PostResponseDto;
import com.example.CRUD.entity.Post;
import com.example.CRUD.entity.User;
import com.example.CRUD.repository.PostRepository;
import com.example.CRUD.repository.UserRepository;
import com.example.CRUD.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;


    private PostResponseDto toDto(Post post){
        PostResponseDto dto = new PostResponseDto();
        dto.setUsername(post.getUser().getUsername());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setMediaUrl(post.getMediaUrl());
        dto.setLikeCount(post.getLikes().size());
        dto.setCommentsCount(post.getComments().size());

        return dto;
    }


    public List<PostResponseDto> findAll() {
        return postRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }


    public PostResponseDto findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return toDto(post);
    }


    public PostResponseDto create(String content, MultipartFile file, String username) {

        User user = userRepository.findByUsername(username);

        Post post = new Post();
        post.setContent(content);
        post.setUser(user);

        if (file != null && !file.isEmpty()) {
            String url = fileUploadService.uploadFile(file);
            post.setMediaUrl(url);
        }

        postRepository.save(post);

        return toDto(post);
    }


    public PostResponseDto update(Long id, String content,
                                  MultipartFile file, String username) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not allowed to update this post");
        }

        if (content != null) {
            post.setContent(content);
        }

        if (file != null && !file.isEmpty()) {
            String url = fileUploadService.uploadFile(file);
            post.setMediaUrl(url);
        }

        postRepository.save(post);

        return toDto(post);
    }


    public PostResponseDto delete(Long id, String username) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not allowed to delete this post");
        }

        postRepository.delete(post);

        return toDto(post);
    }
}