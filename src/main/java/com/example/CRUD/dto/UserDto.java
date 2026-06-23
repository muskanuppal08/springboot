package com.example.CRUD.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String email ;
    private String password ;
    private String username ;
    private String name ;
    private String phone ;
    private String profilePic ;
}