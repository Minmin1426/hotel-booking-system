package com.hotelbooking.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerNoteRequest {
    @NotBlank(message = "Content is required")
    private String content;
    private Boolean isPinned;
}
