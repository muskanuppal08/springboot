package com.example.CRUD.controller;

import com.example.CRUD.entity.DirectMessage;
import com.example.CRUD.entity.Streak;
import com.example.CRUD.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ChatController {

    private final ChatService chatService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DirectMessage> sendMessage(
            @RequestParam("recipientId") Long recipientId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "disappearing", defaultValue = "false") boolean disappearing,
            Authentication authentication) {

        String senderName = authentication.getName();
        DirectMessage message = chatService.sendMessage(senderName, recipientId, content, file, disappearing);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{contactId}")
    public ResponseEntity<List<DirectMessage>> getChatHistory(
            @PathVariable Long contactId,
            Authentication authentication) {

        String username = authentication.getName();
        List<DirectMessage> history = chatService.getChatHistory(username, contactId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/streaks")
    public ResponseEntity<List<Streak>> getStreaks(Authentication authentication) {
        String username = authentication.getName();
        List<Streak> streaks = chatService.getStreaks(username);
        return ResponseEntity.ok(streaks);
    }
}
