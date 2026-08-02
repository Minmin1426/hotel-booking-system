package com.hotelbooking.payment.refund;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundAuditLogRepository extends JpaRepository<RefundAuditLog, Long> {

    Page<RefundAuditLog> findByBookingIdOrderByCreatedAtDesc(Long bookingId, Pageable pageable);

    Page<RefundAuditLog> findByPaymentIdOrderByCreatedAtDesc(Long paymentId, Pageable pageable);

    Page<RefundAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
