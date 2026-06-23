package com.example.CRUD.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "likes" ,
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "post_id"})
        }
)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id ;


    @ManyToOne
    @JoinColumn(name = "user_id")
    User user  ;

    @ManyToOne
    @JoinColumn(name = "post_id")
    Post post ;

    LocalDateTime createdAt ;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}