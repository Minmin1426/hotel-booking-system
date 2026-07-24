package com.hotelbooking.user.ctp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CtpAuditLogRepository extends JpaRepository<CtpAuditLog, Long> {

    Page<CtpAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
