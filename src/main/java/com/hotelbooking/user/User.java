package com.hotelbooking.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role; // "CUSTOMER" | "ADMIN" | "DIRECTOR" | "GUEST"

    @Column(nullable = false)
    private String status; // "ACTIVE" | "LOCKED" | "INACTIVE"

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_logout_at")
    private LocalDateTime lastLogoutAt;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "identification_number")
    private String identificationNumber;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- 007-customer-portal-profile: Account type ---
    @Column(name = "account_type")
    @Builder.Default
    private String accountType = "CUSTOMER"; // "CUSTOMER" | "CORPORATE_MEMBER"

    @Column(name = "google_subject_id", unique = true)
    private String googleSubjectId;

    // Password reset OTP
    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    // 011-loyalty-membership-tiers: Loyalty tier fields
    @Column(name = "current_tier")
    @Builder.Default
    private String currentTier = "BRONZE"; // BRONZE, SILVER, GOLD, PLATINUM, *_BUSINESS variants

    @Column(name = "tier_evaluated_at")
    private LocalDateTime tierEvaluatedAt;

    // --- 015-admin-customer-management: VIP fields ---
    @Column(name = "is_vip")
    @Builder.Default
    private Boolean isVip = false;



    @Column(name = "vip_marked_at")
    private LocalDateTime vipMarkedAt;

    @Column(name = "vip_marked_by")
    private Long vipMarkedBy;

    // --- Helper methods ---
    public boolean isCorporateMember() {
        return "CORPORATE_MEMBER".equalsIgnoreCase(accountType);
    }

    public boolean isBronzeTier() {
        return "BRONZE".equalsIgnoreCase(currentTier) || "BRONZE_BUSINESS".equalsIgnoreCase(currentTier);
    }

    public boolean isPlatinumTier() {
        return "PLATINUM".equalsIgnoreCase(currentTier) || "PLATINUM_BUSINESS".equalsIgnoreCase(currentTier);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"LOCKED".equalsIgnoreCase(this.status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    // Explicit getters and setters for cross-package compilation safety
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getIdentificationNumber() { return identificationNumber; }
    public void setIdentificationNumber(String identificationNumber) { this.identificationNumber = identificationNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getCurrentTier() { return currentTier; }
    public void setCurrentTier(String currentTier) { this.currentTier = currentTier; }

    public Boolean getIsVip() { return isVip; }
    public void setIsVip(Boolean isVip) { this.isVip = isVip; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts != null ? failedLoginAttempts : 0; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLastLogoutAt() { return lastLogoutAt; }
    public void setLastLogoutAt(LocalDateTime lastLogoutAt) { this.lastLogoutAt = lastLogoutAt; }

    public String getGoogleSubjectId() { return googleSubjectId; }
    public void setGoogleSubjectId(String googleSubjectId) { this.googleSubjectId = googleSubjectId; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }

    public LocalDateTime getTierEvaluatedAt() { return tierEvaluatedAt; }
    public void setTierEvaluatedAt(LocalDateTime tierEvaluatedAt) { this.tierEvaluatedAt = tierEvaluatedAt; }

    public LocalDateTime getVipMarkedAt() { return vipMarkedAt; }
    public void setVipMarkedAt(LocalDateTime vipMarkedAt) { this.vipMarkedAt = vipMarkedAt; }

    public Long getVipMarkedBy() { return vipMarkedBy; }
    public void setVipMarkedBy(Long vipMarkedBy) { this.vipMarkedBy = vipMarkedBy; }
}