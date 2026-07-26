# TÀI LIỆU CHUYÊN SÂU: PHÂN HỆ THANH TOÁN (PAYMENT) & VOUCHER MÃ GIẢM GIÁ
## DÙNG CHO BÁO CÁO REVIEW VÀ BẢO VỆ TRƯỚC GIẢNG VIÊN & HỘI ĐỒNG CHUYÊN MÔN
> **Dự án:** Hotel Booking System (Hệ thống Đặt phòng Khách sạn Trực tuyến)  
> **Kiến trúc:** Java 17 · Spring Boot 3+ · SQL Server · Package-by-Feature (`com.hotelbooking.payment`, `com.hotelbooking.voucher`)  
> **Đặc tả áp dụng:** SPEC-034 (Online Payment & Refund), SPEC-035 (Voucher Management), SPEC-013 (Booking Integration).

---

# PHẦN I: ĐẶC TẢ NGHIỆP VỤ & QUY TẮC BUSINESS RULES (FROM SPEC TO ARCHITECTURE)

## 1.1 Tổng Quan Đặc Tả Nghiệp Vụ Thanh Toán (SPEC-034: Payment Engine)
Phân hệ Thanh toán đóng vai trò là "nút chặn tài chính" của toàn bộ hệ thống đặt phòng. Hệ thống hỗ trợ 3 hình thức thanh toán chính:
1. **STRIPE (Thanh toán thẻ quốc tế / Online Payment Intent):**
   - Tích hợp cổng Stripe SDK qua API `PaymentIntent`.
   - Cơ chế xác nhận bất đồng bộ (Asynchronous Event-Driven) qua **Stripe Webhook**.
   - Bảo mật chữ ký Webhook (HMAC-SHA256) nhằm ngăn chặn tấn công giả mạo yêu cầu (Replay Attacks).
2. **CASH (Thanh toán Tiền mặt tại quầy):**
   - Khách chọn đặt phòng trả sau hoặc thanh toán trực tiếp với Lễ tân/Thu ngân.
   - Trạng thái Booking lập tức chuyển sang `CONFIRMED` với trạng thái thanh toán `PENDING`.
   - Nhân viên / Lễ tân / Quản trị viên (`STAFF`, `RECEPTIONIST`, `ADMIN`, `DIRECTOR`) bấm nút xác nhận tiền mặt trên hệ thống khi nhận đủ tiền.
3. **BANK_TRANSFER (Chuyển khoản Ngân hàng thủ công):**
   - Hệ thống sinh mã tham chiếu chuyển khoản duy nhất `BK-{bookingId}` cùng thông tin tài khoản ngân hàng.
   - Trạng thái thanh toán ở dạng `PENDING` hoặc `PENDING_VERIFICATION` chờ Nhân viên/Admin đối soát sao kê và xác nhận thủ công (`confirmBankTransfer`).

---

## 1.2 Tổng Quan Đặc Tả Nghiệp Vụ Voucher (SPEC-035: Voucher Engine)
Hệ thống Voucher được thiết kế nhằm áp dụng mã giảm giá chính xác, minh bạch và chống lạm dụng.

### Các Quy Tắc Kiểm Tra Hợp Lệ Của Voucher (Voucher Validation Rules):
1. **Trạng thái Booking:** Voucher chỉ được áp dụng cho Booking ở trạng thái `PENDING` (chưa thanh toán). Mỗi Booking chỉ được áp **tối đa 1 Voucher**.
2. **Thời gian hiệu lực (Validity Window):**  
   - `startDate`: Nếu thời gian hiện tại `now < startDate` $\rightarrow$ Báo lỗi *"Voucher is not yet active"*.
   - `endDate`: Nếu thời gian hiện tại `now > endDate` $\rightarrow$ Báo lỗi *"Voucher has expired"*.
3. **Giới hạn số lần sử dụng (Usage Limit & Capacity):**  
   - Nếu `maxUsage > 0` và `currentUsage >= maxUsage` $\rightarrow$ Báo lỗi *"Voucher has reached its usage limit"*.
   - *Lưu ý quan trọng:* Số lượt dùng (`currentUsage`) chỉ thực sự tăng lên **khi giao dịch thanh toán thành công** (`handlePaymentSuccess`), giúp tránh tình trạng khách áp mã nhưng hủy ngang gây lãng phí lượt dùng voucher.
4. **Giá trị đơn hàng tối thiểu (Minimum Booking Value):**  
   - Nếu `totalAmount < minBookingValue` $\rightarrow$ Báo lỗi *"Booking total does not meet the minimum value requirement"*.

### Công Thức Tính Toán Giá & Giảm Giá (Discount Calculation Logic):
- **Trường hợp 1: Giảm theo phần trăm (`PERCENTAGE`):**
  $$\text{discountAmount} = \text{totalAmount} \times \left( \frac{\text{discountValue}}{100} \right)$$
  - Nếu voucher có quy định mức giảm tối đa (`maxDiscount`):  
    $$\text{discountAmount} = \min(\text{discountAmount}, \text{maxDiscount})$$
