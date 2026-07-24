package com.hotelbooking.chat.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long id;
    private String sender;
    private String content;
    private String room;
    private LocalDateTime createdAt;
}
