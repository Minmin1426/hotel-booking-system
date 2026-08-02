package com.hotelbooking.user.ctp;

import com.hotelbooking.user.dto.*;
import com.hotelbooking.user.ctp.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CtpService {

    /**
     * Submit or update a Corporate Tax Profile for the given user.
     * Status transitions to PENDING. If previously VERIFIED, resets to PENDING.
     */
    CorporateProfileResponse submitProfile(Long userId, CorporateProfileRequest request);

    /**
     * Get the corporate profile for the given user.
     */
    CorporateProfileResponse getProfile(Long userId);

    /**
     * Admin: list all CTP verification requests filtered by status.
     */
    Page<CtpVerificationSummary> listVerifications(String ctpStatus, Pageable pageable);

    /**
     * Admin: approve a CTP — set status to VERIFIED, record verified_at and verified_by.
     */
    CorporateProfileResponse approveProfile(Long userId, Long adminId, ApproveCtpRequest request);

    /**
     * Admin: reject a CTP — set status to REJECTED, store reason.
     */
    CorporateProfileResponse rejectProfile(Long userId, Long adminId, RejectCtpRequest request);
}
