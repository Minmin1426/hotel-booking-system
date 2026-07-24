package com.hotelbooking.booking.batch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockBookingRequestRepository extends JpaRepository<BlockBookingRequest, Long> {

    Page<BlockBookingRequest> findByRequesterUserId(Long requesterId, Pageable pageable);

    Page<BlockBookingRequest> findByStatus(BatchStatus status, Pageable pageable);

    @Query("SELECT b FROM BlockBookingRequest b " +
           "LEFT JOIN FETCH b.requester " +
           "WHERE b.blockBookingId = :id")
    Optional<BlockBookingRequest> findByIdWithRequester(@Param("id") Long id);

    @Query("SELECT b FROM BlockBookingRequest b " +
           "LEFT JOIN FETCH b.requester " +
           "LEFT JOIN FETCH b.rows " +
           "WHERE b.blockBookingId = :id")
    Optional<BlockBookingRequest> findByIdWithDetails(@Param("id") Long id);

    long countByStatus(BatchStatus status);
}
