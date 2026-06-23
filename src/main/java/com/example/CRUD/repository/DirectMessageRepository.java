package com.example.CRUD.repository;

import com.example.CRUD.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    @Query("""
        SELECT dm FROM DirectMessage dm
        WHERE (dm.sender.id = :u1 AND dm.recipient.id = :u2)
           OR (dm.sender.id = :u2 AND dm.recipient.id = :u1)
        ORDER BY dm.createdAt ASC
    """)
    List<DirectMessage> findChatHistory(@Param("u1") Long user1, @Param("u2") Long user2);
}
