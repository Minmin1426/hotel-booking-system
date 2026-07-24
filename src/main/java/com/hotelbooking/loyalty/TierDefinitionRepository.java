package com.hotelbooking.loyalty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TierDefinitionRepository extends JpaRepository<TierDefinition, Long> {

    List<TierDefinition> findByAccountTypeOrderByMinAnnualSpendAsc(String accountType);

    Optional<TierDefinition> findByName(String name);

    /**
     * Find the highest tier where minAnnualSpend <= annualSpend.
     * Ordered by minAnnualSpend DESC so first result = most applicable tier.
     */
    @Query("SELECT t FROM TierDefinition t WHERE t.accountType = :accountType AND t.minAnnualSpend <= :annualSpend ORDER BY t.minAnnualSpend DESC")
    List<TierDefinition> findMatchingTiersOrdered(
            @Param("accountType") String accountType,
            @Param("annualSpend") java.math.BigDecimal annualSpend);
}
