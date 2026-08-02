package com.hotelbooking.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    Page<CustomerNote> findByUserUserIdOrderByIsPinnedDescCreatedAtDesc(Long userId, Pageable pageable);

    Optional<CustomerNote> findByNoteIdAndUserUserId(Long noteId, Long userId);

    long countByUserUserId(Long userId);

    @Query("SELECT COUNT(n) FROM CustomerNote n WHERE n.user.userId = :userId AND n.isPinned = true")
    long countPinnedByUserId(@Param("userId") Long userId);
}
