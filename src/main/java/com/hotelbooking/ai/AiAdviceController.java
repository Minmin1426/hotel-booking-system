package com.hotelbooking.ai;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.ai.dto.AiAdviceRequest;
import com.hotelbooking.ai.dto.AiAdviceResponse;
import com.hotelbooking.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AiAdviceController {

    @PostMapping("/advice")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AiAdviceResponse>> advice(@Valid @RequestBody AiAdviceRequest request) {
        BigDecimal roomCost = request.getRoomPrice() != null ? BigDecimal.valueOf(request.getRoomCount()).multiply(request.getRoomPrice()) : BigDecimal.ZERO;
        BigDecimal mealCost = request.getMealPrice() != null ? BigDecimal.valueOf(request.getMealCount()).multiply(request.getMealPrice()) : BigDecimal.ZERO;
        BigDecimal total = roomCost.add(mealCost);

        List<String> suggestions = List.of(
                "Combo tiệc đoàn: buffet + phòng họp",
                "Ưu tiên gói ăn tối ưu theo số lượng khách",
                "Tạo voucher giảm giá cho nhóm trên 10 người"
        );

        AiAdviceResponse response = AiAdviceResponse.builder()
                .recommendation("Gợi ý combo phù hợp cho đoàn " + request.getGroupSize() + " người")
                .suggestions(suggestions)
                .estimatedRoomCost(roomCost)
                .estimatedMealCost(mealCost)
                .estimatedTotal(total)
                .build();

        return ResponseEntity.ok(ApiResponse.success("AI advice generated", response));
    }
}
