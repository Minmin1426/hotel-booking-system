# 🚀 PROJECT TRACKING - HOTEL BOOKING SYSTEM (HỆ THỐNG ĐẶT PHÒNG KHÁCH SẠN MỞ RỘNG)

**Project Title**: Hotel Booking System (Standard & Group Booking Engine)  
**Architecture Scale**: 50-Screen Enterprise System Across 5 Roles (10 Screens / Role)  
**Tech Stack**: Java 17 · Spring Boot 4.0.0-M1 · PostgreSQL Neon Cloud DB / SQL Server · Flyway Migrations (V1 - V30) · React 18 + Vite 8 + TailwindCSS · Docker & Docker Compose  
**Status**: 100% Fully Implemented, Tested, Dockerized & Pushed to Remote Git (`origin/main`)  
**Last Updated**: 2026-07-26

---

## 📌 Executive Summary & Dashboard

| Category | Metric / Status | Details / Target |
|---|---|---|
| **Total System Screens** | **50 / 50 Screens (100%)** | 5 Roles (Customer, Manager/Hotel, Receptionist/Housekeeper, Admin/Finance, Director/AI) |
| **Business Use Cases** | **35 / 35 Use Cases (100%)** | Covered in Package-by-Feature (`auth`, `user`, `hotel`, `room`, `booking`, `payment`, `voucher`, `report`, `setting`, `customer-portal`) |
| **Backend Unit/Integration Tests** | **207 / 207 Passed (100%)** | 0 Failures, 0 Errors (`BUILD SUCCESS` via Maven & JDK 18 Wrapper) |
| **Frontend Production Build** | **Success (`built in 1.79s`)** | Vite 8 + TailwindCSS, 0 Compilation Errors, Bundle size optimized |
| **Flyway DB Migrations** | **V1 ➔ V30 (100%)** | Dual Database Support: Neon Cloud PostgreSQL & Local SQL Server |
| **Dockerization** | **Full-Stack Containerized** | Multi-stage `Dockerfile`, `frontend/Dockerfile`, and `docker-compose.yml` |
| **Git Deployment** | **Synced with Remote (`main`)** | Commit history verified, pre-commit security hooks active |

---

## 🗺️ 50-Screen UI Architecture Matrix (SCR-101 ➔ SCR-510)

### 1. Phân Hệ 1: Cổng Khách Hàng, Doanh Nghiệp & Vé Ăn (SCR 101 – SCR 110)
| Screen ID | Screen Name & Business Function | UI Component & URL Route | Implementation Status |
|---|---|---|---|
| **SCR-101** | Đăng ký, Đăng nhập & Google OAuth | `LoginPage.jsx`, `RegisterPage.jsx` (`/login`, `/register`) | ✅ Completed & Verified |
| **SCR-102** | Hồ sơ Cá nhân & Doanh nghiệp CTP | `ProfilePage.jsx?tab=ctp` (`/profile?tab=ctp`) | ✅ Completed & Verified |
| **SCR-103** | Quản lý Danh sách Đoàn & Excel Import | `HotelDetailPage.jsx` ➔ Tab `📋 DANH SÁCH THÀNH VIÊN` | ✅ Completed & Verified |
| **SCR-104** | Khách hàng Thân thiết & Hạng Thẻ Loyalty | `ProfilePage.jsx?tab=loyalty` (`/profile?tab=loyalty`) | ✅ Completed & Verified |
| **SCR-105** | Ví Điện Tử Cá Nhân & Ví Đoàn | `ProfilePage.jsx?tab=wallet` (`/profile?tab=wallet`) | ✅ Completed & Verified |
| **SCR-106** | Nạp tiền Ví & Hạn mức Chi tiêu Ngày | `ProfilePage.jsx?tab=wallet` ➔ Top-up & Limit modal | ✅ Completed & Verified |
| **SCR-107** | Kho Vé ăn & Mã QR Code Suất ăn | `ProfilePage.jsx?tab=mealtickets` (`/profile?tab=mealtickets`) | ✅ Completed & Verified |
| **SCR-108** | Yêu cầu Hủy đơn & Dự toán Hoàn tiền | `ProfilePage.jsx?tab=bookings` ➔ Cancellation Refund Engine | ✅ Completed & Verified |
| **SCR-109** | Kho Voucher & Đổi điểm Thưởng | `ProfilePage.jsx?tab=vouchers` (`/profile?tab=vouchers`) | ✅ Completed & Verified |
| **SCR-110** | Admin View Quản lý Người dùng & Đoàn | `AdminDashboardPage.jsx?tab=users` (`/admin/users?tab=users`) | ✅ Completed & Verified |

