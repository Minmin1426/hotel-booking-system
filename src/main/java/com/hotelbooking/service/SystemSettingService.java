package com.hotelbooking.service;

public interface SystemSettingService {
    int getLockDurationMinutes();
    void setLockDurationMinutes(int minutes);
}
