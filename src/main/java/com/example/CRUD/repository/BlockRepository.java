package com.example.CRUD.repository;

import com.example.CRUD.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    List<Block> findByBlockerId(Long blockerId);
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}
