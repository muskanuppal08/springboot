package com.example.CRUD.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDateTime;



@Entity
@Data
public class Token {

    @Id
    private String token;
    LocalDateTime expiryDate;

    @OneToOne(cascade = CascadeType.ALL)
    User user ;
}