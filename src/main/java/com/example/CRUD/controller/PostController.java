package com.example.CRUD.controller;


import com.example.CRUD.auth.JwtUtil;
import com.example.CRUD.dto.PostResponseDto;
import com.example.CRUD.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER' , 'ADMIN')")
    public ResponseEntity<PostResponseDto> create(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication) {



        String username = authentication.getName();
        PostResponseDto response = postService.create(content, file, username);

        return ResponseEntity.ok(response);
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER' ,'ADMIN')")
    public ResponseEntity<PostResponseDto> update(
            @PathVariable Long id,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication ){


        String username = authentication.getName();

        PostResponseDto response = postService.update(id, content, file, username);

        return ResponseEntity.ok(response);
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN' , 'UNVERIFIED')")
    public ResponseEntity<List<PostResponseDto>> findAll() {
        return ResponseEntity.ok(postService.findAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.findById(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<PostResponseDto> delete(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();

        return ResponseEntity.ok(postService.delete(id, username));
    }
}