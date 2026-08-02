package com.hotelbooking.auth;
import com.hotelbooking.auth.dto.ForgotPasswordRequest;
import com.hotelbooking.auth.dto.ResetPasswordRequest;

public interface PasswordService {
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
