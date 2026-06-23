package com.example.CRUD.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
    private String content;
    private String mediaUrl ;
    private LocalDateTime createdAt ;
    private String username ;
    private int likeCount ;
    private int commentsCount ;

}