package com.hotelbooking.admin;

import com.hotelbooking.user.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CustomerSpecification {

    private String search;
    private String accountType;
    private String tier;
    private String status;
    private Boolean isVip;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime lastLoginAfter;
    private Long groupId; // for corporate group filter

    public Specification<User> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String term = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), term),
                        cb.like(cb.lower(root.get("fullName")), term),
                        cb.like(cb.lower(root.get("phoneNumber")), term),
                        cb.like(cb.lower(root.get("identificationNumber")), term)
                ));
            }

            if (accountType != null && !accountType.isBlank()) {
                predicates.add(cb.equal(root.get("accountType"), accountType));
            }

            if (tier != null && !tier.isBlank()) {
                predicates.add(cb.equal(root.get("currentTier"), tier));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (isVip != null) {
                predicates.add(cb.equal(root.get("isVip"), isVip));
            }

            if (createdAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
            }

            if (createdBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore));
            }

            if (lastLoginAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("lastLoginAt"), lastLoginAfter));
            }

            if (groupId != null) {
                Join<Object, Object> membership = root.join("groupMemberships", JoinType.INNER);
                predicates.add(cb.equal(membership.get("group").get("groupId"), groupId));
            }

            // Exclude admin users from customer list
            predicates.add(cb.notEqual(root.get("role"), "ADMIN"));
            predicates.add(cb.notEqual(root.get("role"), "DIRECTOR"));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
