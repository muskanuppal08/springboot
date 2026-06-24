package com.example.CRUD.service;


import com.example.CRUD.dto.PostResponseDto;
import com.example.CRUD.entity.Post;
import com.example.CRUD.entity.User;
import com.example.CRUD.entity.Bookmark;
import com.example.CRUD.repository.BookmarkRepository;
import com.example.CRUD.repository.PostRepository;
import com.example.CRUD.repository.UserRepository;
import com.example.CRUD.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;


    private PostResponseDto toDto(Post post){
        PostResponseDto dto = new PostResponseDto();
        dto.setId(post.getId());
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

    @Transactional
    public String toggleBookmark(Long postId, String username) {
        User user = userRepository.findByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndPostId(user.getId(), post.getId());
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return "Bookmark removed";
        } else {
            Bookmark bookmark = new Bookmark();
            bookmark.setUser(user);
            bookmark.setPost(post);
            bookmarkRepository.save(bookmark);
            return "Bookmark added";
        }
    }

    public Page<PostResponseDto> getBookmarkedPosts(String username, int page, int size) {
        User user = userRepository.findByUsername(username);
        Pageable pageable = PageRequest.of(page, size);
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserId(user.getId(), pageable);
        return bookmarks.map(b -> toDto(b.getPost()));
    }
}