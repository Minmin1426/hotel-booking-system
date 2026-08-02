package com.hotelbooking.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancellationRequestRepository extends JpaRepository<CancellationRequest, Long> {
    List<CancellationRequest> findByHotelId(Long hotelId);
    List<CancellationRequest> findByStatus(CancellationRequest.RequestStatus status);
}