- **Trường hợp 2: Giảm số tiền cố định (`FIXED_AMOUNT`):**
  $$\text{discountAmount} = \text{discountValue}$$
- **Ràng buộc khống chế (Boundary Protection):**
  Số tiền giảm không bao giờ được vượt quá tổng tiền phòng gốc:
  $$\text{discountAmount} = \min(\text{discountAmount}, \text{totalAmount})$$
- **Số tiền thanh toán cuối cùng (`finalPrice`):**
  $$\text{finalPrice} = \text{totalAmount} - \text{discountAmount}$$

---

# PHẦN II: CẤU TRÚC MÃ NGUỒN CHI TIẾT TỪNG CLASS (FROM CODE)

Toàn bộ phân hệ Thanh toán và Voucher được tổ chức theo mô hình **Package-by-Feature** đóng gói trong 2 package chính: `com.hotelbooking.payment` và `com.hotelbooking.voucher`.

```
src/main/java/com/hotelbooking/
├── payment/
│   ├── Payment.java                        # Entity lưu thông tin giao dịch thanh toán
│   ├── PaymentAuditLog.java                # Entity nhật ký vết giao dịch (Audit Trail)
│   ├── PaymentRepository.java              # JPA Repository cho giao dịch thanh toán
│   ├── PaymentAuditLogRepository.java       # JPA Repository cho nhật ký vết
│   ├── PaymentService.java                 # Interface định nghĩa các nghiệp vụ thanh toán
│   ├── PaymentServiceImpl.java             # Implementation chứa core logic Stripe, Cash, Bank, Refund
│   ├── PaymentController.java              # REST API endpoints cho User & Admin
│   ├── PaymentWebhookController.java       # REST API endpoint nhận callback từ Stripe Webhook
│   └── dto/
│       ├── PaymentRequestDTO.java          # DTO nhận yêu cầu khởi tạo thanh toán
│       ├── PaymentResponseDTO.java         # DTO trả về thông tin clientSecret / Ngân hàng
│       ├── PaymentConfirmRequest.java      # DTO xác nhận thanh toán thủ công
│       └── WebhookCallbackDTO.java         # DTO cấu trúc thông điệp Webhook
└── voucher/
    ├── Voucher.java                        # Entity lưu thông tin Mã giảm giá
    ├── VoucherRepository.java              # JPA Repository cho Voucher
    ├── VoucherService.java                 # Interface dịch vụ Voucher
    ├── VoucherServiceImpl.java             # Core logic áp dụng & tra cứu Voucher
    ├── VoucherController.java              # REST API endpoints cho Voucher
    └── dto/
        ├── ApplyVoucherRequestDTO.java     # DTO yêu cầu áp dụng voucher vào booking
        └── VoucherResponse.java            # DTO trả về thông tin hiển thị Voucher
```

---

## 2.1 Bảng Chi Tiết Tất Cả Các Class & Chức Năng

