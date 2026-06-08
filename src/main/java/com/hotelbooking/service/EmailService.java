package com.hotelbooking.service;

public interface EmailService {
    void sendResetPasswordEmail(String email, String token);
}