---

### 2. Phân Hệ 2: Vận Hành Khách Sạn, Ma Trận Phòng & Nhà Hàng (SCR 201 – SCR 210)
| Screen ID | Screen Name & Business Function | UI Component & URL Route | Implementation Status |
|---|---|---|---|
| **SCR-201** | Dashboard Tổng quan Đối tác Khách sạn | `AdminDashboardPage.jsx?tab=hotels` | ✅ Completed & Verified |
| **SCR-202** | Khai báo Khách sạn & Khu vực Nhà hàng | `AdminDashboardPage.jsx?tab=hotels` ➔ Modal Create Hotel | ✅ Completed & Verified |
| **SCR-203** | Quản lý Loại phòng & Quỹ phòng Đoàn | `AdminDashboardPage.jsx?tab=rooms` | ✅ Completed & Verified |
| **SCR-204** | Sơ đồ Phòng Real-time (Room Matrix) | `StaffRoomPage.jsx` (`/staff/rooms`) | ✅ Completed & Verified |
| **SCR-205** | Phân bổ Phòng Hàng loạt cho Đoàn | `HotelDetailPage.jsx` ➔ Form Gán phòng liền kề | ✅ Completed & Verified |
| **SCR-206** | Quản lý Menu Nhà hàng & Gói Vé ăn | `HotelDetailPage.jsx?tab=meal` | ✅ Completed & Verified |
| **SCR-207** | Quét Mã QR Vé ăn tại Nhà hàng | `StaffRoomPage.jsx` ➔ Button `📷 Quét Mã QR Vé Ăn` | ✅ Completed & Verified |
| **SCR-208** | Cấu hình Giá phòng & Chiết khấu Đoàn | `AdminDashboardPage.jsx?tab=rooms` ➔ Price & Discount inputs | ✅ Completed & Verified |
| **SCR-209** | Duyệt Yêu cầu Hủy phòng & Hoàn tiền | `AdminDashboardPage.jsx?tab=bookings` | ✅ Completed & Verified |
| **SCR-210** | Admin View Duyệt Khách sạn mới | `AdminDashboardPage.jsx?tab=hotels` ➔ Status toggle | ✅ Completed & Verified |

---

### 3. Phân Hệ 3: Đặt Phòng Đoàn, Vé Ăn & Nghiệp Vụ Lễ Tân (SCR 301 – SCR 310)
| Screen ID | Screen Name & Business Function | UI Component & URL Route | Implementation Status |
|---|---|---|---|
| **SCR-301** | Tìm kiếm Khách sạn Lẻ & Đoàn | `HotelsPage.jsx` (`/`) ➔ Hero Search Tabs | ✅ Completed & Verified |
| **SCR-302** | Kết quả Tìm kiếm & Bộ lọc Combo | `HotelsPage.jsx` ➔ Grid Filter Catalog | ✅ Completed & Verified |
| **SCR-303** | Chi tiết Khách sạn & Menu Nhà hàng | `HotelDetailPage.jsx` (`/hotels/:id`) | ✅ Completed & Verified |
| **SCR-304** | Wizard Đặt phòng Đoàn (Group Booking) | `HotelDetailPage.jsx?tab=group` ➔ 25% Discount Calc | ✅ Completed & Verified |
| **SCR-305** | Đặt bàn & Mua Vé ăn Riêng lẻ | `HotelDetailPage.jsx?tab=meal` | ✅ Completed & Verified |
| **SCR-306** | Nhập Danh sách Khách Đoàn & Phân công | `HotelDetailPage.jsx` ➔ Group Member Manifest Modal | ✅ Completed & Verified |
| **SCR-307** | Chi tiết Đơn Đặt đoàn & Mã QR Tổng | `HotelDetailPage.jsx` ➔ Checkout Booking Modal | ✅ Completed & Verified |
| **SCR-308** | Lễ tân Quản lý & Tiếp nhận Đơn Đoàn | `StaffRoomPage.jsx` (Role RECEPTIONIST) | ✅ Completed & Verified |
| **SCR-309** | Lễ tân Check-in Đoàn Cấp tốc | `StaffRoomPage.jsx` ➔ Button `🚀 Check-in Cấp Tốc` | ✅ Completed & Verified |
| **SCR-310** | Lễ tân Check-out Đoàn & Phụ thu | `StaffRoomPage.jsx` ➔ Occupancy actions | ✅ Completed & Verified |

