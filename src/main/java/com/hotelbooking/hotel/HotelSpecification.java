package com.hotelbooking.hotel;
import com.hotelbooking.hotel.dto.HotelFilterRequest;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class HotelSpecification {

    public static Specification<Hotel> filter(HotelFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getName() != null && !request.getName().isBlank()) {
                String searchPattern = "%" + request.getName().toLowerCase().replace(" ", "") + "%";
                predicates.add(cb.like(
                        cb.lower(cb.function("REPLACE", String.class, root.get("name"), cb.literal(" "), cb.literal(""))),
                        searchPattern
                ));
            }

            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                String searchPattern = "%" + request.getLocation().toLowerCase().replace(" ", "") + "%";
                predicates.add(cb.like(
                        cb.lower(cb.function("REPLACE", String.class, root.get("location"), cb.literal(" "), cb.literal(""))),
                        searchPattern
                ));
            }

            if (request.getIsActive() != null) {
                predicates.add(cb.equal(
                        root.get("isActive"),
                        request.getIsActive()
                ));
            }

            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String searchPattern = "%" + request.getKeyword().toLowerCase().replace(" ", "") + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.function("REPLACE", String.class, root.get("name"), cb.literal(" "), cb.literal(""))), searchPattern),
                        cb.like(cb.lower(cb.function("REPLACE", String.class, root.get("location"), cb.literal(" "), cb.literal(""))), searchPattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