| Tên Class / Interface | Đường Dẫn (Package Path) | Loại Class | Vai Trò & Chức Năng Chi Tiết |
| :--- | :--- | :--- | :--- |
| **`Payment.java`** | `com.hotelbooking.payment` | `@Entity` | Đánh ánh bảng `payments` trong CSDL SQL Server. Lưu mã giao dịch (`transactionId`), phương thức (`paymentMethod`), cổng (`gateway`), số tiền (`amount`), trạng thái (`status`), thông tin hoàn tiền (`refundAmount`, `refundTime`, `refundRetryCount`) và liên kết `@OneToOne` với `Booking`. |
| **`PaymentAuditLog.java`** | `com.hotelbooking.payment` | `@Entity` | Đánh ánh bảng `payment_audit_logs`. Lưu vết audit trail toàn bộ lịch sử tác động thanh toán (Tạo intent, Webhook nhận được, xác minh chữ ký thất bại, hoàn tiền thành công/thất bại) phục vụ truy vết bảo mật & kế toán. |
| **`PaymentRepository.java`** | `com.hotelbooking.payment` | `@Repository` | Tầng truy xuất CSDL cho `Payment`. Chứa các hàm truy vấn đặc biệt như `findByTransactionIdForUpdate` (sử dụng Khóa bi quan `PESSIMISTIC_WRITE` tránh xung đột Webhook) và `findByStatusAndRefundRetryCountLessThan`. |
| **`PaymentAuditLogRepository.java`** | `com.hotelbooking.payment` | `@Repository` | Tầng truy xuất CSDL cho `PaymentAuditLog`. |
| **`PaymentService.java`** | `com.hotelbooking.payment` | `Interface` | Khai báo các hợp đồng nghiệp vụ: `createPaymentRequest`, `processStripeWebhook`, `verifyPayment`, `confirmCashPayment`, `confirmBankTransfer`, `processRefund`, `retryFailedRefunds`. |
| **`PaymentServiceImpl.java`** | `com.hotelbooking.payment` | `@Service` | **Trái tim của phân hệ Payment (434 dòng code)**. Triển khai tích hợp Stripe API (`PaymentIntent.create`, `Refund.create`), kiểm tra chữ ký Webhook HMAC-SHA256, xử lý Idempotency, cập nhật trạng thái Booking & Voucher, và chạy Tác vụ ngầm `@Scheduled` tự động hoàn tiền lại khi lỗi. |
| **`PaymentController.java`** | `com.hotelbooking.payment` | `@RestController` | Định tuyến các API người dùng và quản trị viên: `/api/v1/payments/create`, `/api/v1/payments/verify`, `/{bookingId}/refund`, `/{paymentId}/confirm-cash`, `/{paymentId}/confirm-bank`. Bảo mật bằng `@PreAuthorize`. |
| **`PaymentWebhookController.java`** | `com.hotelbooking.payment` | `@RestController` | Endpoint công khai `/api/payments/webhook` tiếp nhận sự kiện từ Stripe Server. Bắt lỗi `SignatureVerificationException` trả về HTTP 401, lỗi nghiệp vụ trả HTTP 200 để tránh Stripe gửi lại vô hạn. |
| **`Voucher.java`** | `com.hotelbooking.voucher` | `@Entity` | Ánh ánh bảng `vouchers`. Đóng gói thông tin mã giảm giá (`code`), loại giảm (`discountType`: `PERCENTAGE` / `FIXED_AMOUNT`), giá trị giảm (`discountValue`), giá trị đơn tối thiểu (`minBookingValue`), giảm tối đa (`maxDiscount`), thời hạn (`startDate`, `endDate`), số lượt dùng tối đa (`maxUsage`) và lượt đã dùng (`currentUsage`). |
| **`VoucherRepository.java`** | `com.hotelbooking.voucher` | `@Repository` | JPA Repository hỗ trợ hàm `findByCode(String code)`. |
| **`VoucherService.java`** | `com.hotelbooking.voucher` | `Interface` | Khai báo phương thức `applyVoucher` và `getAllActiveVouchers`. |
| **`VoucherServiceImpl.java`** | `com.hotelbooking.voucher` | `@Service` | Triển khai 6 bước rà soát điều kiện hợp lệ của Voucher, tính toán số tiền được giảm (`discountAmount`) và cập nhật `finalPrice` cho `Booking`. |
| **`VoucherController.java`** | `com.hotelbooking.voucher` | `@RestController` | Định tuyến API cho người dùng áp mã `/api/v1/vouchers/apply` và xem danh sách voucher khả dụng `/api/v1/vouchers`. |

---

## 2.2 Mô Hình Dữ Liệu CSDL (Database Schema & Table Mapping)

### Bảng `payments`
```sql
CREATE TABLE payments (
    payment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL FOREIGN KEY REFERENCES bookings(booking_id),
    payment_method VARCHAR(50) NOT NULL,    -- STRIPE, CASH, BANK_TRANSFER
    gateway VARCHAR(50),                    -- STRIPE, CASH, MANUAL_BANK
    amount DECIMAL(18,2) NOT NULL,          -- Số tiền giao dịch
    status VARCHAR(50) NOT NULL,            -- PENDING, SUCCESS, FAILED, REFUND_PENDING, REFUNDED
    transaction_id VARCHAR(100) UNIQUE,     -- Stripe PaymentIntent ID (pi_...) hoặc CASH-...
    payment_time DATETIME2,                 -- Thời điểm thanh toán thành công
    refund_amount DECIMAL(18,2),            -- Số tiền hoàn
    refund_time DATETIME2,                  -- Thời điểm hoàn tiền
    refund_transaction_id VARCHAR(100),     -- Mã giao dịch hoàn tiền
    refund_retry_count INT DEFAULT 0        -- Số lần đã thử lại hoàn tiền tự động
);
```

### Bảng `vouchers`
```sql
CREATE TABLE vouchers (
    voucher_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,      -- Mã voucher (ví dụ: SUMMER2026)
    discount_type VARCHAR(20) NOT NULL,     -- PERCENTAGE, FIXED_AMOUNT
    discount_value DECIMAL(18,2) NOT NULL,  -- Phần trăm hoặc Số tiền cố định
    max_discount DECIMAL(18,2),             -- Mức giảm tối đa (nếu là PERCENTAGE)
    min_booking_value DECIMAL(18,2) DEFAULT 0, -- Giá trị đơn đặt tối thiểu
    start_date DATETIME2,                   -- Ngày bắt đầu hiệu lực
    end_date DATETIME2,                     -- Ngày hết hạn
    max_usage INT DEFAULT 0,                -- Giới hạn số lượt (0 = không giới hạn)
    currentUsage INT DEFAULT 0              -- Số lượt đã sử dụng thành công
);
```

---

# PHẦN III: TOÀN BỘ LUỒNG NGHIỆP VỤ & LUỒNG DỮ LIỆU TỪNG DÒNG CODE (END-TO-END DATA FLOW)

## 3.1 Luồng 1: Áp Dụng Mã Giảm Giá (Voucher Application Flow)

