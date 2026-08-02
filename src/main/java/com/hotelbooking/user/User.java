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

    // --- Corporate Tax Profile (CTP) fields ---
    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "company_address", columnDefinition = "TEXT")
    private String companyAddress;

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "ctp_status")
    @Builder.Default
    private String ctpStatus = "NOT_SUBMITTED"; // "NOT_SUBMITTED" | "PENDING" | "VERIFIED" | "REJECTED"

    @Column(name = "ctp_verified_at")
    private LocalDateTime ctpVerifiedAt;

    @Column(name = "ctp_verified_by")
    private Long ctpVerifiedBy;

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

    public boolean isCtpVerified() {
        return "VERIFIED".equalsIgnoreCase(ctpStatus);
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
}