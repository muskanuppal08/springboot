package com.example.CRUD.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;
    private String content;
    private String mediaUrl ;
    private LocalDateTime createdAt ;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user ;

    @OneToMany(cascade = CascadeType.ALL)
    List<Like> likes  = new ArrayList<>() ;

    @OneToMany(cascade = CascadeType.ALL)
    List<Comment> comments  = new ArrayList<>() ;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }


}