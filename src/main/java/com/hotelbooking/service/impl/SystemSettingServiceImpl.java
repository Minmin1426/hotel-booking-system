package com.hotelbooking.service.impl;

import com.hotelbooking.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    @Value("${room.lock.duration-minutes:10}")
    private int lockDurationMinutes = 10;

    @Override
    public int getLockDurationMinutes() {
        return this.lockDurationMinutes;
    }

    @Override
    public void setLockDurationMinutes(int minutes) {
        if (minutes < 1 || minutes > 1440) {
            throw new IllegalArgumentException("Lock duration must be between 1 and 1440 minutes");
        }
        this.lockDurationMinutes = minutes;
    }
}
