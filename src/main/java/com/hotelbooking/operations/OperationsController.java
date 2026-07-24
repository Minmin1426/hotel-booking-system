package com.hotelbooking.operations;

import com.hotelbooking.operations.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OperationsController {

    private final OperationsService operationsService;

    // SCR-201: Dashboard Tổng quan
    @GetMapping("/dashboard")
    public ResponseEntity<OperationsDashboardDto> getDashboard(@RequestParam(defaultValue = "1") Long hotelId) {
        return ResponseEntity.ok(operationsService.getDashboard(hotelId));
    }

    // SCR-202: Trang Đăng ký Khách sạn & Tiện ích Nhà hàng
    @GetMapping("/restaurant-areas")
    public ResponseEntity<List<RestaurantArea>> getRestaurantAreas(@RequestParam(defaultValue = "1") Long hotelId) {
        return ResponseEntity.ok(operationsService.getRestaurantAreasByHotel(hotelId));
    }

    @PostMapping("/restaurant-areas")
    public ResponseEntity<RestaurantArea> createRestaurantArea(@RequestBody RestaurantArea area) {
        if (area.getHotelId() == null) area.setHotelId(1L);
        return ResponseEntity.ok(operationsService.createRestaurantArea(area));
    }

    @PutMapping("/restaurant-areas/{id}")
    public ResponseEntity<RestaurantArea> updateRestaurantArea(@PathVariable Long id, @RequestBody RestaurantArea area) {
        return ResponseEntity.ok(operationsService.updateRestaurantArea(id, area));
    }

    @DeleteMapping("/restaurant-areas/{id}")
    public ResponseEntity<Void> deleteRestaurantArea(@PathVariable Long id) {
        operationsService.deleteRestaurantArea(id);
        return ResponseEntity.noContent().build();
    }

    // SCR-203: Quản lý Loại phòng & Quỹ phòng cho Đoàn
    @GetMapping("/room-allotments")
    public ResponseEntity<List<RoomGroupAllotment>> getRoomAllotments(@RequestParam(defaultValue = "1") Long hotelId) {
        return ResponseEntity.ok(operationsService.getAllotmentsByHotel(hotelId));
    }

    @PostMapping("/room-allotments")
    public ResponseEntity<RoomGroupAllotment> saveRoomAllotment(@RequestBody RoomGroupAllotment allotment) {
        if (allotment.getHotelId() == null) allotment.setHotelId(1L);
        return ResponseEntity.ok(operationsService.createOrUpdateAllotment(allotment));
    }

    // SCR-204: Trang Sơ đồ Phòng Real-time (Room Matrix)
    @GetMapping("/room-matrix")
    public ResponseEntity<List<RoomMatrixState>> getRoomMatrix(
            @RequestParam(defaultValue = "1") Long hotelId,
            @RequestParam(required = false) Integer floor) {
        return ResponseEntity.ok(operationsService.getRoomMatrix(hotelId, floor));
    }

    @PutMapping("/room-matrix/{id}/status")
    public ResponseEntity<RoomMatrixState> updateRoomStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        RoomMatrixState.RoomStatus status = RoomMatrixState.RoomStatus.valueOf(payload.get("status"));
        String notes = payload.get("notes");
        return ResponseEntity.ok(operationsService.updateRoomStatus(id, status, notes));
    }

    // SCR-205: Phân bổ Phòng hàng loạt cho Đoàn
    @PostMapping("/group-allocate")
    public ResponseEntity<List<RoomMatrixState>> allocateGroupRooms(@RequestBody GroupAllocationRequestDto request) {
        return ResponseEntity.ok(operationsService.allocateGroupRooms(request));
    }

    // SCR-206: Trang Quản lý Menu Nhà hàng & Gói Vé ăn
    @GetMapping("/meal-packages")
    public ResponseEntity<List<MealPackage>> getMealPackages(@RequestParam(defaultValue = "1") Long hotelId) {
        return ResponseEntity.ok(operationsService.getMealPackages(hotelId));
    }

    @PostMapping("/meal-packages")
    public ResponseEntity<MealPackage> createMealPackage(@RequestBody MealPackage mealPackage) {
        if (mealPackage.getHotelId() == null) mealPackage.setHotelId(1L);
        return ResponseEntity.ok(operationsService.createMealPackage(mealPackage));
    }

    @PutMapping("/meal-packages/{id}")
    public ResponseEntity<MealPackage> updateMealPackage(@PathVariable Long id, @RequestBody MealPackage mealPackage) {
        return ResponseEntity.ok(operationsService.updateMealPackage(id, mealPackage));
    }

    // SCR-207: Trang Quét Mã QR Vé ăn tại Nhà hàng
    @PostMapping("/qr-scan")
    public ResponseEntity<QrScanRequestDto.Response> scanMealTicket(@RequestBody QrScanRequestDto.Request request) {
        return ResponseEntity.ok(operationsService.scanAndRedeemMealTicket(request));
    }

    // SCR-208: Trang Cấu hình Giá phòng & Chiết khấu Đoàn
    @GetMapping("/pricing-rules")
    public ResponseEntity<List<GroupPricingRule>> getPricingRules(@RequestParam(defaultValue = "1") Long hotelId) {
        return ResponseEntity.ok(operationsService.getPricingRules(hotelId));
    }

    @PostMapping("/pricing-rules")
    public ResponseEntity<GroupPricingRule> savePricingRule(@RequestBody GroupPricingRule rule) {
        if (rule.getHotelId() == null) rule.setHotelId(1L);
        return ResponseEntity.ok(operationsService.savePricingRule(rule));
    }

    @PostMapping("/pricing-calc")
    public ResponseEntity<DynamicPricingCalcDto.Response> calculatePricing(@RequestBody DynamicPricingCalcDto.Request request) {
        return ResponseEntity.ok(operationsService.calculateDynamicPrice(request));
    }

    // SCR-209: Trang Duyệt Yêu cầu Hủy phòng & Hoàn tiền (Partner Desk)
    @GetMapping("/cancellations")
    public ResponseEntity<List<CancellationRequest>> getCancellations(@RequestParam(defaultValue = "1") Long hotelId) {
        return ResponseEntity.ok(operationsService.getCancellationRequests(hotelId));
    }

    @PostMapping("/cancellations")
    public ResponseEntity<CancellationRequest> createCancellation(@RequestBody CancellationRequest request) {
        if (request.getHotelId() == null) request.setHotelId(1L);
        return ResponseEntity.ok(operationsService.createCancellationRequest(request));
    }

    @PutMapping("/cancellations/{id}/process")
    public ResponseEntity<CancellationRequest> processCancellation(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        CancellationRequest.RequestStatus status = CancellationRequest.RequestStatus.valueOf(payload.get("status"));
        String note = payload.get("note");
        return ResponseEntity.ok(operationsService.processCancellationRequest(id, status, note));
    }

    // SCR-210: Trang Admin View: Duyệt Khách sạn & Tổ hợp Nhà hàng
    @GetMapping("/hotel-approvals")
    public ResponseEntity<List<HotelApprovalRequest>> getPendingApprovals() {
        return ResponseEntity.ok(operationsService.getPendingHotelApprovals());
    }

    @PostMapping("/hotel-approvals")
    public ResponseEntity<HotelApprovalRequest> submitApproval(@RequestBody HotelApprovalRequest request) {
        return ResponseEntity.ok(operationsService.submitHotelApproval(request));
    }

    @PutMapping("/hotel-approvals/{id}/review")
    public ResponseEntity<HotelApprovalRequest> reviewApproval(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        HotelApprovalRequest.ApprovalStatus status = HotelApprovalRequest.ApprovalStatus.valueOf(payload.get("status"));
        String comment = payload.get("comment");
        return ResponseEntity.ok(operationsService.reviewHotelApproval(id, status, comment));
    }
}
