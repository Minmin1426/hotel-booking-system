package com.hotelbooking.operations;

import com.hotelbooking.operations.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationsServiceTest {

    @Mock private RestaurantAreaRepository restaurantAreaRepository;
    @Mock private RoomGroupAllotmentRepository roomGroupAllotmentRepository;
    @Mock private RoomMatrixRepository roomMatrixRepository;
    @Mock private MealPackageRepository mealPackageRepository;
    @Mock private OpsMealTicketRepository mealTicketRepository;
    @Mock private GroupPricingRuleRepository groupPricingRuleRepository;
    @Mock private CancellationRequestRepository cancellationRequestRepository;
    @Mock private HotelApprovalRequestRepository hotelApprovalRequestRepository;

    @InjectMocks
    private OperationsServiceImpl operationsService;

    @Test
    void testCalculateDynamicPrice_GroupDiscount() {
        DynamicPricingCalcDto.Request req = new DynamicPricingCalcDto.Request();
        req.setHotelId(1L);
        req.setRoomQuantity(12); // >10 rooms -> 15% discount
        req.setOriginalUnitPrice(new BigDecimal("1000000"));
        req.setIsWeekend(false);

        DynamicPricingCalcDto.Response response = operationsService.calculateDynamicPrice(req);

        assertNotNull(response);
        assertEquals(12, response.getRoomQuantity());
        assertEquals(new BigDecimal("15.0"), response.getDiscountPercentage());
        assertEquals(new BigDecimal("1800000.0"), response.getDiscountAmount());
        assertEquals(new BigDecimal("10200000.0"), response.getFinalTotalPrice());
    }

    @Test
    void testScanAndRedeemMealTicket_Success() {
        MealTicket ticket = MealTicket.builder()
                .ticketCode("QR-TEST-001")
                .guestName("Nguyễn Văn B")
                .packageName("Buffet Sáng")
                .totalMeals(3)
                .remainingMeals(3)
                .status(MealTicket.TicketStatus.ACTIVE)
                .build();

        when(mealTicketRepository.findByTicketCode("QR-TEST-001")).thenReturn(Optional.of(ticket));

        QrScanRequestDto.Request req = new QrScanRequestDto.Request("QR-TEST-001", 1);
        QrScanRequestDto.Response res = operationsService.scanAndRedeemMealTicket(req);

        assertTrue(res.getIsValid());
        assertEquals(2, res.getRemainingMeals());
        verify(mealTicketRepository).save(any(MealTicket.class));
    }
}
