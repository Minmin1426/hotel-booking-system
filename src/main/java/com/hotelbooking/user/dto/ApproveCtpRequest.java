package com.hotelbooking.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveCtpRequest {

    private String note; // Optional admin note on approval
}
