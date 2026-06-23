package com.hotelbooking.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * UC-31: Request ẩn/hiện review vi phạm.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "HIDE|SHOW", message = "Action must be HIDE or SHOW")
    private String action; // HIDE | SHOW

    private String reason; // bắt buộc khi action=HIDE
}
