package com.hotelbooking.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository("opsMealTicketRepository")
public interface OpsMealTicketRepository extends JpaRepository<MealTicket, Long> {
    Optional<MealTicket> findByTicketCode(String ticketCode);
    List<MealTicket> findByBookingId(Long bookingId);
}