```
[Client] --(POST /api/v1/vouchers/apply)--> [VoucherController]
                                                    |
                                       [VoucherServiceImpl.applyVoucher]
                                                    |
         +------------------------------------------+------------------------------------------+
         |                                          |                                          |
 1. Check Status = PENDING                2. Check Unique Voucher                  3. Find Code in DB
 (Chưa thanh toán)                       (Chưa từng áp mã)                        (VoucherRepository)
         |                                          |                                          |
         +------------------------------------------+------------------------------------------+
                                                    |
 4. Rà soát Điều kiện: (StartDate <= Now <= EndDate) & (CurrentUsage < MaxUsage) & (Total >= MinBookingValue)
                                                    |
 5. Tính discountAmount (Hỗ trợ PERCENTAGE + maxDiscount HOẶC FIXED_AMOUNT)
                                                    |
 6. Cập nhật Booking: setVoucher(v), setDiscountAmount(d), setFinalPrice(total - d) -> save()
```

### Chi tiết xử lý dòng code trong `VoucherServiceImpl.java`:
- **Line 29-34:** Kiểm tra Booking tồn tại và có trạng thái `PENDING`. Nếu trạng thái là `CONFIRMED` hay `CANCELLED`, ném ngoại lệ `BusinessException`.
- **Line 43-61:** Rà soát điều kiện ngày hiệu lực, giới hạn lượt dùng `currentUsage >= maxUsage` và giá trị đơn vị `totalAmount < minBookingValue`.
- **Line 64-72:** Tính toán giảm giá: Nếu `PERCENTAGE`, lấy `totalAmount * (discountValue / 100)`. Nếu kết quả vượt quá `maxDiscount`, gán `discountAmount = maxDiscount`.
- **Line 79-85:** Tính `finalPrice = totalAmount - discountAmount`. Lưu liên kết `booking.setVoucher(voucher)` và lưu xuống database.

---

## 3.2 Luồng 2: Khởi Tạo Thanh Toán Online / Tiền Mặt / Chuyển Khoản (Payment Creation Flow)

```
[Client] --(POST /api/v1/payments/create)--> [PaymentController]
                                                    |
                                       [PaymentServiceImpl.createPaymentRequest]
                                                    |
             +--------------------------------------+--------------------------------------+
             |                                      |                                      |
    [TH 1: CASH]                   [TH 2: BANK_TRANSFER]                     [TH 3: STRIPE]
             |                                      |                                      |
 - Sinh transactionId:              - Sinh transactionId:                 - Gọi Stripe SDK:
   "CASH-" + UUID                     "BT-" + UUID                          `PaymentIntent.create()`
 - Payment status = PENDING         - Payment status = PENDING            - Amount = finalPrice * 100 (Cents)
 - Booking status = CONFIRMED       - Trả về Thông tin STK Ngân hàng      - Trả về `clientSecret` cho FE
 - Trả clientSecret="CASH_PAYMENT"    chuẩn SWIFT/Branch                     hiển thị khung nhập thẻ
```

### Chi tiết xử lý dòng code trong `PaymentServiceImpl.java`:
- **Line 58-63:** Tìm Booking theo `bookingId`. Đảm bảo Booking ở trạng thái `PENDING`.
- **Line 134-142 (Stripe Creation):**  
  ```java
  PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
          .setAmount(booking.getTotalAmount().multiply(new BigDecimal(100)).longValue()) // Đổi USD sang Cents
          .setCurrency("usd")
          .putMetadata("bookingId", booking.getBookingId().toString())
          .addPaymentMethodType("card")
          .build();
  PaymentIntent intent = PaymentIntent.create(params);
  ```
- **Line 145-166:** Lưu bản ghi `Payment` với trạng thái `PENDING` và `transactionId = intent.getId()`. Đồng thời lưu bản ghi `PaymentAuditLog` đánh dấu hành động `CREATE_PAYMENT_INTENT_SUCCESS`. Trả về DTO chứa `clientSecret` để Frontend Stripe Elements dựng form nhập thẻ.

---

## 3.3 Luồng 3: Xử Lý Bất Đồng Bộ Webhook Từ Stripe & Tự Động Xác Nhận (Webhook Processing & Idempotency)

Đây là luồng kỹ thuật quan trọng nhất trong việc tích hợp cổng thanh toán trực tuyến.

```
[Stripe Server] --(POST /api/payments/webhook Header: Stripe-Signature)--> [PaymentWebhookController]
                                                                                      |
                                                                   [PaymentServiceImpl.processStripeWebhook]
                                                                                      |
 1. Xác thực Chữ ký HMAC-SHA256: `Webhook.constructEvent(payload, sigHeader, secret)`
    -> Thất bại: Ném SecurityException -> Trả về HTTP 401 Unauthorized (Chống giả mạo)
                                                                                      |
 2. Bắt sự kiện: `event.getType() == "payment_intent.succeeded"`
                                                                                      |
 3. Gọi `handlePaymentSuccess(transactionId, payload)`
    -> Sử dụng Khóa Bi Quan (Pessimistic Lock): `paymentRepository.findByTransactionIdForUpdate(transactionId)`
                                                                                      |
 4. KIỂM TRA IDEMPOTENCY (Kháng Trùng Lặp Webhook):
    - Nếu `payment.getStatus() == "SUCCESS"` -> Bỏ qua ngay (Return), không xử lý lại!
                                                                                      |
 5. CẬP NHẬT TRẠNG THÁI & VOUCHER:
    - `payment.setStatus("SUCCESS")`
    - `booking.setPaymentStatus("SUCCESS")`, `booking.setStatus("CONFIRMED")`
    - NẾU CÓ VOUCHER: `voucher.setCurrentUsage(voucher.getCurrentUsage() + 1)` -> Save Voucher!
                                                                                      |
 6. Gửi Email Xác Nhận Đặt Phòng (Async Email Service) & Lưu Audit Log `PAYMENT_SUCCESS`.
```

