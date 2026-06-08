package com.hotelbooking.service.impl;

import com.hotelbooking.dto.UpdateProfileRequest;
import com.hotelbooking.dto.UserProfileResponse;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.mapper.UserMapper;
import com.hotelbooking.model.User;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("UC5: Updating profile for userId={}", userId);

        User user = findUserById(userId);

        validateEmailNotTakenByOther(request.getEmail(), userId);

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIdentificationNumber(request.getIdentificationNumber());

        User saved = userRepository.save(user);
        log.info("UC5: Profile updated successfully for userId={}", userId);
        return userMapper.toProfileResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = findUserById(userId);
        return userMapper.toProfileResponse(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private void validateEmailNotTakenByOther(String email, Long currentUserId) {
        if (userRepository.existsByEmailAndUserIdNot(email, currentUserId)) {
            throw new BusinessException("Email address is already in use by another account");
        }
    }
}
