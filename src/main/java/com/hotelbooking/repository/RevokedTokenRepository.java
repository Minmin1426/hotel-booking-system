package com.hotelbooking.repository;

import com.hotelbooking.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM RevokedToken r WHERE r.expiryTime < :time")
    int deleteByExpiryTimeBefore(@Param("time") LocalDateTime time);
}