---

### 4. Phân Hệ 4: Thanh Toán Cọc Đoàn, Engine Hoàn Tiền & Hóa Đơn VAT (SCR 401 – SCR 410)
| Screen ID | Screen Name & Business Function | UI Component & URL Route | Implementation Status |
|---|---|---|---|
| **SCR-401** | Chọn Phương thức Thanh toán Combo | `HotelDetailPage.jsx` ➔ Checkout Modal (Online, Cash, Bank) | ✅ Completed & Verified |
| **SCR-402** | Thanh toán Đặt cọc Đoàn (30% Deposit) | `HotelDetailPage.jsx?tab=group` ➔ Deposit Calculation | ✅ Completed & Verified |
| **SCR-403** | Kết quả Thanh toán & Biên lai Điện tử | `PaymentStatusPage.jsx` (`/payment/success`, `/payment/cancel`) | ✅ Completed & Verified |
| **SCR-404** | Engine & Màn hình Hoàn tiền Tự động | `ProfilePage.jsx?tab=bookings` ➔ Refund Engine | ✅ Completed & Verified |
| **SCR-405** | Xử lý Hoàn tiền Vé ăn Thừa | `ProfilePage.jsx?tab=bookings` | ✅ Completed & Verified |
| **SCR-406** | Quản lý Mã Giảm giá Combo | `AdminDashboardPage.jsx?tab=vouchers` | ✅ Completed & Verified |
| **SCR-407** | Tra cứu Dòng tiền Cọc & Lịch sử Giao dịch | `ProfilePage.jsx?tab=wallet` | ✅ Completed & Verified |
| **SCR-408** | Xuất Hóa đơn Red VAT Doanh nghiệp | `ProfilePage.jsx?tab=ctp` & `AdminDashboardPage.jsx?tab=ctp` | ✅ Completed & Verified |
| **SCR-409** | Đối soát Doanh thu Phòng & Nhà hàng | `AdminDashboardPage.jsx?tab=reports` | ✅ Completed & Verified |
| **SCR-410** | Admin Payout & Duyệt lệnh Hoàn tiền | `AdminDashboardPage.jsx?tab=bookings` | ✅ Completed & Verified |

---

### 5. Phân Hệ 5: Real-time Chat, AI Tiệc Đoàn & Báo Cáo Analytics (SCR 501 – SCR 510)
| Screen ID | Screen Name & Business Function | UI Component & URL Route | Implementation Status |
|---|---|---|---|
| **SCR-501** | Chat Real-time Khách <-> Lễ tân | `Header.jsx` & `StaffRoomPage.jsx` | ✅ Completed & Verified |
| **SCR-502** | Chat Hỗ trợ Trưởng đoàn Dedicated | `StaffRoomPage.jsx` | ✅ Completed & Verified |
| **SCR-503** | AI Chatbot Tư vấn Combo & Tiệc Đoàn | `HotelsPage.jsx` ➔ Floating Widget `🤖 AI Tư Vấn Đặt Đoàn` | ✅ Completed & Verified |
| **SCR-504** | Order Dịch vụ Tận phòng (Room Service) | `StaffRoomPage.jsx` | ✅ Completed & Verified |
| **SCR-505** | Quản lý Thông báo Push Notification | `Header.jsx` Notifications badge | ✅ Completed & Verified |
| **SCR-506** | Dashboard Báo cáo Doanh thu Giám đốc | `AdminDashboardPage.jsx?tab=reports` (DIRECTOR / ADMIN) | ✅ Completed & Verified |
| **SCR-507** | Báo cáo Khách đoàn vs Khách lẻ | `AdminDashboardPage.jsx?tab=reports` ➔ Occupancy breakdown | ✅ Completed & Verified |
| **SCR-508** | Báo cáo Hủy phòng & Chi phí Hoàn tiền | `AdminDashboardPage.jsx?tab=reports` ➔ Booking stats | ✅ Completed & Verified |
| **SCR-509** | Báo cáo Doanh thu Nhà hàng & Vé ăn | `AdminDashboardPage.jsx?tab=reports` ➔ Revenue Report | ✅ Completed & Verified |
| **SCR-510** | Xuất Báo cáo Excel / PDF Tổng hợp | `AdminDashboardPage.jsx?tab=reports` ➔ Button `📊 Export Report` | ✅ Completed & Verified |

