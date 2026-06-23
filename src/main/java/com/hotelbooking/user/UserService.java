package com.hotelbooking.user;
import com.hotelbooking.user.dto.UpdateProfileRequest;
import com.hotelbooking.user.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    UserProfileResponse getProfile(Long userId);
}