### Cơ chế Kháng Trùng Lặp (Idempotency Mechanism):
Stripe có thể gửi một sự kiện Webhook nhiều lần (do mạng chập chờn hoặc timeout). Nếu không xử lý Idempotency:
- Số lượt sử dụng Voucher sẽ bị cộng tăng vô lý nhiều lần.
- Email xác nhận đặt phòng bị gửi lặp lại nhiều lần.
- Dữ liệu bị xung đột state machine.  
**Giải pháp:** Trong dòng code **Line 206-209**, hệ thống kiểm tra: `if ("SUCCESS".equals(payment.getStatus())) return;`. Nhờ vậy, cho dù Stripe gửi 100 webhook trùng lặp, hệ thống chỉ xử lý duy nhất ở lần đầu tiên.

---

## 3.4 Luồng 4: Tự Động Hoàn Tiền (Refund) & Tác Vụ Ngầm Retry Engine (`@Scheduled`)

Khi Khách hàng hủy phòng hợp lệ (theo Business Rules) hoặc Admin chủ động bấm Hoàn tiền:

```
[Admin / System] --(POST /api/v1/payments/{bookingId}/refund)--> [PaymentServiceImpl.processRefund]
                                                                               |
 1. Kiểm tra Booking & Payment đã thanh toán (`SUCCESS`).
 2. Chuyển trạng thái Payment sang `REFUND_PENDING` và set `refundRetryCount = 0`.
                                                                               |
                                                       [TÁC VỤ NGẦM SCHEDULER: `retryFailedRefunds()`]
                                                       (Chạy tự động 60 giây / lần: `@Scheduled(fixedDelay = 60000)`)
                                                                               |
 3. Tìm các Payment có `status = "REFUND_PENDING"` và `refundRetryCount < 3`.
                                                                               |
 4. Tăng `refundRetryCount = refundRetryCount + 1`.
                                                                               |
 5. Gọi Stripe Refund API: `Refund.create(RefundCreateParams)`
    +------------- THÀNH CÔNG -------------+------------- THẤT BẠI (Mạng lỗi / Thẻ hết hạn) -------------+
    |                                                                                                       |
 - `payment.setStatus("REFUNDED")`                                                        - Kiểm tra số lần thử:
 - `booking.setStatus("CANCELLED")`                                                         + Nếu `attemptCount < 3`: Giữ nguyên `REFUND_PENDING`
 - Gửi Email xác nhận hoàn tiền cho khách.                                                    để lượt Scheduler sau thử lại.
 - Lưu Audit Log `REFUND_SUCCESS`.                                                          + Nếu `attemptCount >= 3`: Chuyển trạng thái sang
                                                                                              `MANUAL_REFUND_REQUIRED` và gửi cảnh báo
                                                                                              cho Kế toán / Admin xử lý thủ công!
```

---

# PHẦN IV: BẢO VỆ BẰNG VĂN NÓI — KỊCH BẢN NÓI THUYẾT TRÌNH TRƯỚC GIẢNG VIÊN (ORAL DEFENSE SCRIPT)

*Dưới đây là kịch bản chuẩn văn nói kỹ thuật giúp bạn trình bày tự tin, chuyên nghiệp và thuyết phục tuyệt đối trước Hội đồng Giảng viên.*

---

### 🎙️ Lời Mở Đầu Giới Thiệu Phân Hệ Payment & Voucher:
> *"Kính thưa Thầy/Cô và Hội đồng, sau đây em xin đại diện nhóm trình bày chi tiết về **Phân hệ Thanh toán (Payment Engine) và Quản lý Mã giảm giá (Voucher Engine)** của hệ thống Hotel Booking System.*  
> *Đây là phân hệ nòng cốt đảm bảo tính toàn vẹn về mặt tài chính và giao dịch của ứng dụng. Phân hệ được chúng em thiết kế tuân thủ nghiêm ngặt hai tài liệu đặc tả **SPEC-034** và **SPEC-035**, áp dụng mô hình kiến trúc **Package-by-Feature** đóng gói trong 2 package `com.hotelbooking.payment` và `com.hotelbooking.voucher`."*

---

