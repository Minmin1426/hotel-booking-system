package com.hotelbooking.booking.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockBookingRowRepository extends JpaRepository<BlockBookingRow, Long> {

    List<BlockBookingRow> findByBlockBookingRequestBlockBookingId(Long blockBookingId);

    List<BlockBookingRow> findByBlockBookingRequestBlockBookingIdAndRowStatus(Long blockBookingId, RowStatus status);

    @Query("SELECT COUNT(r) FROM BlockBookingRow r WHERE r.blockBookingRequest.blockBookingId = :blockBookingId AND r.rowStatus = :status")
    long countByBlockBookingIdAndStatus(@Param("blockBookingId") Long blockBookingId, @Param("status") RowStatus status);

    @Query("SELECT COUNT(r) FROM BlockBookingRow r WHERE r.blockBookingRequest.blockBookingId = :blockBookingId")
    long countByBlockBookingId(@Param("blockBookingId") Long blockBookingId);
}
