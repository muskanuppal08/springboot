package com.example.CRUD.repository;

import com.example.CRUD.entity.Mute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MuteRepository extends JpaRepository<Mute, Long> {
    Optional<Mute> findByMuterIdAndMutedId(Long muterId, Long mutedId);
    List<Mute> findByMuterId(Long muterId);
    boolean existsByMuterIdAndMutedId(Long muterId, Long mutedId);
}
