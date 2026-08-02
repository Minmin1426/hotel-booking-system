package com.hotelbooking.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelApprovalRequestRepository extends JpaRepository<HotelApprovalRequest, Long> {
    List<HotelApprovalRequest> findByStatus(HotelApprovalRequest.ApprovalStatus status);
}