### 🎙️ Trình Bày Về Luồng Thanh Toán Online & Stripe Webhook:
> *"Về thanh toán trực tuyến, hệ thống tích hợp cổng thanh toán quốc tế **Stripe API** thông qua cơ chế `PaymentIntent`. Khi người dùng tạo yêu cầu thanh toán, Server không trực tiếp lưu thông tin thẻ để đảm bảo chuẩn bảo mật PCI-DSS, mà sinh ra một mã `clientSecret` trả về cho Frontend hiển thị khung nhập thẻ bảo mật.*  
>  
> *Đặc biệt, để xử lý việc xác nhận thanh toán an toàn, chúng em áp dụng **Kiến trúc Bất đồng bộ dựa trên Sự kiện (Event-Driven Webhook)**:*  
> 1. *Khi giao dịch hoàn tất trên Stripe, Stripe Server sẽ gửi một `POST Webhook` về endpoint `/api/payments/webhook` của ứng dụng.*  
> 2. *Tại `PaymentWebhookController`, dòng code đầu tiên chúng em thực hiện là **xác minh chữ ký HMAC-SHA256** thông qua `Webhook.constructEvent()`. Nếu chữ ký không khớp hoặc bị can thiệp trên đường truyền, hệ thống ném `SecurityException` và từ chối xử lý ngay lập tức.*  
> 3. *Để chống sự cố gửi lặp sự kiện từ cổng thanh toán, chúng em đã triển khai **Cơ chế Kháng trùng lặp (Idempotency)** tại `PaymentServiceImpl.handlePaymentSuccess`. Nhờ cơ chế kiểm tra trạng thái và Khóa bi quan (`PESSIMISTIC_WRITE`), hệ thống đảm bảo cho dù Webhook bị gửi lặp lại bao nhiêu lần thì trạng thái Booking chỉ cập nhật đúng 1 lần và số lượt sử dụng Voucher chỉ được cộng đúng 1 lần."*

---

### 🎙️ Trình Bày Về Luồng Quản Lý Voucher:
> *"Về phân hệ Voucher, chúng em xây dựng bộ quy tắc kiểm tra (Validation Engine) gồm 6 tầng trong `VoucherServiceImpl.java`. Hệ thống hỗ trợ cả 2 hình thức giảm giá: Giảm theo phần trăm (`PERCENTAGE` có khống chế `maxDiscount`) và Giảm số tiền cố định (`FIXED_AMOUNT`).*  
>  
> *Một điểm cải tiến nghiệp vụ quan trọng là: **Số lượt sử dụng của Voucher (`currentUsage`) không bị trừ ngay khi áp mã**, mà chỉ chính thức ghi nhận tăng thêm khi giao dịch thanh toán thành công (`SUCCESS`). Điều này giúp tránh hoàn toàn rủi ro khách áp mã giữ chỗ nhưng không thanh toán làm lãng phí voucher của nhà hàng/khách sạn."*

---

### 🎙️ Trình Bày Về Quy Trình Hoàn Tiền & Tác Vụ Ngầm (Refund Retry Engine):
> *"Cuối cùng, về quy trình Hủy phòng & Hoàn tiền, chúng em xây dựng một **Tác vụ ngầm (Scheduler Retry Engine)** chạy tự động định kỳ bằng `@Scheduled(fixedDelay = 60000)` trong `PaymentServiceImpl`.*  
> *Khi một yêu cầu hoàn tiền bị thất bại do sự cố mạng, tác vụ ngầm sẽ tự động thử lại tối đa 3 lần. Nếu sau 3 lần vẫn thất bại, hệ thống sẽ tự động chuyển trạng thái giao dịch sang `MANUAL_REFUND_REQUIRED` và ghi log vết audit trail (`PaymentAuditLog`) để thông báo cho Nhân viên Kế toán can thiệp xử lý thủ công, đảm bảo không bao giờ bị thất thoát tiền của khách hàng."*

---

# PHẦN V: BỘ CÂU HỎI VÀ ĐÁP ÁN BẢO VỆ CHUYÊN SÂU TRƯỚC HỘI ĐỒNG (HỎI - ĐÁP ĐẦY ĐỦ)

---

### ❓ Câu 1: Tại sao nhóm không lưu trực tiếp thông tin thẻ ngân hàng của khách hàng vào CSDL mà lại dùng Stripe Client Secret?
- **Trả lời:**  
  Việc lưu trữ thông tin thẻ (như số thẻ, mã CVV, ngày hết hạn) trực tiếp vào CSDL vi phạm nghiêm trọng tiêu chuẩn bảo mật quốc tế **PCI-DSS (Payment Card Industry Data Security Standard)** và khiến hệ thống gặp rủi ro pháp lý vô cùng lớn nếu bị rò rỉ dữ liệu.  
  Do đó, nhóm sử dụng mô hình **Stripe PaymentIntent**: Backend chỉ khởi tạo giao dịch trên Stripe Server để nhận về một token tạm thời gọi là `clientSecret`. Client (Frontend) sẽ dùng `clientSecret` này để truyền trực tiếp thông tin thẻ lên Stripe qua SDK bảo mật của Stripe. CSDL của chúng ta chỉ lưu duy nhất mã giao dịch `transactionId` (ví dụ: `pi_3Mtw...`).

