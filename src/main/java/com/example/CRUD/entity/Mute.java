package com.example.CRUD.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muter_id", nullable = false)
    private User muter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muted_id", nullable = false)
    private User muted;

    @CurrentTimestamp
    private LocalDateTime createdAt;
}
