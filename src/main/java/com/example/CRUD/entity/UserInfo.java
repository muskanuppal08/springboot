package com.example.CRUD.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long Id ;

    private String name  ;
    private String profilePic ;
    private String phone ;
    private String location ;
    
    private String bio;
    private String website;
    private String headerPic;
    private boolean verified;
    private String twoFactorSecret;
    private boolean twoFactorEnabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    User user  ;


}