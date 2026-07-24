package com.hotelbooking.chat.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ChatMessageRequest {
    @NotBlank(message = "Sender is required")
    private String sender;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Room is required")
    private String room;
}
