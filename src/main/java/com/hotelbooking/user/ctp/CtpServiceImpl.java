package com.hotelbooking.user.ctp;

import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.user.ctp.dto.*;
import com.hotelbooking.user.dto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CtpServiceImpl implements CtpService {

    private final UserRepository userRepository;
    private final CtpAuditLogRepository ctpAuditLogRepository;

    @Override
    @Transactional
    public CorporateProfileResponse submitProfile(Long userId, CorporateProfileRequest request) {
        log.info("CTP: Submitting corporate profile for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Only corporate members can submit CTP
        if (!user.isCorporateMember()) {
            throw new BusinessException("Only corporate members can submit a corporate tax profile");
        }

        String previousStatus = user.getCtpStatus();

        // If already verified, modifying resets to PENDING
        if ("VERIFIED".equalsIgnoreCase(user.getCtpStatus())) {
            previousStatus = "VERIFIED"; // record the transition
            user.setCtpStatus("PENDING");
            log.info("CTP: Status reset from VERIFIED to PENDING for userId={}", userId);
        } else if (!"REJECTED".equalsIgnoreCase(user.getCtpStatus())) {
            // PENDING → PENDING is allowed (re-submit)
            user.setCtpStatus("PENDING");
        }

        // Update CTP fields
        user.setCompanyName(request.getCompanyName());
        user.setTaxCode(request.getTaxCode());
        user.setCompanyAddress(request.getCompanyAddress());
        user.setBillingEmail(request.getBillingEmail());

        User saved = userRepository.save(user);

        // Record audit log
        recordAudit(userId, null, previousStatus, "PENDING", null);

        log.info("CTP: Corporate profile submitted successfully for userId={}", userId);
        return toCorporateProfileResponse(saved, "Corporate profile submitted successfully. Status: PENDING");
    }

    @Override
    @Transactional(readOnly = true)
    public CorporateProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.isCorporateMember()) {
            throw new BusinessException("Only corporate members have a corporate tax profile");
        }

        return toCorporateProfileResponse(user, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CtpVerificationSummary> listVerifications(String ctpStatus, Pageable pageable) {
        Page<User> users;
        if (ctpStatus != null && !ctpStatus.isBlank()) {
            users = userRepository.findByAccountTypeAndCtpStatus("CORPORATE_MEMBER", ctpStatus, pageable);
        } else {
            users = userRepository.findByAccountType("CORPORATE_MEMBER", pageable);
        }
        return users.map(this::toCtpVerificationSummary);
    }

    @Override
    @Transactional
    public CorporateProfileResponse approveProfile(Long userId, Long adminId, ApproveCtpRequest request) {
        log.info("CTP: Admin {} approving CTP for userId={}", adminId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.isCorporateMember()) {
            throw new BusinessException("User is not a corporate member");
        }

        if ("VERIFIED".equalsIgnoreCase(user.getCtpStatus())) {
            throw new CtpAlreadyVerifiedException("CTP is already verified");
        }

        String previousStatus = user.getCtpStatus();
        user.setCtpStatus("VERIFIED");
        user.setCtpVerifiedAt(LocalDateTime.now());
        user.setCtpVerifiedBy(adminId);

        User saved = userRepository.save(user);

        // Audit log
        String reason = request != null && request.getNote() != null ? request.getNote() : null;
        recordAudit(userId, adminId, previousStatus, "VERIFIED", reason);

        log.info("CTP: CTP approved for userId={} by adminId={}", userId, adminId);
        return toCorporateProfileResponse(saved, "Corporate tax profile verified and approved");
    }

    @Override
    @Transactional
    public CorporateProfileResponse rejectProfile(Long userId, Long adminId, RejectCtpRequest request) {
        log.info("CTP: Admin {} rejecting CTP for userId={}", adminId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.isCorporateMember()) {
            throw new BusinessException("User is not a corporate member");
        }

        if ("REJECTED".equalsIgnoreCase(user.getCtpStatus())) {
            throw new CtpAlreadyVerifiedException("CTP is already rejected");
        }

        String previousStatus = user.getCtpStatus();
        user.setCtpStatus("REJECTED");
        user.setCtpVerifiedAt(null);
        user.setCtpVerifiedBy(null);

        User saved = userRepository.save(user);

        // Audit log
        recordAudit(userId, adminId, previousStatus, "REJECTED", request.getReason());

        log.info("CTP: CTP rejected for userId={} by adminId={}. Reason: {}", userId, adminId, request.getReason());
        return toCorporateProfileResponse(saved, "Corporate tax profile rejected");
    }

    // --- Private helpers ---

    private void recordAudit(Long userId, Long adminId, String previousStatus, String newStatus, String reason) {
        CtpAuditLog auditLog = CtpAuditLog.builder()
                .userId(userId)
                .adminId(adminId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();
        ctpAuditLogRepository.save(auditLog);
    }

    private CorporateProfileResponse toCorporateProfileResponse(User user, String message) {
        return CorporateProfileResponse.builder()
                .userId(user.getUserId())
                .companyName(user.getCompanyName())
                .taxCode(user.getTaxCode())
                .companyAddress(user.getCompanyAddress())
                .billingEmail(user.getBillingEmail())
                .ctpStatus(user.getCtpStatus())
                .ctpVerifiedAt(user.getCtpVerifiedAt())
                .ctpSubmittedAt(user.getUpdatedAt())
                .message(message)
                .build();
    }

    private CtpVerificationSummary toCtpVerificationSummary(User user) {
        return CtpVerificationSummary.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyName(user.getCompanyName())
                .taxCode(user.getTaxCode())
                .billingEmail(user.getBillingEmail())
                .ctpStatus(user.getCtpStatus())
                .submittedAt(user.getUpdatedAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
