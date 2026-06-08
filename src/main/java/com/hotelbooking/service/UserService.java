package com.hotelbooking.service;

import com.hotelbooking.dto.UpdateProfileRequest;
import com.hotelbooking.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    UserProfileResponse getProfile(Long userId);
}
