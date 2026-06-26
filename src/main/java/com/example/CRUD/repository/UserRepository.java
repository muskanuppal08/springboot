package com.example.CRUD.repository;

import com.example.CRUD.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public User findByUsername(String username);

    public User findByEmail(String email);


    public Page<User> findByUsernameContainingIgnoreCase(String username , Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) " +
           "AND u.id NOT IN (SELECT b.blocked.id FROM Block b WHERE b.blocker.id = :currentUserId) " +
           "AND u.id NOT IN (SELECT b.blocker.id FROM Block b WHERE b.blocked.id = :currentUserId)")
    public Page<User> searchUsersExcludingBlocked(@Param("username") String username, @Param("currentUserId") Long currentUserId, Pageable pageable);
}