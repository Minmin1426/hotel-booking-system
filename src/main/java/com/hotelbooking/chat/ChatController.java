package com.hotelbooking.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.chat.dto.ChatMessageRequest;
import com.hotelbooking.chat.dto.ChatMessageResponse;
import com.hotelbooking.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final List<ChatMessageResponse> messages = new CopyOnWriteArrayList<>();

    private static final int MAX_MESSAGES = 200;

    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> send(@Valid @RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(System.currentTimeMillis())
                .sender(request.getSender())
                .content(request.getContent())
                .room(request.getRoom())
                .createdAt(LocalDateTime.now())
                .build();
        messages.add(response);
        if (messages.size() > MAX_MESSAGES) {
            messages.subList(0, messages.size() - MAX_MESSAGES).clear();
        }
        log.info("Chat message stored in room {}", request.getRoom());
        return ResponseEntity.ok(ApiResponse.success("Message sent", response));
    }

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> list(
            @RequestParam(defaultValue = "general") String room,
            @RequestParam(required = false) String since) {
        List<ChatMessageResponse> filtered = messages.stream()
                .filter(m -> room.equals(m.getRoom()))
                .filter(m -> since == null || !m.getCreatedAt().isBefore(LocalDateTime.parse(since)))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved", filtered));
    }

    @GetMapping("/messages/stream")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> stream(
            @RequestParam(defaultValue = "general") String room,
            @RequestParam String since) {
        List<ChatMessageResponse> filtered = messages.stream()
                .filter(m -> room.equals(m.getRoom()))
                .filter(m -> m.getCreatedAt().isAfter(LocalDateTime.parse(since))
                        || m.getCreatedAt().isEqual(LocalDateTime.parse(since)))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("New messages", filtered));
    }
}
