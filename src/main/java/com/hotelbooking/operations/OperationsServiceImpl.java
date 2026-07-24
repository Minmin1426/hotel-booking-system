package com.hotelbooking.operations;

import com.hotelbooking.operations.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationsServiceImpl implements OperationsService {

    private final RestaurantAreaRepository restaurantAreaRepository;
    private final RoomGroupAllotmentRepository roomGroupAllotmentRepository;
    private final RoomMatrixRepository roomMatrixRepository;
    private final MealPackageRepository mealPackageRepository;
    private final OpsMealTicketRepository mealTicketRepository;
    private final GroupPricingRuleRepository groupPricingRuleRepository;
    private final CancellationRequestRepository cancellationRequestRepository;
    private final HotelApprovalRequestRepository hotelApprovalRequestRepository;

    @Override
    public OperationsDashboardDto getDashboard(Long hotelId) {
        List<RoomMatrixState> matrix = roomMatrixRepository.findByHotelId(hotelId);
        if (matrix.isEmpty()) {
            matrix = seedInitialMatrix(hotelId);
        }

        long totalRooms = matrix.size();
        long occupiedRooms = matrix.stream().filter(r -> r.getStatus() == RoomMatrixState.RoomStatus.OCCUPIED).count();
        double occupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100.0 : 75.5;

        List<OperationsDashboardDto.UpcomingGroupDto> upcomingGroups = List.of(
            OperationsDashboardDto.UpcomingGroupDto.builder()
                .bookingId(101L)
                .groupName("Đoàn Khách Du Lịch Saigontourist")
                .leaderName("Nguyễn Văn Hùng")
                .contactPhone("0908123456")
                .roomCount(15)
                .checkInDate("2026-07-25")
                .checkOutDate("2026-07-28")
                .status("CONFIRMED")
                .isAllocated(true)
                .build(),
            OperationsDashboardDto.UpcomingGroupDto.builder()
                .bookingId(102L)
                .groupName("Hội thảo Công ty FPT Software")
                .leaderName("Trần Thị Mai")
                .contactPhone("0912987654")
                .roomCount(22)
                .checkInDate("2026-07-26")
                .checkOutDate("2026-07-29")
                .status("PENDING_ALLOCATION")
                .isAllocated(false)
                .build()
        );

        return OperationsDashboardDto.builder()
                .occupancyRate(Math.round(occupancyRate * 10.0) / 10.0)
                .todayRoomRevenue(new BigDecimal("48500000"))
                .todayRestaurantRevenue(new BigDecimal("18200000"))
                .totalGroupArrivalsToday(upcomingGroups.size())
                .upcomingGroups(upcomingGroups)
                .build();
    }

    @Override
    public RestaurantArea createRestaurantArea(RestaurantArea area) {
        return restaurantAreaRepository.save(area);
    }

    @Override
    public List<RestaurantArea> getRestaurantAreasByHotel(Long hotelId) {
        List<RestaurantArea> list = restaurantAreaRepository.findByHotelId(hotelId);
        if (list.isEmpty()) {
            list = List.of(
                RestaurantArea.builder().hotelId(hotelId).areaName("Sảnh Ăn Chính Grand Ballroom").seatingCapacity(250).tableCount(30).kitchenCapacity(500).status(RestaurantArea.AreaStatus.ACTIVE).build(),
                RestaurantArea.builder().hotelId(hotelId).areaName("Khu VIP Sky Lounge").seatingCapacity(60).tableCount(8).kitchenCapacity(100).status(RestaurantArea.AreaStatus.ACTIVE).build()
            );
            restaurantAreaRepository.saveAll(list);
        }
        return list;
    }

    @Override
    public RestaurantArea updateRestaurantArea(Long id, RestaurantArea area) {
        area.setId(id);
        return restaurantAreaRepository.save(area);
    }

    @Override
    public void deleteRestaurantArea(Long id) {
        restaurantAreaRepository.deleteById(id);
    }

    @Override
    public RoomGroupAllotment createOrUpdateAllotment(RoomGroupAllotment allotment) {
        return roomGroupAllotmentRepository.save(allotment);
    }

    @Override
    public List<RoomGroupAllotment> getAllotmentsByHotel(Long hotelId) {
        List<RoomGroupAllotment> list = roomGroupAllotmentRepository.findByHotelId(hotelId);
        if (list.isEmpty()) {
            list = List.of(
                RoomGroupAllotment.builder().hotelId(hotelId).roomType("Deluxe Ocean View").totalRoomsAvailable(30).maxGroupQuota(18).currentAllocatedCount(12).groupBasePrice(new BigDecimal("1450000")).notes("Được phép giữ tối đa 18 phòng cho đoàn").build(),
                RoomGroupAllotment.builder().hotelId(hotelId).roomType("Standard Twin").totalRoomsAvailable(40).maxGroupQuota(25).currentAllocatedCount(20).groupBasePrice(new BigDecimal("950000")).notes("Đầy đủ giường đơn cho khách đoàn").build()
            );
            roomGroupAllotmentRepository.saveAll(list);
        }
        return list;
    }

    @Override
    public List<RoomMatrixState> getRoomMatrix(Long hotelId, Integer floor) {
        List<RoomMatrixState> list;
        if (floor != null && floor > 0) {
            list = roomMatrixRepository.findByHotelIdAndFloor(hotelId, floor);
        } else {
            list = roomMatrixRepository.findByHotelId(hotelId);
        }
        if (list.isEmpty()) {
            list = seedInitialMatrix(hotelId);
        }
        return list;
    }

    @Override
    public RoomMatrixState updateRoomStatus(Long roomMatrixStateId, RoomMatrixState.RoomStatus status, String notes) {
        RoomMatrixState state = roomMatrixRepository.findById(roomMatrixStateId)
                .orElseThrow(() -> new RuntimeException("Room matrix item not found: " + roomMatrixStateId));
        state.setStatus(status);
        if (notes != null) state.setNotes(notes);
        if (status == RoomMatrixState.RoomStatus.HOUSEKEEPING) {
            state.setLastHousekeepingAt(LocalDateTime.now());
        }
        return roomMatrixRepository.save(state);
    }

    @Override
    public List<RoomMatrixState> seedInitialMatrix(Long hotelId) {
        List<RoomMatrixState> list = new ArrayList<>();
        for (int f = 1; f <= 3; f++) {
            for (int r = 1; r <= 10; r++) {
                String roomNum = String.format("%d%02d", f, r);
                RoomMatrixState.RoomStatus status = RoomMatrixState.RoomStatus.AVAILABLE;
                String guest = null;
                String group = null;

                if (f == 1 && r <= 4) {
                    status = RoomMatrixState.RoomStatus.OCCUPIED;
                    guest = "Đoàn Saigontourist - Khách " + r;
                    group = "Saigontourist Group";
                } else if (f == 2 && r == 3) {
                    status = RoomMatrixState.RoomStatus.HOUSEKEEPING;
                }

                list.add(RoomMatrixState.builder()
                        .hotelId(hotelId)
                        .roomNumber(roomNum)
                        .floor(f)
                        .roomType(f == 3 ? "Executive Suite" : (f == 2 ? "Deluxe Ocean" : "Standard Twin"))
                        .status(status)
                        .currentGuestName(guest)
                        .groupName(group)
                        .build());
            }
        }
        return roomMatrixRepository.saveAll(list);
    }

    @Override
    public List<RoomMatrixState> allocateGroupRooms(GroupAllocationRequestDto request) {
        List<RoomMatrixState> states = roomMatrixRepository.findAllById(request.getRoomMatrixStateIds());
        for (RoomMatrixState s : states) {
            s.setStatus(RoomMatrixState.RoomStatus.OCCUPIED);
            s.setGroupName(request.getGroupName());
            s.setAssignedBookingId(request.getBookingId());
        }
        return roomMatrixRepository.saveAll(states);
    }

    @Override
    public MealPackage createMealPackage(MealPackage mealPackage) {
        return mealPackageRepository.save(mealPackage);
    }

    @Override
    public List<MealPackage> getMealPackages(Long hotelId) {
        List<MealPackage> list = mealPackageRepository.findByHotelId(hotelId);
        if (list.isEmpty()) {
            list = List.of(
                MealPackage.builder().hotelId(hotelId).packageCode("BUF-BRK-01").packageName("Buffet Sáng Quốc Tế Buffet Premium").category(MealPackage.MealCategory.BREAKFAST_BUFFET).pricePerPax(new BigDecimal("250000")).dishesDescription("Hơn 50 món Á - Âu, Phở truyền thống, Quầy Bánh mỳ & Trái cây tươi").isActive(true).build(),
                MealPackage.builder().hotelId(hotelId).packageName("Set Menu Trưa Hải Sản Biển").packageCode("SET-LNC-02").category(MealPackage.MealCategory.LUNCH_SET).pricePerPax(new BigDecimal("350000")).dishesDescription("Súp hải sản, Tôm hùm bỏ lò, Cá chẽm hấp gừng, Lẩu hải sản").isActive(true).build()
            );
            mealPackageRepository.saveAll(list);
        }
        return list;
    }

    @Override
    public MealPackage updateMealPackage(Long id, MealPackage mealPackage) {
        mealPackage.setId(id);
        return mealPackageRepository.save(mealPackage);
    }

    @Override
    public QrScanRequestDto.Response scanAndRedeemMealTicket(QrScanRequestDto.Request request) {
        MealTicket ticket = mealTicketRepository.findByTicketCode(request.getTicketCode())
                .orElseGet(() -> {
                    MealTicket mock = MealTicket.builder()
                            .ticketCode(request.getTicketCode())
                            .guestName("Nguyễn Văn A (Phòng 204)")
                            .roomNumber("204")
                            .mealPackageId(1L)
                            .packageName("Buffet Sáng Quốc Tế")
                            .totalMeals(4)
                            .remainingMeals(3)
                            .validUntil(LocalDateTime.now().plusDays(2))
                            .status(MealTicket.TicketStatus.ACTIVE)
                            .build();
                    return mealTicketRepository.save(mock);
                });

        if (ticket.getStatus() != MealTicket.TicketStatus.ACTIVE || ticket.getRemainingMeals() <= 0) {
            return QrScanRequestDto.Response.builder()
                    .isValid(false)
                    .message("Vé ăn không hợp lệ hoặc đã hết lượt sử dụng!")
                    .ticketCode(ticket.getTicketCode())
                    .guestName(ticket.getGuestName())
                    .roomNumber(ticket.getRoomNumber())
                    .packageName(ticket.getPackageName())
                    .remainingMeals(ticket.getRemainingMeals())
                    .totalMeals(ticket.getTotalMeals())
                    .scanTime(LocalDateTime.now())
                    .build();
        }

        int redeem = request.getRedeemCount() != null ? request.getRedeemCount() : 1;
        int newRemaining = Math.max(0, ticket.getRemainingMeals() - redeem);
        ticket.setRemainingMeals(newRemaining);
        if (newRemaining == 0) {
            ticket.setStatus(MealTicket.TicketStatus.EXHAUSTED);
        }
        mealTicketRepository.save(ticket);

        return QrScanRequestDto.Response.builder()
                .isValid(true)
                .message("Xác nhận vé hợp lệ! Đã trừ " + redeem + " lượt ăn thành công.")
                .ticketCode(ticket.getTicketCode())
                .guestName(ticket.getGuestName())
                .roomNumber(ticket.getRoomNumber())
                .packageName(ticket.getPackageName())
                .remainingMeals(newRemaining)
                .totalMeals(ticket.getTotalMeals())
                .scanTime(LocalDateTime.now())
                .build();
    }

    @Override
    public List<MealTicket> getTicketsByBooking(Long bookingId) {
        return mealTicketRepository.findByBookingId(bookingId);
    }

    @Override
    public GroupPricingRule savePricingRule(GroupPricingRule rule) {
        return groupPricingRuleRepository.save(rule);
    }

    @Override
    public List<GroupPricingRule> getPricingRules(Long hotelId) {
        List<GroupPricingRule> list = groupPricingRuleRepository.findByHotelId(hotelId);
        if (list.isEmpty()) {
            list = List.of(
                GroupPricingRule.builder().hotelId(hotelId).minRooms(5).discountPercent(new BigDecimal("10.0")).weekendSurchargePercent(new BigDecimal("15.0")).peakSeasonMultiplier(new BigDecimal("1.2")).description("Đặt từ 5 đến 9 phòng: Chiết khấu 10%").isActive(true).build(),
                GroupPricingRule.builder().hotelId(hotelId).minRooms(10).discountPercent(new BigDecimal("15.0")).weekendSurchargePercent(new BigDecimal("15.0")).peakSeasonMultiplier(new BigDecimal("1.2")).description("Đặt từ 10 đến 19 phòng: Chiết khấu 15%").isActive(true).build()
            );
            groupPricingRuleRepository.saveAll(list);
        }
        return list;
    }

    @Override
    public DynamicPricingCalcDto.Response calculateDynamicPrice(DynamicPricingCalcDto.Request req) {
        int qty = req.getRoomQuantity() != null ? req.getRoomQuantity() : 1;
        BigDecimal unitPrice = req.getOriginalUnitPrice() != null ? req.getOriginalUnitPrice() : new BigDecimal("1200000");

        BigDecimal discountPct = BigDecimal.ZERO;
        if (qty >= 20) discountPct = new BigDecimal("20.0");
        else if (qty >= 10) discountPct = new BigDecimal("15.0");
        else if (qty >= 5) discountPct = new BigDecimal("10.0");

        BigDecimal baseTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
        BigDecimal discountAmt = baseTotal.multiply(discountPct).divide(new BigDecimal("100"));
        
        BigDecimal weekendSurcharge = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(req.getIsWeekend())) {
            weekendSurcharge = baseTotal.multiply(new BigDecimal("15.0")).divide(new BigDecimal("100"));
        }

        BigDecimal finalTotal = baseTotal.subtract(discountAmt).add(weekendSurcharge);
        BigDecimal finalUnitPrice = finalTotal.divide(BigDecimal.valueOf(qty), 2, java.math.RoundingMode.HALF_UP);

        String summary = String.format("Áp dụng chiết khấu đoàn %s%% cho %d phòng", discountPct, qty);

        return DynamicPricingCalcDto.Response.builder()
                .roomQuantity(qty)
                .originalUnitPrice(unitPrice)
                .baseTotalPrice(baseTotal)
                .discountPercentage(discountPct)
                .discountAmount(discountAmt)
                .weekendSurchargeAmount(weekendSurcharge)
                .finalTotalPrice(finalTotal)
                .finalEffectiveUnitPrice(finalUnitPrice)
                .AppliedRuleSummary(summary)
                .build();
    }

    @Override
    public CancellationRequest createCancellationRequest(CancellationRequest request) {
        return cancellationRequestRepository.save(request);
    }

    @Override
    public List<CancellationRequest> getCancellationRequests(Long hotelId) {
        List<CancellationRequest> list = cancellationRequestRepository.findByHotelId(hotelId);
        if (list.isEmpty()) {
            list = List.of(
                CancellationRequest.builder().hotelId(hotelId).bookingId(501L).bookingCode("BK-88391").customerName("Đoàn Cty Sungroup").customerPhone("0934112233").reason("Hủy chuyến do thay đổi lịch trình công tác đột xuất").totalBookingAmount(new BigDecimal("35000000")).calculatedRefundAmount(new BigDecimal("28000000")).refundPercentage(80).status(CancellationRequest.RequestStatus.PENDING).build()
            );
            cancellationRequestRepository.saveAll(list);
        }
        return list;
    }

    @Override
    public CancellationRequest processCancellationRequest(Long requestId, CancellationRequest.RequestStatus status, String partnerNote) {
        CancellationRequest req = cancellationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Cancellation request not found: " + requestId));
        req.setStatus(status);
        req.setPartnerNote(partnerNote);
        req.setProcessedAt(LocalDateTime.now());
        return cancellationRequestRepository.save(req);
    }

    @Override
    public HotelApprovalRequest submitHotelApproval(HotelApprovalRequest request) {
        return hotelApprovalRequestRepository.save(request);
    }

    @Override
    public List<HotelApprovalRequest> getPendingHotelApprovals() {
        List<HotelApprovalRequest> list = hotelApprovalRequestRepository.findByStatus(HotelApprovalRequest.ApprovalStatus.PENDING);
        if (list.isEmpty()) {
            list = List.of(
                HotelApprovalRequest.builder().hotelName("Khách Sạn Mới Horizon Luxury Resort & Spa").location("Bãi Dài, Phú Quốc").contactEmail("contact@horizonresort.vn").contactPhone("02973889900").foodSafetyCertNumber("ATTP-2026-8891/SYT").certExpiryDate(java.time.LocalDate.of(2028, 12, 31)).restaurantSeatingCapacity(300).status(HotelApprovalRequest.ApprovalStatus.PENDING).build()
            );
            hotelApprovalRequestRepository.saveAll(list);
        }
        return list;
    }

    @Override
    public HotelApprovalRequest reviewHotelApproval(Long requestId, HotelApprovalRequest.ApprovalStatus status, String adminComment) {
        HotelApprovalRequest req = hotelApprovalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Approval request not found: " + requestId));
        req.setStatus(status);
        req.setAdminComment(adminComment);
        req.setReviewedAt(LocalDateTime.now());
        return hotelApprovalRequestRepository.save(req);
    }
}
