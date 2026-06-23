package com.example.CRUD.service;


import com.example.CRUD.dto.GlobalSearchResponseDto;
import com.example.CRUD.dto.PostResponseDto;
import com.example.CRUD.dto.UserDto;
import com.example.CRUD.dto.UserResponseDto;
import com.example.CRUD.entity.Post;
import com.example.CRUD.entity.User;
import com.example.CRUD.entity.UserInfo;
import com.example.CRUD.repository.PostRepository;
import com.example.CRUD.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchService {


    @Autowired
    PostRepository postRepository;
    @Autowired
    UserRepository userRepository;


    private UserResponseDto toResponseDto(User user){
        UserResponseDto userDto = new UserResponseDto();
        UserInfo userInfo = user.getUserInfo();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setName(userInfo.getName());

        return userDto;
    }

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


    public Page<UserResponseDto> searchUser(String username , int page , int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findByUsernameContainingIgnoreCase(username , pageable) ;

        return users.map(this::toResponseDto) ;
    }

    // search post

    public Page<PostResponseDto> searchPost(String content , int page , int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByContentContainsIgnoreCase(content, pageable);

        return posts.map(this::toDto);

    }

    public GlobalSearchResponseDto searchGlobalSearch(String content , int page , int size , String type){
        GlobalSearchResponseDto response = new GlobalSearchResponseDto();
        boolean isUserSearch = "user".equalsIgnoreCase(type);
        boolean isPostSearch = "post".equalsIgnoreCase(type);

        if (type == null || type.isBlank()) {
            isUserSearch = true;
            isPostSearch = true;
        }

        if(isUserSearch){
            response.setUsers(searchUser(content,page,size));
        }

        if(isPostSearch){
            response.setPosts(searchPost(content,page,size));
        }

        return response;
    }

}