---

### ❓ Câu 2: Giả sử Stripe Webhook bị kẻ gian cố tình gửi dữ liệu giả mạo đến endpoint `/api/payments/webhook`, hệ thống ngăn chặn ra sao?
- **Trả lời:**  
  Hệ thống ngăn chặn bằng **Chữ ký số HMAC-SHA256 (Stripe Signature Verification)**.  
  Trong `PaymentWebhookController.java` và `PaymentServiceImpl.java` (Line 175-188), mọi request gửi đến webhook bắt buộc phải chứa Header `Stripe-Signature`.  
  Backend sử dụng `Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret)` để tính toán lại chữ ký dựa trên `payload` và mã bí mật `stripeWebhookSecret` chỉ có Server chúng ta và Stripe biết.  
  Nếu dữ liệu bị sửa đổi dù chỉ 1 ký tự, chữ ký sẽ không khớp, `SignatureVerificationException` sẽ bị kích hoạt và hệ thống lập tức ném lỗi `SecurityException`, trả về HTTP 401 Unauthorized và ghi log vết vào `PaymentAuditLog`.

---

### ❓ Câu 3: Webhook Idempotency (Tính kháng trùng lặp) là gì và nhóm cài đặt nó ở đâu trong code?
- **Trả lời:**  
  *Idempotency* (Tính vô hiệu trùng lặp) là tính chất đảm bảo một thao tác khi thực thi nhiều lần vẫn mang lại kết quả giống hệt như thực thi 1 lần.  
  Trong tích hợp thanh toán, do mạng chập chờn, Stripe có thể gửi lại cùng 1 Webhook (`payment_intent.succeeded`) nhiều lần.  
  Nhóm cài đặt tại hàm `handlePaymentSuccess()` trong `PaymentServiceImpl.java` (Line 206-209):
  ```java
  Payment payment = paymentRepository.findByTransactionIdForUpdate(transactionId)
          .orElseThrow(...);

  if ("SUCCESS".equals(payment.getStatus())) {
      return; // Bỏ qua ngay lập tức, không xử lý lại
  }
  ```
  Nhờ dòng lệnh này, các thao tác như cộng `currentUsage` của Voucher, đổi trạng thái Booking thành `CONFIRMED` hay gửi Email xác nhận chỉ chạy đúng **duy nhất 1 lần**.

---

### ❓ Câu 4: Tại sao trong `PaymentRepository.java` nhóm lại dùng `@Lock(LockModeType.PESSIMISTIC_WRITE)` cho phương thức `findByTransactionIdForUpdate`?
- **Trả lời:**  
  Nhóm sử dụng **Khóa bi quan (Pessimistic Locking)** ở cấp độ CSDL (`SELECT ... WITH (UPDLOCK, ROWLOCK)`) để giải quyết bài toán đua tranh dữ liệu (Race Condition).  
  Trường hợp hai request Webhook trùng lặp từ Stripe gửi đến gần như cùng một milisecond, nếu không dùng khóa bi quan, cả 2 thread có thể cùng đọc trạng thái `payment.getStatus()` là `PENDING` tại cùng một thời điểm và cùng thực hiện cập nhật.  
  Với `PESSIMISTIC_WRITE`, thread đầu tiên chạm vào bản ghi `Payment` sẽ khóa bản ghi đó lại. Thread thứ hai phải chờ thread thứ nhất commit transaction xong. Khi thread thứ hai vào được thì trạng thái đã là `SUCCESS` và sẽ bị return ra ngay theo điều kiện kiểm tra Idempotency.

---

### ❓ Câu 5: Nếu người dùng áp mã giảm giá Voucher nhưng sau đó không tiến hành thanh toán (hủy giữa chừng), số lượt sử dụng voucher có bị mất không?
- **Trả lời:**  
  **Không bị mất.** Nhóm thiết kế tách biệt 2 bước:
  1. Khi gọi API `/api/v1/vouchers/apply`, hệ thống chỉ rà soát điều kiện và tính toán số tiền giảm `discountAmount`, ghi nhận liên kết `booking.setVoucher(voucher)` và cập nhật `finalPrice`. Lúc này `voucher.currentUsage` **chưa tăng**.
  2. Số lượt `currentUsage` của Voucher chỉ thực sự được cộng tăng (`currentUsage + 1`) trong hàm `handlePaymentSuccess()` khi và chỉ khi giao dịch thanh toán chuyển sang `SUCCESS`.  
  Nhờ đó, nếu khách hàng hủy phòng hoặc bỏ dở giữa chừng, lượt sử dụng voucher vẫn giữ nguyên cho các khách hàng khác.

---

