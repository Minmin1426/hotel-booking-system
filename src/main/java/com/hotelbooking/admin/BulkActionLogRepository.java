package com.hotelbooking.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkActionLogRepository extends JpaRepository<BulkActionLog, Long> {

    Page<BulkActionLog> findByAdminUserIdOrderByCreatedAtDesc(Long adminId, Pageable pageable);
}
