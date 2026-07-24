package com.hotelbooking.operations;

import com.hotelbooking.operations.dto.*;

import java.util.List;

public interface OperationsService {
    OperationsDashboardDto getDashboard(Long hotelId);
    RestaurantArea createRestaurantArea(RestaurantArea area);
    List<RestaurantArea> getRestaurantAreasByHotel(Long hotelId);
    RestaurantArea updateRestaurantArea(Long id, RestaurantArea area);
    void deleteRestaurantArea(Long id);
    RoomGroupAllotment createOrUpdateAllotment(RoomGroupAllotment allotment);
    List<RoomGroupAllotment> getAllotmentsByHotel(Long hotelId);
    List<RoomMatrixState> getRoomMatrix(Long hotelId, Integer floor);
    RoomMatrixState updateRoomStatus(Long roomMatrixStateId, RoomMatrixState.RoomStatus status, String notes);
    List<RoomMatrixState> seedInitialMatrix(Long hotelId);
    List<RoomMatrixState> allocateGroupRooms(GroupAllocationRequestDto request);
    MealPackage createMealPackage(MealPackage mealPackage);
    List<MealPackage> getMealPackages(Long hotelId);
    MealPackage updateMealPackage(Long id, MealPackage mealPackage);
    QrScanRequestDto.Response scanAndRedeemMealTicket(QrScanRequestDto.Request request);
    List<MealTicket> getTicketsByBooking(Long bookingId);
    GroupPricingRule savePricingRule(GroupPricingRule rule);
    List<GroupPricingRule> getPricingRules(Long hotelId);
    DynamicPricingCalcDto.Response calculateDynamicPrice(DynamicPricingCalcDto.Request request);
    CancellationRequest createCancellationRequest(CancellationRequest request);
    List<CancellationRequest> getCancellationRequests(Long hotelId);
    CancellationRequest processCancellationRequest(Long requestId, CancellationRequest.RequestStatus status, String partnerNote);
    HotelApprovalRequest submitHotelApproval(HotelApprovalRequest request);
    List<HotelApprovalRequest> getPendingHotelApprovals();
    HotelApprovalRequest reviewHotelApproval(Long requestId, HotelApprovalRequest.ApprovalStatus status, String adminComment);
}
