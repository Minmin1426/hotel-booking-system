package com.hotelbooking.operations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupPricingRuleRepository extends JpaRepository<GroupPricingRule, Long> {
    List<GroupPricingRule> findByHotelId(Long hotelId);
    List<GroupPricingRule> findByHotelIdAndIsActiveTrue(Long hotelId);
}
