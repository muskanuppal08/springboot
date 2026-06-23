package com.example.CRUD.repository;

import com.example.CRUD.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public User findByUsername(String username);


    public Page<User> findByUsernameContainingIgnoreCase(String username , Pageable pageable);
}