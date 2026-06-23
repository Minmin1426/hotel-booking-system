package com.hotelbooking.user.mapper;
import com.hotelbooking.user.User;
import com.hotelbooking.user.dto.UserProfileResponse;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getUserId()) // mapping userId to id in response
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .phoneNumber(user.getPhoneNumber())
                .identificationNumber(user.getIdentificationNumber())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
