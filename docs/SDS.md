# FPT UNIVERSITY - SOFTWARE DESIGN SPECIFICATION

**Document Title**: Software Design Specification (SDS)  
**System Name**: Hotel Booking System (Standard & Group Booking Engine)  
**Project Code**: SWP391 - Hotel Booking System Architecture  
**Version**: 1.1  
**Location & Date**: Hanoi, July 2026  

---

## Table of Contents
- [I. Record of Changes](#i-record-of-changes)
- [II. Software Design Document](#ii-software-design-document)
  - [1. High Level Design](#1-high-level-design)
    - [1.1 Software Architecture](#11-software-architecture)
      - [1.1.1 Architecture Component Responsibility & Communication Table](#111-architecture-component-responsibility--communication-table)
      - [1.1.2 Architecture Rules](#112-architecture-rules)
    - [1.2 Package Diagram](#12-package-diagram)
      - [1.2.1 Backend Package Responsibility & Dependency Table](#121-backend-package-responsibility--dependency-table)
      - [1.2.2 Naming Conventions](#122-naming-conventions)
    - [1.3 Database Design](#13-database-design)
      - [1.3.1 Entity / Table Source Priority Rules](#131-entity--table-source-priority-rules)
      - [1.3.2 Complete Database Object Dictionary](#132-complete-database-object-dictionary)
      - [1.3.3 Table Definition: `users`](#133-table-definition-users)
      - [1.3.4 Table Definition: `roles`](#134-table-definition-roles)
      - [1.3.5 Table Definition: `user_roles`](#135-table-definition-user_roles)
      - [1.3.6 Table Definition: `user_profiles`](#136-table-definition-user_profiles)
      - [1.3.7 Table Definition: `hotels`](#137-table-definition-hotels)
      - [1.3.8 Table Definition: `room_types`](#138-table-definition-room_types)
      - [1.3.9 Table Definition: `rooms`](#139-table-definition-rooms)
      - [1.3.10 Table Definition: `hotel_images`](#1310-table-definition-hotel_images)
      - [1.3.11 Table Definition: `reviews`](#1311-table-definition-reviews)
      - [1.3.12 Table Definition: `bookings`](#1312-table-definition-bookings)
      - [1.3.13 Table Definition: `booking_items`](#1313-table-definition-booking_items)
      - [1.3.14 Table Definition: `booking_guests`](#1314-table-definition-booking_guests)
      - [1.3.15 Table Definition: `payments`](#1315-table-definition-payments)
      - [1.3.16 Table Definition: `wallets`](#1316-table-definition-wallets)
      - [1.3.17 Table Definition: `wallet_transactions`](#1317-table-definition-wallet_transactions)
      - [1.3.18 Table Definition: `refund_requests`](#1318-table-definition-refund_requests)
      - [1.3.19 Table Definition: `vouchers`](#1319-table-definition-vouchers)
      - [1.3.20 Table Definition: `voucher_usages`](#1320-table-definition-voucher_usages)
      - [1.3.21 Table Definition: `meal_tickets`](#1321-table-definition-meal_tickets)
      - [1.3.22 Table Definition: `audit_logs`](#1322-table-definition-audit_logs)
      - [1.3.23 Table Definition: `system_settings`](#1323-table-definition-system_settings)
      - [1.3.61 Database Reporting Views](#1361-database-reporting-views)
      - [1.3.64 Current-Main Schema Reconciliation Table](#1364-current-main-schema-reconciliation-table)
  - [2. State Transition Diagrams](#2-state-transition-diagrams)
    - [2.1 Booking Lifecycle State Diagram](#21-booking-lifecycle-state-diagram)
    - [2.2 Room Lock Lifecycle State Diagram](#22-room-lock-lifecycle-state-diagram)
    - [2.3 Payment Lifecycle State Diagram](#23-payment-lifecycle-state-diagram)
    - [2.4 Refund Request Lifecycle State Diagram](#24-refund-request-lifecycle-state-diagram)
    - [2.5 Voucher Lifecycle State Diagram](#25-voucher-lifecycle-state-diagram)
    - [2.6 Meal Ticket Lifecycle State Diagram](#26-meal-ticket-lifecycle-state-diagram)
    - [2.7 Wallet Transaction Lifecycle State Diagram](#27-wallet-transaction-lifecycle-state-diagram)
    - [2.8 Room Physical State Lifecycle State Diagram](#28-room-physical-state-lifecycle-state-diagram)
    - [2.9 Review Moderation State Diagram](#29-review-moderation-state-diagram)
    - [2.10 Audit Log Append State Diagram](#210-audit-log-append-state-diagram)
  - [3. Detailed Design](#3-detailed-design)
    - [3.1 Authentication, RBAC & Security Subsystem](#31-authentication-rbac--security-subsystem)
    - [3.2 Inbound Booking & Room Lock Subsystem](#32-inbound-booking--room-lock-subsystem)
    - [3.3 Group Booking & CTP Manifest Subsystem](#33-group-booking--ctp-manifest-subsystem)
    - [3.4 Payment Engine & E-Wallet Webhooks Subsystem](#34-payment-engine--e-wallet-webhooks-subsystem)
    - [3.5 QR Meal Ticket & Hotel Operations Subsystem](#35-qr-meal-ticket--hotel-operations-subsystem)
    - [3.6 Financial Refund Policy Engine Subsystem](#36-financial-refund-policy-engine-subsystem)
    - [3.7 Director Analytics & Audit Monitoring Subsystem](#37-director-analytics--audit-monitoring-subsystem)

---

# I. Record of Changes

| Version | Date | A/M/D | In charge | Change Description |
|---|---|---|---|---|
| V0.1 | 15/05/2026 | A | Lead Architect | Initial high-level architecture and package structure design. |
| V0.6 | 10/06/2026 | M | Database Architect | Complete database object dictionary (Tables V1 - V33) & views. |
| V1.0 | 05/07/2026 | A | Backend Lead | State machines, sequence diagrams, and class layer responsibility tables. |
| V1.1 | 26/07/2026 | M | System Architect | Finalized 8-column table breakdowns, API mappings, and schema reconciliation. |

*Flags: **A** = Added, **M** = Modified, **D** = Deleted*

---

# II. Software Design Document

## 1. High Level Design

### 1.1 Software Architecture

`Figure SDS-ARCH-01. Multi-tier Software Architecture Overview.`

#### 1.1.1 Architecture Component Responsibility & Communication Table

| Component Name | Layer | Primary Responsibility | Communication Protocol |
|---|---|---|---|
| **Presentation SPA** | Frontend | React 18 + Vite 8 Single Page Application rendering 50 UI screens. | HTTPS / REST JSON |
| **API Gateway / Security**| Gateway | Spring Security filter chain enforcing stateless JWT authentication & RBAC. | In-memory / HTTP Interceptor |
| **Auth Service** | Application| User authentication, password BCrypt hashing, JWT issuance & OAuth2. | Spring Service Calls |
| **User Service** | Application| Profile management, corporate CTP credentials, loyalty point calculation. | JPA / DB Services |
| **Hotel Service** | Application| Master hotel data catalog, image upload management, review moderation. | JPA / File Storage |
| **Room Service & Lock Engine**| Application| Room inventory management, pessimistic date locks for 600s checkout hold. | Spring Service / DB Lock |
| **Booking Service** | Application| Reservation orchestration, 25% group discount calculation, CTP manifest import. | Transactional Service |
| **Payment & Webhook Engine**| Integration | Payment gateway integrations (VNPAY/Stripe), IPN webhook verification, Wallet ledger.| HTTPS REST / Webhook |
| **Voucher & Meal Ticket Engine**| Application| Promotion voucher validation, QR code meal ticket generator & scanner parser. | Spring Service / QR Parser |
| **Reporting & Analytics** | Application| Financial aggregation, fill-rate calculations, Excel `.xlsx` report generator. | Database Views / Apache POI |
| **Data Access Repository Layer**| Data | Spring Data JPA repositories interfacing relational database schemas. | JDBC / ORM SQL |

#### 1.1.2 Architecture Rules
1. **Layer Encapsulation**: Controllers must delegate business logic to Services. Entities must never be exposed directly in REST controllers; DTOs are mandatory.
2. **Stateless Security**: Authentication relies strictly on stateless JWT Bearer Tokens. Sessions are not persisted on backend nodes.
3. **Database DDL**: Application runtime user has DML privileges only (SELECT, INSERT, UPDATE, DELETE). All DDL alterations run via Flyway scripts.

---

### 1.2 Package Diagram

`Figure SDS-PKG-01. Backend Package Dependency Diagram.`

#### 1.2.1 Backend Package Responsibility & Dependency Table

| Package Name | Layer / Scope | Primary Responsibility | Dependencies |
|---|---|---|---|
| `com.hotelbooking.auth` | Security | Authentication, login controllers, JWT token provider. | `user`, `common` |
| `com.hotelbooking.user` | Feature | User entities, profiles, loyalty membership logic. | `common` |
| `com.hotelbooking.hotel` | Feature | Hotel catalog, images, customer reviews moderation. | `common` |
| `com.hotelbooking.room` | Feature | Room types, room inventory, temporary date locking. | `hotel`, `common` |
| `com.hotelbooking.booking` | Feature | Core booking engine, group booking wizard, CTP manifest. | `room`, `user`, `common` |
| `com.hotelbooking.payment` | Feature | VNPAY/Stripe integrations, IPN webhooks, e-wallet. | `booking`, `common` |
| `com.hotelbooking.voucher` | Feature | Discount vouchers storefront and redemption rules. | `booking`, `common` |
| `com.hotelbooking.mealticket`| Feature | Dynamic QR meal tickets generation and scanner parsing. | `booking`, `common` |
| `com.hotelbooking.report` | Feature | Financial analytics, occupancy views, Excel export. | `booking`, `hotel`, `common` |
| `com.hotelbooking.operations`| Operations | Real-time Room Matrix grid & Rapid check-in/out logic. | `room`, `booking`, `common` |
| `com.hotelbooking.notification`| Integration | Async email dispatch and push notification badge updates.| `common` |
| `com.hotelbooking.common` | Infrastructure| Cross-cutting config, global exception handler, security utils.| None |

#### 1.2.2 Naming Conventions

| Context | Convention | Example |
|---|---|---|
| **Java Package** | lowercase dotted | `com.hotelbooking.booking.service` |
| **Java Class** | PascalCase | `BookingService` |
| **REST Resource** | kebab-case under `/api/v1` | `/api/v1/group-bookings` |
| **Database Table** | snake_case, plural | `booking_items` |
| **Database Column**| snake_case | `check_in_date` |
| **React Component**| PascalCase | `GroupBookingModal` |

---

### 1.3 Database Design

#### 1.3.1 Entity / Table Source Priority Rules
1. **Flyway Migration Scripts** (`src/main/resources/db/migration/postgresql/*.sql`)
2. **JPA Entity Definitions** (`com.hotelbooking.*.entity.*`)

#### 1.3.2 Complete Database Object Dictionary

| No | Table Name | JPA Entity Class | Domain / Purpose |
|---|---|---|---|
| 1 | `users` | `User` | User identity & authentication baseline |
| 2 | `roles` | `Role` | Security roles (`ROLE_CUSTOMER`, `ROLE_ADMIN`, etc.) |
| 3 | `user_roles` | Join Table | User-to-Role many-to-many relationship |
| 4 | `user_profiles` | `UserProfile` | Extended customer profile & loyalty points |
| 5 | `hotels` | `Hotel` | Hotel property metadata |
| 6 | `room_types` | `RoomType` | Room category pricing & capacity specifications |
| 7 | `rooms` | `Room` | Physical room instances & live occupancy state |
| 8 | `hotel_images` | `HotelImage` | Verified hotel gallery images |
| 9 | `reviews` | `Review` | Customer reviews & moderation status |
| 10 | `bookings` | `Booking` | Reservation transaction header |
| 11 | `booking_items` | `BookingItem` | Line items detailing booked rooms and pricing |
| 12 | `booking_guests` | `BookingGuest` | Group member manifest list |
| 13 | `payments` | `Payment` | Payment transactions (VNPAY/Stripe/Wallet) |
| 14 | `wallets` | `Wallet` | Customer digital e-wallet balance |
| 15 | `wallet_transactions` | `WalletTransaction` | Immutable debit/credit ledger |
| 16 | `refund_requests` | `RefundRequest` | Cancellation refund requests |
| 17 | `vouchers` | `Voucher` | Discount voucher definitions |
| 18 | `voucher_usages` | `VoucherUsage` | Customer voucher redemption tracking |
| 19 | `meal_tickets` | `MealTicket` | QR code meal vouchers for hotel dining |
| 20 | `audit_logs` | `AuditLog` | System audit trail records |
| 21 | `system_settings` | `SystemSetting` | Global system configurations |

---

> [!IMPORTANT]
> Tất cả các bảng dữ liệu đều được quy định theo **Bảng 8 cột chuẩn** (No, Field, Java/DB Type, PK, FK, UN, NN, Description) với cờ viết tắt 1 ký tự (`PK`, `FK`, `UN`, `NN`).

#### 1.3.3 Table Definition: `users`
JPA Class: `com.hotelbooking.user.entity.User`. Purpose: User credentials and security status.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key (Auto-increment) |
| 2 | `email` | `String / VARCHAR(100)` | | | ✓ | ✓ | Unique user email address |
| 3 | `password` | `String / VARCHAR(255)` | | | | ✓ | BCrypt password hash |
| 4 | `full_name` | `String / VARCHAR(100)` | | | | ✓ | User full name |
| 5 | `phone` | `String / VARCHAR(20)` | | | | | Contact telephone number |
| 6 | `status` | `String / VARCHAR(20)` | | | | ✓ | Account status (`ACTIVE`, `LOCKED`) |
| 7 | `created_at` | `LocalDateTime / TIMESTAMP`| | | | ✓ | Record creation timestamp |

#### 1.3.4 Table Definition: `roles`
JPA Class: `com.hotelbooking.user.entity.Role`. Purpose: Security roles definition.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `name` | `String / VARCHAR(50)` | | | ✓ | ✓ | Role name (`ROLE_CUSTOMER`, `ROLE_ADMIN`) |

#### 1.3.5 Table Definition: `user_roles`
JPA Class: Join Table. Purpose: User to role mapping.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `user_id` | `Long / BIGINT` | ✓ | ✓ | | ✓ | Foreign Key referencing `users(id)` |
| 2 | `role_id` | `Long / BIGINT` | ✓ | ✓ | | ✓ | Foreign Key referencing `roles(id)` |

#### 1.3.6 Table Definition: `user_profiles`
JPA Class: `com.hotelbooking.user.entity.UserProfile`. Purpose: Extended customer data & loyalty.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `user_id` | `Long / BIGINT` | | ✓ | ✓ | ✓ | Foreign Key referencing `users(id)` |
| 3 | `loyalty_points`| `Integer / INT` | | | | ✓ | Accumulated loyalty points |
| 4 | `membership_tier`|`String / VARCHAR(20)`| | | | ✓ | Tier (`SILVER`, `GOLD`, `PLATINUM`) |
| 5 | `tax_code` | `String / VARCHAR(50)` | | | | | Corporate CTP tax ID |

#### 1.3.7 Table Definition: `hotels`
JPA Class: `com.hotelbooking.hotel.entity.Hotel`. Purpose: Hotel property metadata.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `name` | `String / VARCHAR(150)`| | | | ✓ | Registered hotel name |
| 3 | `address` | `String / VARCHAR(255)`| | | | ✓ | Physical street address |
| 4 | `city` | `String / VARCHAR(100)`| | | | ✓ | City location |
| 5 | `rating` | `Double / DECIMAL(2,1)`| | | | ✓ | Rating score (1.0 - 5.0) |
| 6 | `status` | `String / VARCHAR(20)` | | | | ✓ | Status (`ACTIVE`, `DISABLED`) |

#### 1.3.8 Table Definition: `room_types`
JPA Class: `com.hotelbooking.room.entity.RoomType`. Purpose: Room category pricing & capacity.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `hotel_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `hotels(id)` |
| 3 | `name` | `String / VARCHAR(100)`| | | | ✓ | Type name (Deluxe, Suite) |
| 4 | `base_price` | `BigDecimal / DECIMAL(12,2)`| | | | ✓ | Standard price per night |
| 5 | `max_occupancy`|`Integer / INT` | | | | ✓ | Maximum person capacity |

#### 1.3.9 Table Definition: `rooms`
JPA Class: `com.hotelbooking.room.entity.Room`. Purpose: Physical room instances.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `hotel_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `hotels(id)` |
| 3 | `room_type_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `room_types(id)` |
| 4 | `room_number` | `String / VARCHAR(20)` | | | | ✓ | Room door number (e.g. 101) |
| 5 | `status` | `String / VARCHAR(20)` | | | | ✓ | State (`VACANT`, `OCCUPIED`, `DIRTY`)|

#### 1.3.10 Table Definition: `hotel_images`
JPA Class: `com.hotelbooking.hotel.entity.HotelImage`. Purpose: Gallery photo storage.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `hotel_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `hotels(id)` |
| 3 | `image_url` | `String / VARCHAR(500)`| | | | ✓ | Absolute file / Cloud URL |

#### 1.3.11 Table Definition: `reviews`
JPA Class: `com.hotelbooking.hotel.entity.Review`. Purpose: Customer feedback.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `user_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `users(id)` |
| 3 | `hotel_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `hotels(id)` |
| 4 | `rating` | `Integer / INT` | | | | ✓ | Rating score (1 - 5) |
| 5 | `comment` | `String / TEXT` | | | | | Customer review text |
| 6 | `status` | `String / VARCHAR(20)` | | | | ✓ | Moderation (`PENDING`, `APPROVED`)|

#### 1.3.12 Table Definition: `bookings`
JPA Class: `com.hotelbooking.booking.entity.Booking`. Purpose: Core reservation header.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `booking_code` | `String / VARCHAR(50)` | | | ✓ | ✓ | Unique business identifier |
| 3 | `user_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `users(id)` |
| 4 | `hotel_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `hotels(id)` |
| 5 | `check_in` | `LocalDate / DATE` | | | | ✓ | Check-in date |
| 6 | `check_out` | `LocalDate / DATE` | | | | ✓ | Check-out date |
| 7 | `total_amount` | `BigDecimal / DECIMAL(12,2)`| | | | ✓ | Final total booking price |
| 8 | `status` | `String / VARCHAR(30)` | | | | ✓ | Lifecycle state (`PENDING_PAYMENT`, `CONFIRMED`, `CANCELLED`) |
| 9 | `version` | `Long / BIGINT` | | | | ✓ | Optimistic lock `@Version` field |

#### 1.3.13 Table Definition: `booking_items`
JPA Class: `com.hotelbooking.booking.entity.BookingItem`. Purpose: Room line items.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `booking_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `bookings(id)` |
| 3 | `room_type_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `room_types(id)` |
| 4 | `quantity` | `Integer / INT` | | | | ✓ | Number of rooms booked |
| 5 | `price_per_night`|`BigDecimal / DECIMAL(12,2)`| | | | ✓ | Agreed room rate |

#### 1.3.14 Table Definition: `booking_guests`
JPA Class: `com.hotelbooking.booking.entity.BookingGuest`. Purpose: Group manifest members.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `booking_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `bookings(id)` |
| 3 | `guest_name` | `String / VARCHAR(100)`| | | | ✓ | Full name of group member |
| 4 | `id_card` | `String / VARCHAR(50)` | | | | | Passport or ID card number |

#### 1.3.15 Table Definition: `payments`
JPA Class: `com.hotelbooking.payment.entity.Payment`. Purpose: Financial payment records.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `booking_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `bookings(id)` |
| 3 | `payment_method`|`String / VARCHAR(30)` | | | | ✓ | Method (`VNPAY`, `STRIPE`, `WALLET`) |
| 4 | `transaction_id`|`String / VARCHAR(100)`| | | ✓ | ✓ | External Gateway reference ID |
| 5 | `amount` | `BigDecimal / DECIMAL(12,2)`| | | | ✓ | Paid monetary amount |
| 6 | `status` | `String / VARCHAR(20)` | | | | ✓ | Payment status (`SUCCESS`, `FAILED`) |

#### 1.3.16 Table Definition: `wallets`
JPA Class: `com.hotelbooking.wallet.entity.Wallet`. Purpose: Customer e-wallet account.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `user_id` | `Long / BIGINT` | | ✓ | ✓ | ✓ | Foreign Key referencing `users(id)` |
| 3 | `balance` | `BigDecimal / DECIMAL(12,2)`| | | | ✓ | Available cash balance |
| 4 | `daily_limit` | `BigDecimal / DECIMAL(12,2)`| | | | ✓ | User-configured spending threshold |

#### 1.3.17 Table Definition: `wallet_transactions`
JPA Class: `com.hotelbooking.wallet.entity.WalletTransaction`. Purpose: Immutable ledger.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `wallet_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `wallets(id)` |
| 3 | `type` | `String / VARCHAR(20)` | | | | ✓ | Transaction type (`TOPUP`, `DEBIT`, `REFUND`) |
| 4 | `amount` | `BigDecimal / DECIMAL(12,2)`| | | | ✓ | Transaction monetary amount |

#### 1.3.18 Table Definition: `refund_requests`
JPA Class: `com.hotelbooking.payment.entity.RefundRequest`. Purpose: Refund management.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `booking_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `bookings(id)` |
| 3 | `user_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `users(id)` |
| 4 | `refund_amount` |`BigDecimal / DECIMAL(12,2)`| | | | ✓ | Computed refund amount |
| 5 | `status` | `String / VARCHAR(20)` | | | | ✓ | Status (`PENDING`, `PROCESSED`)|

#### 1.3.19 Table Definition: `vouchers`
JPA Class: `com.hotelbooking.voucher.entity.Voucher`. Purpose: Promotion vouchers.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `code` | `String / VARCHAR(50)` | | | ✓ | ✓ | Unique voucher promo code |
| 3 | `discount_type` |`String / VARCHAR(20)` | | | | ✓ | Type (`PERCENTAGE`, `FLAT`) |
| 4 | `discount_val` |`BigDecimal / DECIMAL(12,2)`| | | | ✓ | Discount magnitude |

#### 1.3.20 Table Definition: `voucher_usages`
JPA Class: `com.hotelbooking.voucher.entity.VoucherUsage`. Purpose: Redemption tracking.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `voucher_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `vouchers(id)` |
| 3 | `user_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `users(id)` |

#### 1.3.21 Table Definition: `meal_tickets`
JPA Class: `com.hotelbooking.mealticket.entity.MealTicket`. Purpose: Dynamic QR dining ticket.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `booking_id` | `Long / BIGINT` | | ✓ | | ✓ | Foreign Key referencing `bookings(id)` |
| 3 | `qr_code_hash` |`String / VARCHAR(255)`| | | ✓ | ✓ | Encrypted QR verification token |
| 4 | `status` | `String / VARCHAR(20)` | | | | ✓ | Status (`AVAILABLE`, `USED`, `EXPIRED`) |

#### 1.3.22 Table Definition: `audit_logs`
JPA Class: `com.hotelbooking.common.entity.AuditLog`. Purpose: Immutable audit log.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `actor_email` | `String / VARCHAR(100)`| | | | ✓ | User email performing operation |
| 3 | `action` | `String / VARCHAR(100)`| | | | ✓ | Executed action string |
| 4 | `created_at` | `LocalDateTime / TIMESTAMP`| | | | ✓ | Timestamp of audit entry |

#### 1.3.23 Table Definition: `system_settings`
JPA Class: `com.hotelbooking.setting.entity.SystemSetting`. Purpose: System configuration.

| No | Field | Java / DB Type | PK | FK | UN | NN | Description |
|---|---|---|---|---|---|---|---|
| 1 | `id` | `Long / BIGINT` | ✓ | | | ✓ | Primary Key |
| 2 | `setting_key` | `String / VARCHAR(100)`| | | ✓ | ✓ | Config key |
| 3 | `setting_val` | `String / TEXT` | | | | ✓ | Config value |

---

#### 1.3.61 Database Reporting Views

##### `v_occupancy_summary`
```sql
CREATE VIEW v_occupancy_summary AS
SELECT h.id AS hotel_id, h.name AS hotel_name,
       COUNT(r.id) AS total_rooms,
       SUM(CASE WHEN r.status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied_rooms,
       ROUND(SUM(CASE WHEN r.status = 'OCCUPIED' THEN 1 ELSE 0 END)::decimal / COUNT(r.id) * 100, 2) AS occupancy_rate
FROM hotels h
JOIN rooms r ON h.id = r.hotel_id
GROUP BY h.id, h.name;
```

##### `v_revenue_by_hotel`
```sql
CREATE VIEW v_revenue_by_hotel AS
SELECT h.id AS hotel_id, h.name AS hotel_name,
       COUNT(b.id) AS total_bookings,
       SUM(b.total_amount) AS gross_revenue
FROM hotels h
JOIN bookings b ON h.id = b.hotel_id
WHERE b.status = 'CONFIRMED'
GROUP BY h.id, h.name;
```

##### `v_voucher_usage`
```sql
CREATE VIEW v_voucher_usage AS
SELECT v.id AS voucher_id, v.code,
       COUNT(vu.id) AS total_redeemed
FROM vouchers v
LEFT JOIN voucher_usages vu ON v.id = vu.voucher_id
GROUP BY v.id, v.code;
```

---

#### 1.3.64 Current-Main Schema Reconciliation Table

| Table Name | Action in Migrations | Known Implementation Notes |
|---|---|---|
| `meal_tickets` | Added in V27, Aligned in V33 | Uses JSON metadata column for QR payload verification. |
| `refund_requests` | Added in V22 | Linked directly to `bookings(id)` and `users(id)` for maker-checker. |
| `audit_logs` | Refactored in V16 | Append-only configuration enforced at DB trigger level. |

---

## 2. State Transition Diagrams

### 2.1 Booking Lifecycle State Diagram
`Figure SDS-STM-01. Booking Lifecycle State Diagram.`
`[DIAGRAM VERIFICATION NOTE: State machine verified against BookingStateEnum.java]`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `DRAFT` | Submit booking wizard | `PENDING_PAYMENT` | Lock room inventory for 600s (`BR-33`). |
| `PENDING_PAYMENT` | IPN payment success received | `CONFIRMED` | Dispatch e-ticket email & QR code. |
| `PENDING_PAYMENT` | Timer > 10 mins without payment | `EXPIRED` | Release room lock inventory atomically. |
| `CONFIRMED` | Guest arrives at reception | `CHECKED_IN` | Mark room status as `OCCUPIED`. |
| `CONFIRMED` | Cancel request submitted | `CANCELLED` | Calculate refund tier per `BR-14`. |
| `CHECKED_IN` | Guest completes stay | `CHECKED_OUT` | Mark room status as `DIRTY` for cleaning. |

State vocabulary verified: `PENDING_PAYMENT`, `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`, `EXPIRED`.

---

### 2.2 Room Lock Lifecycle State Diagram
`Figure SDS-STM-02. Room Lock State Diagram.`
`[DIAGRAM VERIFICATION NOTE: Lock timer verified against RoomLockEngine.java]`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `UNLOCKED` | Acquire lock request | `LOCKED_TEMPORARY` | Set lock expiration `NOW() + 600s`. |
| `LOCKED_TEMPORARY` | Payment success callback | `LOCKED_PERMANENT` | Assign room booking relation. |
| `LOCKED_TEMPORARY` | Timer expires (600s) | `UNLOCKED` | System background cron releases lock. |

State vocabulary: `UNLOCKED`, `LOCKED_TEMPORARY`, `LOCKED_PERMANENT`.

---

### 2.3 Payment Lifecycle State Diagram
`Figure SDS-STM-03. Payment State Diagram.`
`[DIAGRAM VERIFICATION NOTE: Webhook handlers verified in PaymentService.java]`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `INITIATED` | Gateway redirect success | `PENDING_IPN` | Await asynchronous gateway webhook. |
| `PENDING_IPN` | Valid IPN signature received | `SUCCESS` | Update booking status to `CONFIRMED`. |
| `PENDING_IPN` | Gateway payment failure | `FAILED` | Notify user and prompt retry. |

State vocabulary: `INITIATED`, `PENDING_IPN`, `SUCCESS`, `FAILED`.

---

### 2.4 Refund Request Lifecycle State Diagram
`Figure SDS-STM-04. Refund State Diagram.`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `SUBMITTED` | Compute refund tier (`BR-14`)| `APPROVED` | Calculate refund amount percentage. |
| `APPROVED` | Execute wallet balance credit | `PROCESSED` | Credit cash to customer wallet ledger. |

State vocabulary: `SUBMITTED`, `APPROVED`, `PROCESSED`.

---

### 2.5 Voucher Lifecycle State Diagram
`Figure SDS-STM-05. Voucher State Diagram.`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `ACTIVE` | Customer redeems promo code | `REDEEMED` | Log entry in `voucher_usages` table. |
| `ACTIVE` | Expiry date reached | `EXPIRED` | Invalidate voucher code for checkout. |

State vocabulary: `ACTIVE`, `REDEEMED`, `EXPIRED`.

---

### 2.6 Meal Ticket Lifecycle State Diagram
`Figure SDS-STM-06. Meal Ticket State Diagram.`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `AVAILABLE` | Staff scans QR code at restaurant| `USED` | Mark ticket as redeemed. |
| `AVAILABLE` | Breakfast window closes (09:30)| `EXPIRED` | Invalidate ticket for future scanning. |

State vocabulary: `AVAILABLE`, `USED`, `EXPIRED`.

---

### 2.7 Wallet Transaction Lifecycle State Diagram
`Figure SDS-STM-07. Wallet Transaction State Diagram.`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `PENDING` | Top-up payment success | `COMPLETED` | Append immutable transaction ledger. |

State vocabulary: `PENDING`, `COMPLETED`.

---

### 2.8 Room Physical State Lifecycle State Diagram
`Figure SDS-STM-08. Physical Room State Diagram.`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `VACANT` | Rapid Check-In executed | `OCCUPIED` | Assign guest to physical room door. |
| `OCCUPIED` | Check-Out executed | `DIRTY` | Flag room for housekeeper cleaning. |
| `DIRTY` | Housekeeper completes cleaning | `VACANT` | Return room to available inventory. |

State vocabulary: `VACANT`, `OCCUPIED`, `DIRTY`.

---

### 2.9 Review Moderation State Diagram
`Figure SDS-STM-09. Review Moderation State Diagram.`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `PENDING` | Admin approves review content | `APPROVED` | Publish review on public hotel page. |
| `PENDING` | Admin rejects abusive text | `REJECTED` | Hide review from public catalog. |

State vocabulary: `PENDING`, `APPROVED`, `REJECTED`.

---

### 2.10 Audit Log Append State Diagram
`Figure SDS-STM-10. Audit Log State Diagram.`

| Current State | Event / Guard | Next State | Required Action / Invariant |
|---|---|---|---|
| `RECORDED` | System attempts write operation| `APPENDED` | Write immutable entry to `audit_logs`. |

State vocabulary: `RECORDED`, `APPENDED`.

---

## 3. Detailed Design

### 3.1 Authentication, RBAC & Security Subsystem

#### 3.1.1 Class Diagram
`Figure SDS-CLS-01. Auth Subsystem Class Diagram.`

**Layer / Class Responsibility Table:**

| Layer | Class Name | Description / Responsibility |
|---|---|---|
| Controller | `AuthController` | Handles `/api/v1/auth/login`, `/register`, `/google` HTTP requests. |
| Service | `AuthService` | Executes BCrypt verification, issues JWT tokens via `JwtTokenProvider`. |
| Repository | `UserRepository` | Spring Data JPA interface for querying `users` table. |
| Entity | `User` | JPA entity mapped to `users` database table. |
| Security | `JwtAuthenticationFilter` | Intercepts HTTP requests and verifies `Authorization: Bearer <token>`. |

#### 3.1.2 Login Sequence Diagram
`Figure SDS-SEQ-01. User Authentication Sequence.`

**Step-by-Step Interaction Table:**

| Step | Participant | Interaction / Result |
|---|---|---|
| 1 | Customer → AuthController | `POST /api/v1/auth/login` with JSON payload `{email, password}` |
| 2 | AuthController → AuthService | Invokes `authService.login(requestDto)` |
| 3 | AuthService → UserRepository | Executes `userRepository.findByEmail(email)` |
| 4 | UserRepository → AuthService | Returns populated `User` entity instance |
| 5 | AuthService → JwtTokenProvider | Validates BCrypt hash; generates JWT token string |
| 6 | AuthService → AuthController | Returns `AuthResponseDto` containing Bearer Token |
| 7 | AuthController → Customer | HTTP 200 OK with JWT JSON response |

---

### 3.2 Inbound Booking & Room Lock Subsystem

#### 3.2.1 Class Diagram
`Figure SDS-CLS-02. Booking Subsystem Class Diagram.`

**Layer / Class Responsibility Table:**

| Layer | Class Name | Description / Responsibility |
|---|---|---|
| Controller | `BookingController` | Endpoints `/api/v1/bookings`, `/group-bookings`. |
| Service | `BookingService` | Core booking orchestrator, date validation, and transaction manager. |
| Service | `RoomLockService` | Redis/DB pessimistic locking mechanism for 10-minute hold. |
| Repository | `BookingRepository` | Data access for `bookings` and `booking_items`. |
| Entity | `Booking` | JPA persistent entity representing booking headers. |

#### 3.2.2 Create Booking & Room Lock Sequence Diagram
`Figure SDS-SEQ-02. Room Lock & Booking Creation Sequence.`

**Step-by-Step Interaction Table:**

| Step | Participant | Interaction / Result |
|---|---|---|
| 1 | Customer → BookingController | `POST /api/v1/bookings` with room selection & dates |
| 2 | BookingController → BookingService | Invokes `bookingService.createBooking(dto, userDetails)` |
| 3 | BookingService → RoomLockService | Calls `roomLockService.acquireLock(roomId, dates, 600s)` |
| 4 | RoomLockService → BookingService | Lock acquired successfully; returns Lock Token |
| 5 | BookingService → BookingRepository | Saves `Booking` entity (`status = PENDING_PAYMENT`) |
| 6 | BookingService → BookingController | Returns `BookingResponseDto` with `booking_code` |
| 7 | BookingController → Customer | HTTP 201 Created with payment redirect URL |

---

### 3.3 Group Booking & CTP Manifest Subsystem

#### 3.3.1 Class Diagram & Sequence
`Figure SDS-CLS-03. Group Booking Subsystem Class Diagram.`
`Figure SDS-SEQ-03. Group Booking Manifest Import Sequence.`

**Step-by-Step Interaction Table:**

| Step | Participant | Interaction / Result |
|---|---|---|
| 1 | CTP Booker → BookingController | `POST /api/v1/group-bookings` with room_qty >= 5 |
| 2 | BookingController → GroupBookingService| Computes 25% discount (`BR-02`) & 30% deposit requirement |
| 3 | CTP Booker → BookingController | `POST /api/v1/group-bookings/{id}/manifest` with Excel file |
| 4 | BookingController → ManifestService | Parses `.xlsx` rows and bulk inserts `booking_guests` |

---

### 3.4 Payment Engine & E-Wallet Webhooks Subsystem

#### 3.4.1 Class Diagram & Sequence
`Figure SDS-CLS-04. Payment & Wallet Subsystem Class Diagram.`
`Figure SDS-SEQ-04. VNPAY/Stripe IPN Webhook Verification Sequence.`

**Step-by-Step Interaction Table:**

| Step | Participant | Interaction / Result |
|---|---|---|
| 1 | Gateway → PaymentController | `POST /api/v1/payments/ipn/vnpay` with HMAC signature |
| 2 | PaymentController → PaymentService | Verifies HMAC signature & checks transaction idempotency |
| 3 | PaymentService → BookingService | Updates booking status to `CONFIRMED` |
| 4 | PaymentService → PaymentController | Returns HTTP 200 OK (`RspCode: 00`) |

---

### 3.5 QR Meal Ticket & Hotel Operations Subsystem

#### 3.5.1 Class Diagram & Sequence
`Figure SDS-CLS-05. Operations Subsystem Class Diagram.`
`Figure SDS-SEQ-05. QR Meal Ticket Scanner Validation Sequence.`

**Step-by-Step Interaction Table:**

| Step | Participant | Interaction / Result |
|---|---|---|
| 1 | Staff → OperationsController | `POST /api/v1/meal-tickets/verify` with `{qr_hash}` |
| 2 | OperationsController → MealTicketService| Validates hash, time window (06:30-09:30), and status |
| 3 | MealTicketService → OperationsController | Updates status to `USED`; returns HTTP 200 OK |

---

### 3.6 Financial Refund Policy Engine Subsystem

#### 3.6.1 Class Diagram & Sequence
`Figure SDS-CLS-06. Refund Policy Engine Class Diagram.`
`Figure SDS-SEQ-06. Automated Refund Processing Sequence.`

**Step-by-Step Interaction Table:**

| Step | Participant | Interaction / Result |
|---|---|---|
| 1 | Customer → BookingController | `POST /api/v1/bookings/{id}/cancel` |
| 2 | BookingController → RefundEngineService | Evaluates hours prior to check-in (`BR-14`) |
| 3 | RefundEngineService → WalletService | Credits calculated refund amount to customer wallet |

---

### 3.7 Director Analytics & Audit Monitoring Subsystem

#### 3.7.1 Class Diagram & Sequence
`Figure SDS-CLS-07. Analytics Subsystem Class Diagram.`
`Figure SDS-SEQ-07. Executive Financial Report Export Sequence.`

**Step-by-Step Interaction Table:**

| Step | Participant | Interaction / Result |
|---|---|---|
| 1 | Director → ReportController | `GET /api/v1/reports/revenue/export?period=MONTH` |
| 2 | ReportController → ReportService | Queries DB View `v_revenue_by_hotel` |
| 3 | ReportService → ReportController | Generates Apache POI `.xlsx` binary stream |
