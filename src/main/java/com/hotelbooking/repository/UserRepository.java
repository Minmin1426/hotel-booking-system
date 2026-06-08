package com.hotelbooking.repository;

import com.hotelbooking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Page<User> findAll(Pageable pageable);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.userId <> :excludeId")
    boolean existsByEmailAndUserIdNot(
            @Param("email") String email,
            @Param("excludeId") Long excludeId);

    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.userId = :userId")
    void updateStatus(@Param("userId") Long userId, @Param("status") String status);
}