package com.hotelbooking.repository;

import com.hotelbooking.dto.HotelFilterRequest;
import com.hotelbooking.model.Hotel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class HotelSpecification {

    public static Specification<Hotel> filter(HotelFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getName() != null && !request.getName().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + request.getName().toLowerCase() + "%"
                ));
            }

            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("location")),
                        "%" + request.getLocation().toLowerCase() + "%"
                ));
            }

            if (request.getIsActive() != null) {
                predicates.add(cb.equal(
                        root.get("isActive"),
                        request.getIsActive()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
