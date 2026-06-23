package com.hotelbooking.setting;

public interface SystemSettingService {
    int getLockDurationMinutes();
    void setLockDurationMinutes(int minutes);
}
