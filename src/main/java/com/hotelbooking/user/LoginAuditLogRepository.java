package com.hotelbooking.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAuditLogRepository extends JpaRepository<LoginAuditLog, Long> {
}
