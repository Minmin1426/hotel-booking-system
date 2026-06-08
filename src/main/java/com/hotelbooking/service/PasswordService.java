package com.hotelbooking.service;

import com.hotelbooking.dto.ForgotPasswordRequest;
import com.hotelbooking.dto.ResetPasswordRequest;

public interface PasswordService {
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
