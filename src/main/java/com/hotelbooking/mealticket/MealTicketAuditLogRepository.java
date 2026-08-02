package com.hotelbooking.mealticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealTicketAuditLogRepository extends JpaRepository<MealTicketAuditLog, Long> {
    Page<MealTicketAuditLog> findByTicketTicketIdOrderByTimestampDesc(Long ticketId, Pageable pageable);
}
