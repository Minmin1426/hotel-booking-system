package com.hotelbooking.voucher;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_vouchers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "voucher_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @CreationTimestamp
    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public void markUsed(Booking booking) {
        this.isUsed = true;
        this.booking = booking;
        this.usedAt = LocalDateTime.now();
    }
}