---

## 🗄️ Database Migrations History (Flyway V1 - V30)

| Migration Script | Scope & Domain | Purpose / Description |
|---|---|---|
| **V1 - V12** | Core Schema | Tables for `users`, `hotels`, `rooms`, `bookings`, `booking_rooms`, `payments`, `vouchers`, `reviews`, `system_settings`, `room_locks`. |
| **V13 - V19** | Core Enhancements | Index optimizations, soft-delete triggers, pessimistic lock columns, BCrypt password hashing constraints. |
| **V20** | `corporate_tax_profiles` | Table for Corporate Tax Profile (CTP), Tax Codes (MST), VAT Invoice generation for group delegators. |
| **V21** | `group_member_manifests` | Table for storing group member lists, guest IC/passports, assigned rooms, and Excel import mapping. |
| **V22** | `refund_policies` | Schema for auto refund calculation tiers (100%, 80%, 50%, 0%) based on check-in lead time. |
| **V23** | `dynamic_lock_duration` | Configuration settings for dynamic room lock timeouts (10 to 30 mins) managed by Admins. |
| **V24** | `loyalty_membership` | Tables for Customer Loyalty Tiers (Bronze, Silver, Gold, Platinum) and reward point balances. |
| **V25 - V26** | `customer_wallets` | Schema for E-Wallet balances, top-up transaction logs, and daily spending limits. |
| **V27** | `meal_tickets` | Table for meal ticket packages (Buffet Breakfast, Seafood Dinner, Full-Board) and QR Code tokens. |
| **V28** | `qr_scan_audits` | Real-time restaurant scanner audit log table for meal ticket validations. |
| **V29** | `group_booking_deposits` | Support for 30% partial deposit payments (`DEPOSIT_30_PAID`) and group discount rates (-25%). |
| **V30** | Dual DB Migration Fixes | PostgreSQL & SQL Server syntax alignment, cross-platform compatibility for Neon Cloud DB. |

---

## ⚙️ Backend Package-by-Feature Architecture

```
src/main/java/com/hotelbooking/
├── common/             # Cross-cutting: config, exception, security, utils, validation
├── auth/               # Auth controller, JWT tokens, Login/Register DTOs
├── user/               # User management, profile update, RBAC roles
├── hotel/              # Hotel profiles, locations, search & filters, review moderation
├── room/               # Room management, live matrix status, RoomLock & RoomLockCleanupScheduler
├── booking/            # Booking lifecycle, group booking calculator (-25%), 30% deposit, date validator
├── payment/            # Payment gateways (Stripe/VNPay), IPN Webhooks, E-Wallet integration
├── voucher/            # Promotional campaign vouchers, discount validation
├── report/             # Operations & revenue statistics, Excel report exporter
├── customerportal/     # Group member manifest, CTP tax profiles, Meal ticket QR codes
└── setting/            # System settings controller & dynamic lock duration configuration
```

---

## 🧪 Testing & Verification Matrix

- **Unit & Integration Tests**: 207 passed, 0 failures, 0 errors.
- **Execution Environment**:
  ```bash
  cmd /c "set JAVA_HOME=C:\Program Files\Java\jdk-18.0.2.1&& set PATH=C:\Program Files\Java\jdk-18.0.2.1\bin;%PATH%&& mvn test"
  ```
- **Frontend Build Verification**:
  ```bash
  cd frontend && npm run build
  # Output: dist/assets/index-Dv_nfrdd.js 496.46 kB (built in 1.79s)
  ```
- **Docker Compose Full-Stack Execution**:
  ```bash
  docker-compose up --build -d
  ```

---

## 🚢 Git & Deployment Status

- **Repository**: `https://github.com/Minmin1426/hotel-booking-system.git`
- **Branch**: `main`
- **Latest Commit**: `ff32fcb` (`docs(specs): update 003-booking-management specifications with full details, group bookings, deposit, CTP, and 50-screen alignment`)
- **Status**: Clean workspace, synced with remote.
