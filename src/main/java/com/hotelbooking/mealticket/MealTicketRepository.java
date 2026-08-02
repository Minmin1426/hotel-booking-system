package com.hotelbooking.mealticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MealTicketRepository extends JpaRepository<MealTicket, Long> {

    Optional<MealTicket> findByQrCode(String qrCode);

    Page<MealTicket> findByUserUserIdOrderByIssuedAtDesc(Long userId, Pageable pageable);

    Page<MealTicket> findByUserUserIdAndStatusOrderByIssuedAtDesc(Long userId, TicketStatus status, Pageable pageable);

    Page<MealTicket> findByUserUserIdAndTicketTypeCodeOrderByIssuedAtDesc(
            Long userId, String ticketType, Pageable pageable);

    Page<MealTicket> findByUserUserIdAndStatusAndTicketTypeCodeOrderByIssuedAtDesc(
            Long userId, TicketStatus status, String ticketType, Pageable pageable);

    @Modifying
    @Query("UPDATE MealTicket t SET t.status = 'EXPIRED' " +
           "WHERE t.status = 'UNUSED' AND t.expiresAt < :now")
    int expireOldTickets(@Param("now") LocalDateTime now);

    long countByUserUserIdAndStatus(Long userId, TicketStatus status);
}
