package com.example.CRUD.controller;


import com.example.CRUD.dto.PostResponseDto;
import com.example.CRUD.service.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    @GetMapping("")
    Page<PostResponseDto> getFeed(@RequestParam int page, @RequestParam int size , Authentication authentication) {
        String username  = authentication.getName() ;
        return feedService.getFeed(username, page, size);
    }
}