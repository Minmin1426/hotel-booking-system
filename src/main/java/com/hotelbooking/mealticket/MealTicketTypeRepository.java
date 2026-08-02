package com.hotelbooking.mealticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MealTicketTypeRepository extends JpaRepository<MealTicketType, Long> {
    Optional<MealTicketType> findByCode(String code);
    Optional<MealTicketType> findByCodeAndIsActiveTrue(String code);
}
