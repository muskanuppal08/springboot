package com.example.CRUD.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.CRUD.entity.Role;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponseDto {
    private Long id;
    private String name ;
    private String email;
    private String username ;
    private String phone;
    private String profilePic;
    private String bio;
    private String website;
    private String headerPic;
    private boolean verified;
    private boolean twoFactorEnabled;
    private boolean visible;
    private Role role;
}