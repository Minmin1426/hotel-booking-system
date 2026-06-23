# Implementation Plan: 004-payment-billing

**Branch**: `004-payment-billing` | **Date**: 2026-06-23 | **Spec**: [spec.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/004-payment-billing/spec.md)

## Summary
Implement payment transaction processing, webhook endpoint validation, refund logic, and voucher discounting system.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Security, Spring Data JPA
- **Storage**: SQL Server
- **Testing**: JUnit 5, Mockito

## Constitution Check
- **Feature Package**: `com.hotelbooking.payment`, `com.hotelbooking.voucher`
- **Secrets Management**: Webhook signing keys must be loaded from environment variables (`payment.gateway.secret`).
- **DTOs**: Accept request DTOs (`ApplyVoucherRequestDTO`) and return standardized response objects.

## Project Structure

### Source Code
- `src/main/java/com/hotelbooking/payment/`
  - [PaymentController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/payment/PaymentController.java)
  - [PaymentService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/payment/PaymentService.java)
  - [PaymentServiceImpl.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/payment/PaymentServiceImpl.java)
  - [PaymentRepository.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/payment/PaymentRepository.java)
  - [PaymentAuditLogRepository.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/payment/PaymentAuditLogRepository.java)
- `src/main/java/com/hotelbooking/voucher/`
  - [VoucherController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/voucher/VoucherController.java)
  - [VoucherService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/voucher/VoucherService.java)
  - [VoucherServiceImpl.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/voucher/VoucherServiceImpl.java)
  - [VoucherRepository.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/voucher/VoucherRepository.java)

### Testing
- `src/test/java/com/hotelbooking/`
  - [PaymentServiceTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/service/PaymentServiceTest.java)
  - [VoucherServiceTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/voucher/VoucherServiceTest.java)
