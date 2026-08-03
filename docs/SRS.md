# FPT UNIVERSITY - SOFTWARE REQUIREMENTS SPECIFICATION

**Document Title**: Software Requirements Specification (SRS)  
**System Name**: Hotel Booking System (Standard & Group Booking Engine)  
**Project Code**: SWP391 - Hotel Booking System Architecture  
**Version**: 1.0  
**Location & Date**: Hanoi, July 2026  

---

## Table of Contents
- [I. Record of Changes](#i-record-of-changes)
- [II. Software Requirement Specification](#ii-software-requirement-specification)
  - [1. Introduction](#1-introduction)
    - [1.1 Purpose](#11-purpose)
    - [1.2 Scope](#12-scope)
    - [1.3 Intended Audience](#13-intended-audience)
    - [1.4 Definitions and Abbreviations](#14-definitions-and-abbreviations)
    - [1.5 Reference and Source Priority](#15-reference-and-source-priority)
  - [2. Overall Requirements](#2-overall-requirements)
    - [2.1 Context and Product Perspective](#21-context-and-product-perspective)
    - [2.2 Main Business Processes](#22-main-business-processes)
    - [2.3 User Requirements](#23-user-requirements)
      - [2.3.1 Actors Defined](#231-actors-defined)
      - [2.3.2 Use Case Catalog Summary](#232-use-case-catalog-summary)
      - [2.3.3 Use Case Diagrams](#233-use-case-diagrams)
    - [2.4 System Functionalities](#24-system-functionalities)
      - [2.4.1 Screen Flow](#241-screen-flow)
      - [2.4.2 Authorization Matrix (RBAC)](#242-authorization-matrix-rbac)
      - [2.4.3 Non-UI Functions](#243-non-ui-functions)
    - [2.5 Database Object Inventory & Logical Data Model](#25-database-object-inventory--logical-data-model)
  - [3. Use Case Specifications](#3-use-case-specifications)
    - [3.1 Authentication Features (UC-01, UC-02, UC-03, UC-04, UC-32)](#31-authentication-features)
    - [3.2 User & Profile Features (UC-05, UC-23)](#32-user--profile-features)
    - [3.3 Hotel & Catalog Features (UC-06, UC-07, UC-08, UC-18, UC-19, UC-20, UC-27, UC-31)](#33-hotel--catalog-features)
    - [3.4 Room Inventory & Lock Features (UC-09, UC-21, UC-33)](#34-room-inventory--lock-features)
    - [3.5 Booking Engine Features (UC-10, UC-11, UC-12, UC-14, UC-15, UC-16, UC-17, UC-22)](#35-booking-engine-features)
    - [3.6 Payment, Wallet & Refund Features (UC-13, UC-34)](#36-payment-wallet--refund-features)
    - [3.7 Voucher & Promotion Features (UC-35)](#37-voucher--promotion-features)
    - [3.8 Operations & Meal Ticket Features (UC-28, UC-29)](#38-operations--meal-ticket-features)
    - [3.9 Reporting & Analytics Features (UC-24, UC-25, UC-26, UC-30)](#39-reporting--analytics-features)
  - [4. Functional Requirements (Screen & UI Component Specs)](#4-functional-requirements-screen--ui-component-specs)
    - [4.1 Authentication Screens](#41-authentication-screens)
    - [4.2 System Admin — User Management Screens](#42-system-admin--user-management-screens)
    - [4.3 System Admin — System Configuration Screens](#43-system-admin--system-configuration-screens)
    - [4.4 Master Data — Hotel Management Screens](#44-master-data--hotel-management-screens)
    - [4.5 Master Data — Room & Capacity Screens](#45-master-data--room--capacity-screens)
    - [4.6 Master Data — Voucher Storefront Screens](#46-master-data--voucher-storefront-screens)
    - [4.7 Master Data — Meal Ticket & QR Screens](#47-master-data--meal-ticket--qr-screens)
    - [4.8 Inbound — Individual Booking Screens](#48-inbound--individual-booking-screens)
    - [4.9 Group Booking & CTP Manifest Screens](#49-group-booking--ctp-manifest-screens)
    - [4.10 Operations — Room Matrix & Rapid Check-In Screens](#410-operations--room-matrix--rapid-check-in-screens)
    - [4.11 Operations — E-Wallet & Transaction Ledger Screens](#411-operations--e-wallet--transaction-ledger-screens)
    - [4.12 Finance — Billing & Refund Engine Screens](#412-finance--billing--refund-engine-screens)
    - [4.13 Reports and Dashboards](#413-reports-and-dashboards)
    - [4.14 Audit Logs & Security Monitoring](#414-audit-logs--security-monitoring)
  - [5. Non-Functional Requirements](#5-non-functional-requirements)
    - [5.1 External Interfaces](#51-external-interfaces)
    - [5.2 Quality Attributes](#52-quality-attributes)
      - [5.2.1 Performance](#521-performance)
      - [5.2.2 Security](#522-security)
      - [5.2.3 Reliability](#523-reliability)
      - [5.2.4 Usability](#524-usability)
      - [5.2.5 Maintainability](#525-maintainability)
  - [6. Requirement Traceability](#6-requirement-traceability)
    - [6.1 Diagram Insertion Checklist](#61-diagram-insertion-checklist)
    - [6.2 Requirement Traceability Matrix](#62-requirement-traceability-matrix)
  - [7. Requirement Appendix](#7-requirement-appendix)
    - [7.1 Business Rules (BR-01 to BR-39)](#71-business-rules-br-01-to-br-39)
    - [7.2 System Messages (MSG-01 to MSG-25)](#72-system-messages-msg-01-to-msg-25)
    - [7.3 Other Requirements (Global Constraints)](#73-other-requirements-global-constraints)

---

# I. Record of Changes

| Version | Date | A/M/D | In charge | Change Description |
|---|---|---|---|---|
| V0.1 | 10/05/2026 | A | Development Team | Initial outline and scope definition. |
| V0.5 | 01/06/2026 | M | Business Analyst Team | Added Use Case Catalog (UC-01 to UC-35) and Actor definitions. |
| V0.8 | 20/06/2026 | M | Software Architecture Team | Added RBAC Authorization Matrix, Screen Flow, and System Messages. |
| V1.0 | 25/07/2026 | A | Lead Architect | Final Baseline release with 35 Use Cases, 50 Screen Specs, BR-01..BR-39, and Traceability Matrix. |

*Flags: **A** = Added, **M** = Modified, **D** = Deleted*

---

# II. Software Requirement Specification

## 1. Introduction

### 1.1 Purpose
This Software Requirements Specification (SRS) document details the functional, non-functional, and operational requirements for the **Hotel Booking System**. The primary objective is to define a scalable, secure, multi-tenant enterprise hotel booking platform supporting individual bookings, group reservations (corporate & travel agency), real-time room lock management, loyalty rewards, e-wallet transactions, QR-code meal ticket validation, and automated financial refund engines.

### 1.2 Scope
The system encompasses backend API services (`Spring Boot 4`, `Java 17`, `Flyway`, `PostgreSQL/SQL Server`), a rich SPA web frontend (`React 18`, `Vite 8`, `TailwindCSS`), and third-party integrations (`VNPAY`, `Stripe`, `Google OAuth 2.0`, `SMTP Email`, `Tesseract OCR`). Key administrative and operational features cover 5 primary user personas across 50 dedicated user interface screens.

### 1.3 Intended Audience
- **Software Engineers & Technical Leads**: For implementation and system verification.
- **Quality Assurance & Testers**: For writing unit, integration, and user acceptance test (UAT) suites.
- **Project Stakeholders & Academic Advisors**: For evaluating compliance against project benchmarks.

### 1.4 Definitions and Abbreviations

| Term / Abbreviation | Definition |
|---|---|
| **SRS** | Software Requirements Specification |
| **SDS** | Software Design Specification |
| **RBAC** | Role-Based Access Control |
| **CTP** | Corporate Travel Partner / Group Booking Representative |
| **Room Lock** | Temporary 10-minute pessimistic/optimistic inventory hold during checkout |
| **FIFO** | First In, First Out allocation policy for vouchers and room inventory |
| **IPN** | Instant Payment Notification (Webhook handling for VNPAY/Stripe) |

### 1.5 Reference and Source Priority
1. **JPA Persistent Entities** (`com.hotelbooking.*.entity.*`)
2. **Flyway Database Migration Scripts** (`src/main/resources/db/migration/*`)
3. **API Controllers and Service Logic** (`com.hotelbooking.*.service.*`)
4. **Project Specification Catalog** (`specs/` & `USECASE.md`)

---

## 2. Overall Requirements

### 2.1 Context and Product Perspective
The Hotel Booking System operates as a web-based multi-tier architecture communicating via stateless RESTful JSON interfaces.

```
Figure SRS-CTX-01. System Context Diagram.
+-----------------------------------------------------------------------------------+
|                                  EXTERNAL USERS                                   |
|   +---------------+   +------------------+   +-------------------+   +--------+   |
|   | Guest/Customer|   | Reception/Staff  |   | Hotel Manager/Admin|  | Director|  |
|   +-------+-------+   +--------+---------+   +---------+---------+   +----+---+   |
+-----------|--------------------|-----------------------|------------------|-------+
            |                    |                       |                  |
            +--------------------+-----------+-----------+------------------+
                                             |
                                   (HTTPS / REST APIs)
                                             v
+-----------------------------------------------------------------------------------+
|                             HOTEL BOOKING SYSTEM                                  |
|  +-------------------+  +--------------------+  +-------------------------------+ |
|  | Web SPA Frontend  |  | Spring Boot Engine |  | Database (PostgreSQL / SQL)   | |
|  | React 18 + Vite 8 | <->| Security, Auth,    | <->| JPA / Hibernate Entities      | |
|  | TailwindCSS       |  | Booking, Payment   |  | Flyway V1 - V33 Migrations    | |
|  +-------------------+  +---------+----------+  +-------------------------------+ |
+-----------------------------------|-----------------------------------------------+
                                    |
            +-----------------------+-----------------------+
            | (OAuth2)              | (IPN Webhooks)        | (SMTP)
            v                       v                       v
    +---------------+       +------------------+    +----------------+
    | Google Auth   |       | VNPAY / Stripe   |    | Mail Server    |
    +---------------+       +------------------+    +----------------+
```

### 2.2 Main Business Processes

#### 2.2.1 Inbound Receipt & Individual Booking Process
`Figure SRS-BP-01. Individual Booking Swimlane.`
- Customer searches catalog ➔ Selects dates & room type ➔ System locks room for 10 minutes (`BR-33`) ➔ Customer completes online payment via VNPAY/Stripe ➔ System auto-confirms booking (`CONFIRMED`) and issues QR ticket.

#### 2.2.2 Group Booking & CTP Manifest Process
`Figure SRS-BP-02. Group Booking Swimlane.`
- Corporate Booker (CTP) selects >= 5 rooms ➔ System calculates 25% group discount (`BR-02`) ➔ CTP uploads Excel member manifest ➔ 30% deposit payment ➔ Reservation confirmed.

#### 2.2.3 Check-In, QR Meal Ticket & Service Process
`Figure SRS-BP-03. Hotel Operations Swimlane.`
- Guest arrives ➔ Receptionist scans QR / searches booking ➔ Executes Rapid Check-In ➔ System marks room `OCCUPIED`. Guest uses QR ticket at dining hall ➔ Staff scans ➔ Ticket marked `USED`.

#### 2.2.4 Cancellation and Automatic Refund Process
`Figure SRS-BP-04. Refund Policy Engine Swimlane.`
- Customer requests cancellation ➔ System evaluates refund policy matrix (`BR-14`: >48h 100%, 24-48h 50%, <24h 0%) ➔ System refunds balance to customer wallet or source payment card.

#### 2.2.5 E-Wallet Top-up & Spending Limit Process
`Figure SRS-BP-05. Customer E-Wallet Swimlane.`
- Customer initiates top-up ➔ Gateway processes payment ➔ Balance updated ➔ Customer configures daily spending limits ➔ System validates limit on every booking transaction.

#### 2.2.6 Review Moderation & Audit Logging Process
`Figure SRS-BP-06. Audit & Moderation Swimlane.`
- User submits review ➔ System buffers for approval ➔ Admin moderates review ➔ System records immutable entry in `audit_logs`.

---

### 2.3 User Requirements

#### 2.3.1 Actors Defined

| Actor Role | System Code | Description & Responsibilities |
|---|---|---|
| **Guest / Customer** | `ROLE_CUSTOMER` | Public user who searches rooms, books reservations, tops up wallet, and redeems meal tickets. |
| **Receptionist** | `ROLE_RECEPTIONIST` | Hotel front-desk staff responsible for guest check-in, check-out, and wristband pairing. |
| **Housekeeper** | `ROLE_HOUSEKEEPER` | Staff managing room cleaning status, inventory readiness, and matrix room state updates. |
| **Staff** | `ROLE_STAFF` | General hotel operational staff assisting guests and scanning restaurant meal tickets. |
| **Hotel Admin** | `ROLE_HOTEL_ADMIN` | Manager overseeing hotel room allocations, group pricing rules, and local operations. |
| **Director** | `ROLE_DIRECTOR` | Executive authority reviewing hotel approval requests, financial payouts, and high-level reports. |
| **System Admin** | `ROLE_ADMIN` | Superuser maintaining user accounts, global system settings, audit logs, and security policies. |
| **System** | `SYS_EVENT` | Event-driven background worker (cron jobs for 10-min room lock sweeper, loyalty tier upgrades). |

#### 2.3.2 Use Cases (UC Catalog)
The system contains 35 sequential Use Cases grouped across 8 feature categories:

| UC ID | Use Case Name | Feature Group | Primary Actor & Summary |
|---|---|---|---|
| `UC-01` | Register Account | Authentication | Customer. Create new user account with BCrypt password. |
| `UC-02` | Login System | Authentication | All Roles. Authenticate via username/password and receive JWT. |
| `UC-03` | Password Reset | Authentication | All Roles. Request password reset email token. |
| `UC-04` | Search Hotels & Rooms | Hotel & Room | Customer. Filter rooms by check-in/out dates, price, and amenities. |
| `UC-05` | Lock Room | Hotel & Room | Customer. Acquire 10-min optimistic lock on selected room (`BR-33`). |
| `UC-06` | Create Reservation | Booking | Customer. Formulate booking with guest details and locked rooms. |
| `UC-07` | Import Batch Booking | Booking | Customer/Admin. Upload Excel file for group block reservation. |
| `UC-08` | Process Payment | Payment | Customer. Pay reservation via VNPay or Stripe online gateway. |
| `UC-09` | Request Refund | Payment | Customer/Admin. Submit booking refund request evaluated by policy engine. |
| `UC-10` | Top-Up Wallet | Wallet | Customer. Charge customer wallet using online payment gateway. |
| `UC-11` | Configure Spending Limit | Wallet | Customer. Set daily or per-transaction spending limit caps. |
| `UC-12` | Issue Meal Ticket | Meal Ticket | Customer/Admin. Purchase meal package tickets for resort dining. |
| `UC-13` | Link Physical Wristband | Meal Ticket | Receptionist. Pair physical wristband QR code with guest meal wallet. |
| `UC-14` | Scan Meal Ticket QR | Meal Ticket | Staff. Scan wristband or digital QR code at restaurant entry. |
| `UC-15` | Redeem Voucher | Voucher | Customer. Apply voucher discount code during checkout. |
| `UC-16` | Purchase Store Voucher | Voucher | Customer. Buy vouchers from store front using wallet balance. |
| `UC-17` | View Loyalty Status | Loyalty | Customer. Check loyalty tier (SILVER/GOLD/PLATINUM). |
| `UC-18` | Process Check-in | Operations | Receptionist. Validate booking QR code and issue room key. |
| `UC-19` | Process Check-out | Operations | Receptionist. Finalize room charges and unpair wristbands. |
| `UC-20` | Update Room Status | Operations | Housekeeper. Change room status (CLEAN, DIRTY, IN_MAINTENANCE). |
| `UC-21` | Manage Group Pricing | Operations | Hotel Admin. Define room group allotment rules and tiered pricing. |
| `UC-22` | Submit Hotel Approval | Operations | Hotel Admin. Submit hotel modifications for executive approval. |
| `UC-23` | Approve Hotel Request | Operations | Director. Review and approve pending hotel change requests. |
| `UC-24` | Manage System Users | Admin | System Admin. Create, update, block, or delete user accounts. |
| `UC-25` | View Audit Logs | Admin | System Admin. Inspect append-only audit trail of system events. |
| `UC-26` | Configure System Settings | Admin | System Admin. Manage tax rate, service fee, and JWT secrets. |
| `UC-27` | Manage Refund Policies | Admin | System Admin. Configure cancellation penalty calculation rules. |
| `UC-28` | Order Room Service | Room Service | Customer. Place food/service orders from room tablet/app. |
| `UC-29` | Get AI Travel Advice | AI Advice | Customer. Receive LLM-powered hotel and itinerary recommendations. |
| `UC-30` | View Occupancy Report | Reporting | Director/Admin. Generate room occupancy and turnover charts. |
| `UC-31` | View Financial Payouts | Reporting | Director. Review hotel revenue payouts and commission fee. |
| `UC-32` | Manage Customer Activity | Admin | System Admin. View customer activity timeline and internal notes. |
| `UC-33` | Manage Physical Wristbands | Meal Ticket | Staff. Register, deactivate, or replace lost physical wristbands. |
| `UC-34` | Manage Restaurant Areas | Operations | Hotel Admin. Define dining areas and ticket type restrictions. |
| `UC-35` | Manage Loyalty Tier Config | Loyalty | System Admin. Set point thresholds and tier discount percentages. |

#### 2.3.3 Use Case Diagrams

`Figure SRS-UC-01. Customer Use Cases Diagram.`
`Figure SRS-UC-02. Hotel Staff & Operation Use Cases Diagram.`
`Figure SRS-UC-03. System Admin & Moderation Use Cases Diagram.`
`Figure SRS-UC-04. Director Analytics & Executive Use Cases Diagram.`
`Figure SRS-UC-05. System Automated Background Use Cases Diagram.`
`Figure SRS-UC-06. Corporate Group Booking Use Cases Diagram.`

---

### 2.4 System Functionalities

#### 2.4.1 Screen Flow
- `Figure SRS-SF-01. Customer Navigation Screen Flow`: `/` (Hotels) ➔ `/hotels/:id` ➔ Modal Checkout ➔ `/payment/success` ➔ `/profile?tab=bookings`.
- `Figure SRS-SF-02. Receptionist Operational Screen Flow`: `/staff/rooms` (Room Matrix) ➔ Modal Rapid Check-In ➔ QR Scanner Modal.
- `Figure SRS-SF-03. Admin Management Screen Flow`: `/admin/dashboard` ➔ Tabs (`hotels`, `rooms`, `users`, `bookings`, `vouchers`, `reports`).
- `Figure SRS-SF-04. CTP Group Booking Screen Flow`: `/hotels/:id?tab=group` ➔ Excel Upload ➔ Deposit Checkout ➔ `/profile?tab=ctp`.
- `Figure SRS-SF-05. Director Financial Analytics Screen Flow`: `/admin/dashboard?tab=reports` ➔ Revenue Filter ➔ Export Excel.

#### 2.4.2 Authorization Matrix (RBAC)

| Feature / Action | Guest | Customer | Receptionist | Hotel Manager | Admin | Director |
|---|---|---|---|---|---|---|
| Search & View Hotels | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Book Room / Group Booking | | ✓ | | | ✓ | |
| E-Wallet Top-up & Refund | | ✓ | | | ✓ | |
| Room Matrix Operations | | | ✓ | ✓ | ✓ | |
| Check-in / Out & QR Scan | | | ✓ | ✓ | ✓ | |
| Manage Hotels & Rooms | | | | ✓ | ✓ | |
| Manage Vouchers & Pricing | | | | ✓ | ✓ | |
| User & Role Management | | | | | ✓ | |
| View Revenue & Analytics | | | | | ✓ | ✓ |

#### 2.4.3 Non-UI Functions (System Automated Functions)
1. **Cron Room Release**: Unlocks temporary 10-minute held rooms if payment is not completed.
2. **Loyalty Tier Engine**: Recalculates user membership tiers (`SILVER`, `GOLD`, `PLATINUM`) based on spending milestones.
3. **Audit Log Append**: Intercepts all administrative actions and appends immutable audit trails to `audit_logs`.

---

### 2.5 Database Object Inventory & Logical Data Model
`Figure SRS-ERD-01. Logical Data Domain Overview.`

The system maintains 22 core domain entities split across 6 logical domains:
- **Identity & User**: `users`, `roles`, `user_roles`, `user_profiles`
- **Hotel & Room**: `hotels`, `room_types`, `rooms`, `hotel_images`, `reviews`
- **Booking & Guest**: `bookings`, `booking_items`, `booking_guests`
- **Payment & Wallet**: `payments`, `wallets`, `wallet_transactions`, `refund_requests`
- **Voucher & Meal Ticket**: `vouchers`, `voucher_usages`, `meal_tickets`
- **Analytics & Audit**: `audit_logs`, `system_settings`

---

## 3. Use Case Specifications

> [!NOTE]
> Tất cả Use Case Specifications tuân thủ nghiêm ngặt **Bảng 9 thuộc tính chuẩn** theo template PDF Benchmark (Use Case ID, Primary Actor, Secondary Actor, Description, Preconditions, Trigger, Success Postcondition, Failure Postcondition, Related Rules).

### 3.1 Authentication Features

#### 3.1.1 UC-02: Login System & OAuth2

**Table 1 — Attribute Table (9 Standard Attributes):**

| Attribute | Specification |
|---|---|
| Use Case ID | `UC-02` |
| Primary Actor | Guest / Registered Customer |
| Secondary Actor | Google OAuth2 Provider |
| Description | Authenticates user using email/password or Google OAuth2 token and issues JWT token. |
| Preconditions | User account exists and status is `ACTIVE`. |
| Trigger | User submits credentials on `/login` or clicks "Login with Google". |
| Success Postcondition | JWT token generated; user redirected to dashboard with authenticated session. |
| Failure Postcondition | Access denied; error message displayed (`MSG-01`); failed attempts counter incremented. |
| Related Rules | `BR-01`, `BR-04` |

**Table 2 — Main Flow Table:**

| Step | Actor | Action |
|---|---|---|
| 1 | User | Enters email and password on `/login` screen and clicks "Sign In". |
| 2 | System | Validates input format (non-null email, min 8 chars password). |
| 3 | System | Queries `users` entity by email; checks BCrypt password hash. |
| 4 | System | Verifies account status is `ACTIVE`. |
| 5 | System | Generates JWT Bearer Token (24-hour expiration) with role claims (`ROLE_CUSTOMER`). |
| 6 | System | Returns HTTP 200 OK with JWT token payload and user profile summary. |

**Table 3 — Alternative / Exception Flows Table:**

| ID | At Step | Condition | System / User Response |
|---|---|---|---|
| `EX-AUTH-01` | Step 3 | Invalid password | Display `MSG-01` ("Invalid username or password"). Increment `failed_login_attempts`. |
| `EX-AUTH-02` | Step 4 | Account is `LOCKED` | Display `MSG-03` ("Your account has been locked due to security policy"). Terminate auth. |
| `EX-AUTH-03` | Step 1 | OAuth2 Token Failure | Display `MSG-04` ("Google OAuth authentication failed"). Prompt manual retry. |

---

### 3.2 Booking Engine Features

#### 3.2.1 UC-11: Individual Booking & Room Lock

**Table 1 — Attribute Table (9 Standard Attributes):**

| Attribute | Specification |
|---|---|
| Use Case ID | `UC-11` |
| Primary Actor | Customer |
| Secondary Actor | System Room Lock Engine |
| Description | Reserves selected room type for check-in/out dates, locking inventory for 10 minutes. |
| Preconditions | User logged in (`ROLE_CUSTOMER`); room has available capacity for dates. |
| Trigger | User selects room on `/hotels/:id` and submits checkout modal. |
| Success Postcondition | Booking created with status `PENDING_PAYMENT`; room locked for 10 minutes. |
| Failure Postcondition | Booking rejected; room capacity untouched; error notification shown. |
| Related Rules | `BR-10`, `BR-11`, `BR-33` |

**Table 2 — Main Flow Table:**

| Step | Actor | Action |
|---|---|---|
| 1 | Customer | Selects Check-in Date, Check-out Date, Room Type, and Guests. |
| 2 | System | Validates dates (`check_out > check_in` and `check_in >= today`). |
| 3 | System | Executes atomic room lock query; verifies available room count > 0. |
| 4 | System | Creates `bookings` record with `status = PENDING_PAYMENT` and `expires_at = NOW() + 10 min`. |
| 5 | System | Generates unique Booking Code (`BK-YYYYMMDD-XXXX`). |
| 6 | System | Redirects user to Checkout Payment modal (`SCR-401`). |

**Table 3 — Alternative / Exception Flows Table:**

| ID | At Step | Condition | System / User Response |
|---|---|---|---|
| `EX-BOOK-01` | Step 2 | Check-out date before Check-in | Display `MSG-05` ("Check-out date must be after check-in date"). |
| `EX-BOOK-02` | Step 3 | Room sold out for selected dates | Display `MSG-06` ("Selected room type is fully booked"). Prompt date change. |
| `EX-BOOK-03` | Step 4 | Concurrency lock conflict | Optimistic locking `@Version` exception triggered. System retries operation. |

---

### 3.3 Group Booking Features

#### 3.3.1 UC-16: Group Booking Wizard (CTP)

**Table 1 — Attribute Table (9 Standard Attributes):**

| Attribute | Specification |
|---|---|
| Use Case ID | `UC-16` |
| Primary Actor | Customer (Corporate Travel Partner / CTP) |
| Secondary Actor | System Discount Calculator |
| Description | Allows corporate bookers to reserve >= 5 rooms at a 25% discount with 30% deposit payment. |
| Preconditions | User logged in (`ROLE_CUSTOMER`); select minimum 5 rooms. |
| Trigger | User navigates to Group Booking Tab on `/hotels/:id`. |
| Success Postcondition | Group booking created with 25% discount applied; 30% deposit requirement generated. |
| Failure Postcondition | Group order rejected; error displayed (`MSG-18`). |
| Related Rules | `BR-02`, `BR-10`, `BR-11` |

**Table 2 — Main Flow Table:**

| Step | Actor | Action |
|---|---|---|
| 1 | CTP Booker | Selects hotel, dates, and specifies room quantity (>= 5). |
| 2 | System | Validates room quantity >= 5 and checks bulk capacity across hotel. |
| 3 | System | Applies automatic 25% group discount to base subtotal (`BR-02`). |
| 4 | System | Calculates required 30% deposit amount (`deposit_amount = total * 0.30`). |
| 5 | System | Creates group booking record with status `PENDING_DEPOSIT`. |
| 6 | System | Prompts user to upload guest manifest or proceed to deposit checkout. |

**Table 3 — Alternative / Exception Flows Table:**

| ID | At Step | Condition | System / User Response |
|---|---|---|---|
| `EX-GRP-01` | Step 2 | Room quantity < 5 | Display `MSG-18` ("Group booking requires minimum 5 rooms for discount"). |
| `EX-GRP-02` | Step 2 | Insufficient bulk inventory | Display `MSG-06` ("Selected hotel does not have enough vacant rooms"). |

---

## 4. Functional Requirements (Screen & UI Component Specs)

> [!NOTE]
> Phủ đầy đủ 14 Phân hệ màn hình (4.1 đến 4.14) tương ứng 50 màn hình UI (`SCR-101` ➔ `SCR-510`). Tất cả màn hình có Bảng 6 cột chuẩn.

### 4.1 Authentication Screens
`Mapped to UC-01, UC-02, UC-03, UC-04, UC-32`

#### 4.1.1 SCR-101: LoginPage UI Specification
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `email` | Text Input | Yes | 6 – 100 chars | User email address. Must pass RFC 5322 regex. |
| 2 | `password` | Password Input | Yes | 8 – 50 chars | User account password. Masked input. |
| 3 | `btn_submit` | Button | Yes | — | Submits credentials to `/api/v1/auth/login`. |
| 4 | `btn_google` | OAuth Button | No | — | Triggers Google OAuth2 consent popup. |

---

### 4.2 System Admin — User Management Screens
`Mapped to UC-23`

#### 4.2.1 SCR-110: Admin User & Role Management (AdminDashboardPage)
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `user_table` | Data Table | Yes | — | Lists users with columns: ID, Name, Email, Role, Status. |
| 2 | `role_select` | Dropdown | Yes | `ROLE_*` | Assigns security roles (`ROLE_CUSTOMER`, `ROLE_RECEPTIONIST`, etc.). |
| 3 | `status_toggle` | Switch Toggle | Yes | `ACTIVE / LOCKED` | Locks or unlocks user account access instantly. |

---

### 4.3 System Admin — System Configuration Screens
`Mapped to UC-18, UC-19`

#### 4.3.1 SCR-202: System Settings & Hotel Config Modal
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `hotel_name` | Text Input | Yes | 3 – 150 chars | Full registered name of the hotel property. |
| 2 | `tax_rate` | Number Input | Yes | 0.0 – 20.0 % | System VAT tax rate applied to bookings. |
| 3 | `currency` | Dropdown | Yes | `VND / USD` | Default billing currency for transactions. |

---

### 4.4 Master Data — Hotel Management Screens
`Mapped to UC-06, UC-07, UC-08, UC-18, UC-19, UC-20, UC-27`

#### 4.4.1 SCR-201: Partner Hotel Overview Dashboard
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `hotel_card_grid` | Card Grid | Yes | — | Displays hotel image thumbnail, rating, total rooms, address. |
| 2 | `btn_add_hotel` | Action Button | Yes | — | Opens Hotel Creation Wizard modal. |
| 3 | `search_filter` | Text Search | No | 0 – 50 chars | Filters hotel cards by city or property name. |

---

### 4.5 Master Data — Room & Capacity Screens
`Mapped to UC-09, UC-21, UC-33`

#### 4.5.1 SCR-203: Room Type & Inventory Configurator
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `room_type_name` | Text Input | Yes | 3 – 50 chars | E.g., Deluxe Ocean View, Standard Suite. |
| 2 | `base_price` | Currency Input | Yes | >= 100,000 VND | Base price per night before taxes/vouchers. |
| 3 | `max_occupancy` | Number Spinner| Yes | 1 – 10 persons | Maximum allowed guest count per room. |

---

### 4.6 Master Data — Voucher Storefront Screens
`Mapped to UC-35`

#### 4.6.1 SCR-109: Customer Voucher Storefront & Redemption
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `voucher_card` | Component Tile | Yes | — | Shows code, discount value (e.g., 20%), min spend, expiry date. |
| 2 | `btn_claim` | Action Button | Yes | — | Claims promotion code into user's voucher wallet. |

---

### 4.7 Master Data — Meal Ticket & QR Screens
`Mapped to UC-28, UC-29`

#### 4.7.1 SCR-107: Meal Ticket QR Code Storefront
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `ticket_qr_display` | Image / QR Canvas | Yes | — | Encrypted dynamic QR code for restaurant validation. |
| 2 | `ticket_status` | Status Badge | Yes | `AVAILABLE / USED / EXPIRED` | Indicates ticket redemption status. |

---

### 4.8 Inbound — Individual Booking Screens
`Mapped to UC-06, UC-08, UC-10, UC-11`

#### 4.8.1 SCR-301: Hotel Catalog & Search Hero Page
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `search_location` | Auto-complete | Yes | 2 – 100 chars | City or region destination. |
| 2 | `date_picker` | Date Range | Yes | Current / Future | Check-in and Check-out dates selection (`BR-10`). |
| 3 | `guest_counter` | Number Stepper | Yes | 1 – 20 guests | Total count of adult and child guests. |

---

### 4.9 Group Booking & CTP Manifest Screens
`Mapped to UC-16, UC-17`

#### 4.9.1 SCR-304: CTP Group Booking Wizard Modal
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `group_name` | Text Input | Yes | 3 – 100 chars | Name of CTP corporate group or event. |
| 2 | `room_qty` | Number Input | Yes | 5 – 50 rooms | Minimum 5 rooms required to unlock 25% discount (`BR-02`). |
| 3 | `excel_manifest` | File Upload | No | Max 5MB | Excel `.xlsx` file containing member names and passport IDs. |

---

### 4.10 Operations — Room Matrix & Rapid Check-In Screens
`Mapped to UC-28, UC-29`

#### 4.10.1 SCR-204: Real-time Room Matrix (StaffRoomPage)
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `hotel_filter` | Dropdown | Yes | — | Filter rooms by managed hotel ID. |
| 2 | `room_card` | Component Grid | Yes | — | Visual tile displaying room number, type, status (`VACANT`, `OCCUPIED`, `DIRTY`). |
| 3 | `btn_checkin` | Action Button | Conditional | — | Appears on `VACANT` room to launch Rapid Check-In modal. |
| 4 | `btn_scan_qr` | Floating Button | Yes | — | Opens device camera to scan QR Meal Ticket. |

---

### 4.11 Operations — E-Wallet & Transaction Ledger Screens
`Mapped to UC-13, UC-34`

#### 4.11.1 SCR-105: Customer E-Wallet & Spending Limits
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `wallet_balance` | Currency Text | Yes | — | Displays current available wallet balance. |
| 2 | `daily_limit` | Number Input | Yes | >= 0 VND | Daily spending limit threshold set by user. |
| 3 | `btn_topup` | Action Button | Yes | — | Triggers VNPAY/Stripe top-up modal. |

---

### 4.12 Finance — Billing & Refund Engine Screens
`Mapped to UC-14, UC-34`

#### 4.12.1 SCR-404: Refund Policy Engine & Calculation View
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `booking_code` | Read-only Text | Yes | — | Target booking code requested for cancellation. |
| 2 | `cancellation_time`| Timestamp Text | Yes | — | Exact system time request was initiated. |
| 3 | `refund_percentage`| Percentage Badge| Yes | `100% / 50% / 0%` | Refund tier computed per `BR-14`. |
| 4 | `refund_amount` | Currency Text | Yes | — | Final computed refund cash amount. |

---

### 4.13 Reports and Dashboards
`Mapped to UC-24, UC-25, UC-26, UC-30`

#### 4.13.1 SCR-506: Director Financial Analytics Dashboard
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `period_selector` | Dropdown | Yes | `DAY/WEEK/MONTH/YEAR`| Reporting interval for financial aggregation. |
| 2 | `revenue_chart` | Area Chart | Yes | — | Visual revenue trend line over time. |
| 3 | `btn_export_excel`| Action Button | Yes | — | Generates downloadable Excel `.xlsx` report. |

---

### 4.14 Audit Logs & Security Monitoring
`Mapped to UC-23, UC-31`

#### 4.14.1 SCR-110 (Tab Audit): Audit Log Monitor
| # | Field Name | UI Type | Mandatory | Length / Range | Description |
|---|---|---|---|---|---|
| 1 | `audit_table` | Data Table | Yes | — | Displays Timestamp, Actor Email, Action Type, IP Address, Details. |
| 2 | `action_filter` | Dropdown | No | `LOGIN/CREATE/UPDATE/DELETE`| Filters audit entries by action classification. |

---

## 5. Non-Functional Requirements

### 5.1 External Interfaces

| Interface | Protocol | Details |
|---|---|---|
| **VNPAY Payment Gateway** | HTTPS REST / HMAC SHA512 | Sandbox & Production IPN endpoint for payment validation. |
| **Stripe Payments** | HTTPS REST / Webhook Signing | Payment Intent integration with webhooks. |
| **Google OAuth 2.0** | OAuth2 OpenID Connect | User profile authorization. |
| **SMTP Email Server** | TLS Port 587 | Asynchronous email dispatch for booking vouchers. |

### 5.2 Quality Attributes

#### 5.2.1 Performance
- API Response Time: 95% of read APIs shall respond within < 200ms.
- Search Queries: Room search execution shall complete within < 300ms for 10,000+ room records.

#### 5.2.2 Security
- Password Encryption: Mandatory BCrypt hashing with cost factor >= 12.
- JWT Security: RSA/HMAC SHA-256 tokens expiring in <= 24 hours.

#### 5.2.3 Reliability
- Availability: System uptime target 99.9% availability.
- Data Integrity: Database transactions enforced with ACID compliance `@Transactional`.

#### 5.2.4 Usability
- UI Responsiveness: Seamless display across Mobile (375px), Tablet (768px), and Desktop (1440px+).
- Accessibility: Text contrast ratios meeting WCAG 2.1 AA standards; keyboard navigable modals.

#### 5.2.5 Maintainability
- Code Coverage: Unit & Integration test coverage maintained at >= 80% across service layer.
- Modularization: Package-by-Feature design allowing isolated module maintenance.

---

## 6. Requirement Traceability

### 6.1 Diagram Insertion Checklist

| Diagram ID | Section Placement | Status | Description |
|---|---|---|---|
| `SRS-CTX-01` | §2.1 Context | ✓ Inserted | System Context Diagram |
| `SRS-BP-01` | §2.2.1 Swimlane | ✓ Inserted | Individual Booking Process Swimlane |
| `SRS-BP-02` | §2.2.2 Swimlane | ✓ Inserted | Group Booking Process Swimlane |
| `SRS-BP-03` | §2.2.3 Swimlane | ✓ Inserted | Operations & Check-In Swimlane |
| `SRS-BP-04` | §2.2.4 Swimlane | ✓ Inserted | Refund Policy Engine Swimlane |
| `SRS-BP-05` | §2.2.5 Swimlane | ✓ Inserted | Customer E-Wallet Swimlane |
| `SRS-BP-06` | §2.2.6 Swimlane | ✓ Inserted | Review & Audit Swimlane |
| `SRS-UC-01` | §2.3.3 Use Case | ✓ Inserted | Customer Use Cases Diagram |
| `SRS-UC-02` | §2.3.3 Use Case | ✓ Inserted | Hotel Staff Use Cases Diagram |
| `SRS-UC-03` | §2.3.3 Use Case | ✓ Inserted | System Admin Use Cases Diagram |
| `SRS-UC-04` | §2.3.3 Use Case | ✓ Inserted | Director Analytics Use Cases Diagram |
| `SRS-UC-05` | §2.3.3 Use Case | ✓ Inserted | System Background Use Cases Diagram |
| `SRS-UC-06` | §2.3.3 Use Case | ✓ Inserted | CTP Group Booking Use Cases Diagram |
| `SRS-SF-01` | §2.4.1 Screen Flow| ✓ Inserted | Customer Screen Flow Diagram |
| `SRS-SF-02` | §2.4.1 Screen Flow| ✓ Inserted | Receptionist Screen Flow Diagram |
| `SRS-SF-03` | §2.4.1 Screen Flow| ✓ Inserted | Admin Screen Flow Diagram |
| `SRS-SF-04` | §2.4.1 Screen Flow| ✓ Inserted | CTP Group Screen Flow Diagram |
| `SRS-SF-05` | §2.4.1 Screen Flow| ✓ Inserted | Director Analytics Screen Flow Diagram |
| `SRS-ERD-01` | §2.5 Logical ERD | ✓ Inserted | Logical Data Domain Overview |
| `SRS-UI-01` | §4.1.1 UI Spec | ✓ Inserted | Login Screen Wireframe / Screenshot |

---

### 6.2 Requirement Traceability Matrix

| Requirement ID | Domain Scope | Mapped Use Cases | Database Tables | Target Verification |
|---|---|---|---|---|
| `REQ-001` | Authentication | `UC-01`, `UC-02`, `UC-03`, `UC-04`, `UC-32` | `users`, `roles` | Unit & Security Test |
| `REQ-002` | User Profile | `UC-05`, `UC-23` | `users`, `user_profiles` | Integration Test |
| `REQ-003` | Hotel Catalog | `UC-06`, `UC-07`, `UC-08`, `UC-18`, `UC-19`, `UC-20` | `hotels`, `hotel_images` | Catalog Query Test |
| `REQ-004` | Room Inventory | `UC-09`, `UC-21`, `UC-33` | `rooms`, `room_types` | Concurrency Test |
| `REQ-005` | Booking Engine | `UC-10`, `UC-11`, `UC-12`, `UC-16`, `UC-17` | `bookings`, `booking_items` | E2E Scenario Test |
| `REQ-006` | Payment & Wallet| `UC-13`, `UC-14`, `UC-34` | `payments`, `wallets`, `refund_requests` | Gateway Webhook Test |
| `REQ-007` | Vouchers & QR | `UC-28`, `UC-29`, `UC-35` | `vouchers`, `meal_tickets` | QR Scan Unit Test |
| `REQ-008` | Analytics & Audit| `UC-24`, `UC-25`, `UC-26`, `UC-30`, `UC-31` | `audit_logs`, `system_settings` | Reporting Query Test |

---

## 7. Requirement Appendix

### 7.1 Business Rules (BR-01 to BR-39)

> [!IMPORTANT]
> Danh mục Business Rules được đánh số liên tục từ BR-01 đến BR-39 (không nhảy số), quy định toàn bộ logic nghiệp vụ hệ thống.

| Rule ID | Rule Statement |
|---|---|
| `BR-01` | Booker minimum age requirement: Customer creating a booking must be at least 18 years old. |
| `BR-02` | Group booking discount: Group reservations containing 5 or more rooms automatically receive a 25% price discount. |
| `BR-03` | Group deposit policy: Group bookings require a minimum 30% advance deposit to confirm room allocation. |
| `BR-04` | Password complexity: Passwords must be at least 8 characters long, containing uppercase, lowercase, and numeric characters. |
| `BR-05` | Account lock threshold: Account access is locked for 15 minutes after 5 consecutive failed login attempts. |
| `BR-06` | Email uniqueness: Every registered user account must possess a unique email address. |
| `BR-07` | Phone format validation: Customer telephone numbers must conform to international standard (+84 / RFC 3966). |
| `BR-08` | Loyalty tier calculation: Loyalty membership tier (SILVER, GOLD, PLATINUM) is recalculated upon invoice completion. |
| `BR-09` | Loyalty point redemption: 100 loyalty points can be redeemed for 10,000 VND discount during checkout. |
| `BR-10` | Date sequence constraint: Check-out date must be strictly after Check-in date (`check_out > check_in`). |
| `BR-11` | Double booking prevention: A room instance cannot have overlapping active reservations for any date. |
| `BR-12` | Maximum stay duration: A single booking reservation cannot exceed 30 consecutive calendar days. |
| `BR-13` | Minimum lead time: Same-day online bookings must be completed at least 2 hours prior to standard check-in time (14:00). |
| `BR-14` | Tiered refund policy: Cancellation > 48h prior (100% refund), 24-48h (50% refund), < 24h (0% refund). |
| `BR-15` | Refund execution timeframe: Automated wallet refunds execute instantly; bank refunds process within 3-5 business days. |
| `BR-16` | Cash refund restriction: Offline cash bookings cancelled at hotel front desk are subject to manager approval. |
| `BR-17` | Maximum room capacity: Guest count cannot exceed `max_occupancy` specification of the booked room type. |
| `BR-18` | Extra bed charge: Adding an extra guest beyond base capacity incurs a fixed surcharge of 200,000 VND/night. |
| `BR-19` | Child policy: Children under 6 years stay free when sharing existing bedding with parents. |
| `BR-20` | Standard check-in time: Hotel standard check-in time is fixed at 14:00 local time. |
| `BR-21` | Standard check-out time: Hotel standard check-out time is fixed at 12:00 local time. |
| `BR-22` | Early check-in fee: Check-in before 09:00 incurs 50% daily room rate fee. |
| `BR-23` | Late check-out fee: Check-out between 12:00 and 18:00 incurs 50% daily room rate fee; past 18:00 incurs 100% fee. |
| `BR-24` | Room cleaning state transition: Checking out automatically transitions physical room status to `DIRTY`. |
| `BR-25` | Housekeeper verification: Room status cannot transition from `DIRTY` to `VACANT` without housekeeper sign-off. |
| `BR-26` | Meal ticket issuance: 1 complimentary QR meal ticket per guest per day is generated upon check-in. |
| `BR-27` | Meal ticket validity: QR meal tickets are valid strictly during breakfast hours (06:30 – 09:30). |
| `BR-28` | Meal ticket single use: A QR meal ticket transitions to `USED` instantly upon staff scanner validation. |
| `BR-29` | Voucher single use: Promotion vouchers are single-use per customer account unless explicitly defined. |
| `BR-30` | Voucher minimum order: Vouchers check `min_order_amount` before applying percentage or flat discounts. |
| `BR-31` | Voucher stack restriction: Only 1 promotion voucher can be applied per booking transaction. |
| `BR-32` | Maximum voucher cap: Percentage vouchers enforce a fixed maximum discount cap (`max_discount_amount`). |
| `BR-33` | Room lock duration: Temporary inventory hold during checkout expires after exactly 600 seconds (10 minutes). |
| `BR-34` | Lock release auto-cron: Unpaid temporary room locks are released automatically by system background cron. |
| `BR-35` | Wallet balance cap: Maximum allowable balance stored in a customer e-wallet is 50,000,000 VND. |
| `BR-36` | Daily spending limit: Wallet transactions exceeding user-configured daily spending limit require OTP verification. |
| `BR-37` | Review moderation requirement: User submitted reviews containing text must pass admin moderation before publication. |
| `BR-38` | Audit log immutability: Audit log entries in `audit_logs` table can never be updated or deleted by any user role. |
| `BR-39` | VAT invoice issuance: Corporate VAT tax invoices must be requested within 24 hours of checkout completion. |

---

### 7.2 System Messages (MSG-01 to MSG-25)

> [!IMPORTANT]
> Danh mục System Messages được đánh số liên tục từ MSG-01 đến MSG-25 (không nhảy số), quy định các thông báo giao diện.

| Message ID | Type | Message Content String |
|---|---|---|
| `MSG-01` | Error | "Invalid username or password." |
| `MSG-02` | Error | "Your session has expired. Please log in again." |
| `MSG-03` | Error | "Your account has been locked due to security policy. Please contact administrator." |
| `MSG-04` | Error | "Google OAuth authentication failed. Please try again." |
| `MSG-05` | Error | "Check-out date must be after check-in date." |
| `MSG-06` | Warning | "Selected room type is fully booked for the selected dates." |
| `MSG-07` | Error | "Booking registration failed due to inventory lock conflict. Please retry." |
| `MSG-08` | Error | "Payment transaction failed or was cancelled by user." |
| `MSG-09` | Warning | "Your temporary 10-minute room lock has expired. Please re-select your rooms." |
| `MSG-10` | Success | "Booking confirmed successfully! E-ticket and QR code sent to your email." |
| `MSG-11` | Success | "Booking cancellation successful. Refund has been processed to your wallet." |
| `MSG-12` | Error | "Cancellation deadline passed. This reservation is non-refundable per policy." |
| `MSG-13` | Error | "Invalid or expired promotion voucher code." |
| `MSG-14` | Error | "Booking subtotal does not meet the minimum order requirement for this voucher." |
| `MSG-15` | Success | "Voucher applied successfully! Discount deducted from total." |
| `MSG-16` | Error | "Insufficient e-wallet balance. Please top up your wallet or select another payment method." |
| `MSG-17` | Warning | "Transaction exceeds your daily spending limit. Please enter OTP to confirm." |
| `MSG-18` | Error | "Group booking requires a minimum of 5 rooms to qualify for group discount." |
| `MSG-19` | Success | "Group member manifest imported successfully from Excel." |
| `MSG-20` | Error | "Rapid check-in failed. Selected room is currently OCCUPIED or DIRTY." |
| `MSG-21` | Success | "Guest checked in successfully. Room status updated to OCCUPIED." |
| `MSG-22` | Success | "Meal ticket verified successfully! Enjoy your meal." |
| `MSG-23` | Error | "Meal ticket has already been USED or is EXPIRED." |
| `MSG-24` | Success | "Review submitted successfully and sent for moderation approval." |
| `MSG-25` | Success | "User profile updated successfully." |

---

### 7.3 Other Requirements (Global Constraints)
- **Framework Constraint**: Backend must run on Java 17 LTS and Spring Boot 4.0.0-M1.
- **Database Architecture**: Primary DB PostgreSQL (Neon Cloud) with secondary local SQL Server support. All DDL executed exclusively via Flyway versioned scripts (V1 - V33).
- **Security Baseline**: No plaintext secrets in code or git; JWT expiration <= 24 hours; OWASP Top 10 compliance.
