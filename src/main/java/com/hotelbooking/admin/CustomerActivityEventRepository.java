package com.hotelbooking.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerActivityEventRepository extends JpaRepository<CustomerActivityEvent, Long> {

    Page<CustomerActivityEvent> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<CustomerActivityEvent> findByUserUserIdAndEventTypeOrderByCreatedAtDesc(Long userId, String eventType, Pageable pageable);

    @Query("SELECT e.eventType, COUNT(e) FROM CustomerActivityEvent e " +
           "WHERE e.user.userId = :userId " +
           "GROUP BY e.eventType")
    Object[] getEventTypeCounts(@Param("userId") Long userId);

    @Query("SELECT COUNT(e) FROM CustomerActivityEvent e WHERE e.user.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
}
