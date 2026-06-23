package com.example.CRUD.repository;

import com.example.CRUD.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :timestamp")
    void deleteByExpiryDateBefore(@Param("timestamp") LocalDateTime timestamp);
}