### ❓ Câu 6: Làm thế nào hệ thống xử lý khi số tiền giảm giá của Voucher theo % vượt quá số tiền phòng gốc?
- **Trả lời:**  
  Trong `VoucherServiceImpl.java` (Line 74-77), nhóm cài đặt cơ chế bảo vệ ranh giới (Boundary Guard):
  ```java
  if (discountAmount.compareTo(booking.getTotalAmount()) > 0) {
      discountAmount = booking.getTotalAmount();
  }
  BigDecimal finalPrice = booking.getTotalAmount().subtract(discountAmount);
  ```
  Đảm bảo `discountAmount` tối đa chỉ bằng `totalAmount`, dẫn đến `finalPrice` nhỏ nhất là `0`, không bao giờ xảy ra trường hợp `finalPrice` bị âm tiền.

---

### ❓ Câu 7: Quy trình Hoàn tiền (Refund) khi khách hàng hủy phòng hoạt động như thế nào? Nếu cổng Stripe bị lỗi lúc hoàn tiền thì sao?
- **Trả lời:**  
  Khi hoàn tiền, hệ thống không bắt người dùng/admin phải đợi lệnh gọi API đồng bộ sang Stripe. Thay vào đó:
  1. Hàm `processRefund()` đặt trạng thái thanh toán thành `REFUND_PENDING` và set `refundRetryCount = 0`.
  2. Tác vụ ngầm `@Scheduled(fixedDelay = 60000)` trong `PaymentServiceImpl` quét các bản ghi `REFUND_PENDING`.
  3. Nếu gọi API `Refund.create()` của Stripe bị lỗi (do nghẽn mạng), hệ thống tăng `refundRetryCount` lên 1 và giữ nguyên trạng thái `REFUND_PENDING` để lần quét sau (sau 60 giây) thử lại.
  4. Sau 3 lần thử thất bại (`attemptCount >= 3`), hệ thống đổi trạng thái thành `MANUAL_REFUND_REQUIRED` và ghi log cảnh báo để Kế toán xử lý trực tiếp.

---

### ❓ Câu 8: Sự khác biệt giữa `Payment` và `PaymentAuditLog` trong CSDL là gì? Tại sao phải tách thành 2 bảng?
- **Trả lời:**  
  - Bảng `payments` lưu **Trạng thái hiện tại (Current State)** của giao dịch thanh toán (Phục vụ truy vấn nghiệp vụ tức thời như kiểm tra xem booking này đã trả tiền chưa).
  - Bảng `payment_audit_logs` lưu **Nhật ký vết lịch sử (Audit Trail / Append-Only Log)**. Mọi biến động (Khởi tạo intent, Webhook báo về, Xác minh chữ ký thất bại, Thử lại hoàn tiền) đều được ghi thêm một bản ghi mới kèm payload JSON.  
  Việc tách 2 bảng tuân thủ nguyên tắc thiết kế hệ thống tài chính: Bảng trạng thái phục vụ OLTP nhanh chóng, bảng audit log phục vụ đối soát kế toán, điều tra sự cố bảo mật và bảo vệ tính chống chối bỏ (Non-repudiation).

---

### ❓ Câu 9: Nhóm sử dụng annotation nào để phân quyền truy cập các API Thanh toán và Voucher?
- **Trả lời:**  
  Nhóm sử dụng Spring Security `@PreAuthorize` kết hợp với chuẩn phân quyền RBAC (Role-Based Access Control):
  - API áp Voucher `/api/v1/vouchers/apply` & Tạo thanh toán `/payments/create`: `@PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN', 'DIRECTOR', 'RECEPTIONIST')")`.
  - API Hoàn tiền `/{bookingId}/refund`: `@PreAuthorize("hasRole('ADMIN')")` — chỉ Quản trị viên cao nhất mới có quyền kích hoạt hoàn tiền.
  - API Xác nhận tiền mặt / chuyển khoản: `@PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'DIRECTOR')")`.
  - API Webhook `/api/payments/webhook`: Không dùng `@PreAuthorize` vì đây là endpoint công khai tiếp nhận request từ Server Stripe, nhưng được bảo vệ bằng kiểm tra chữ ký số HMAC-SHA256.

---

### ❓ Câu 10: Phân hệ Payment và Voucher liên kết với các Phân hệ khác trong hệ thống như thế nào? Có bị phụ thuộc vòng (Circular Dependency) không?
- **Trả lời:**  
  Phân hệ `payment` và `voucher` chỉ phụ thuộc một chiều vào phân hệ `booking` (`BookingRepository`, `Booking` Entity).  
  Nhờ mô hình **Package-by-Feature**, các quan hệ JPA `@OneToOne` và `@ManyToOne` được định nghĩa rõ ràng. `Payment` chứa khóa ngoại `booking_id`, `Booking` chứa khóa ngoại `voucher_id`.  
  Các Repository nội bộ của từng feature được đóng gói an toàn, không có tình trạng phụ thuộc vòng giữa các Service, tuân thủ nghiêm ngặt nguyên tắc thiết kế sạch (Clean Architecture).

---
*Tài liệu này được biên soạn đầy đủ, siêu chi tiết từ Đặc tả (Spec), Cấu trúc Code, Luồng dữ liệu, Kịch bản văn nói bảo vệ đến Bộ Q&A chuyên sâu dành cho đồ án Hotel Booking System.*
