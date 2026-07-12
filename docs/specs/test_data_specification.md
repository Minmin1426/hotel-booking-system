# Đặc Tả Dữ Liệu Kiểm Thử (Test Data Specification)
# Dự án: Hotel Booking System

Tài liệu này cung cấp hướng dẫn cách quản lý dữ liệu kiểm thử (Test Data) xuyên suốt 4 cấp độ kiểm thử (Unit, Integration, System, Acceptance Test) và cung cấp các mẫu biểu (templates) chuẩn hóa để tài liệu hóa dữ liệu chạy test cho dự án.

---

## 1. Quản lý Dữ liệu Kiểm thử Theo 4 Cấp Độ (Overview)

| Cấp độ Test | Nơi lưu trữ / Cơ chế khởi tạo dữ liệu | Phạm vi ảnh hưởng | Ví dụ dữ liệu thực tế |
| :--- | :--- | :--- | :--- |
| **Unit Test** | Khởi tạo cứng trong code Java (In-memory Mock Object, Mockito Stubbing). | Độc lập trong một Class/Method, chạy nhanh, không ghi vào DB. | Tạo đối tượng `Booking` giả lập qua Builder/Constructor, thiết lập `booking.setStatus(COMPLETED)`. |
| **Integration Test** | Seed qua các script SQL (Flyway test migrations) hoặc gửi dữ liệu qua payload HTTP giả lập (`MockMvc`). | Kiểm thử sự tương tác giữa nhiều lớp (Service + Database, Controller + Security). | SQL Script chèn sẵn tài khoản mẫu, gửi JSON Request body áp dụng Voucher qua API `POST /vouchers/apply`. |
| **System Test** | Môi trường Sandbox / Staging DB (đã được seed dữ liệu mẫu lớn hoặc dữ liệu thật giả lập). | Toàn hệ thống (End-to-End từ giao diện Frontend sang Backend, Database và Gateway VNPay). | Tài khoản thẻ test VNPay (`97041985...`), ngày đặt thực tế trên giao diện lịch, tài khoản nhân viên thật. |
| **Acceptance Test (UAT)** | Tập dữ liệu kịch bản nghiệp vụ thật của doanh nghiệp (mã Voucher thực tế, giá phòng thực tế, User Personas). | Xác thực mức độ thỏa mãn các nghiệp vụ (User Stories) của khách hàng/doanh nghiệp. | Khách hàng Nguyễn Văn A, đặt phòng Deluxe từ 15/07 đến 18/07, áp dụng mã `SUMMER2026` để được giảm giá 10%. |

---

## 2. Template Tài Liệu Hóa Dữ Liệu Test (Templates & Examples)

Dưới đây là các mẫu tài liệu hóa dữ liệu kiểm thử cho từng cấp độ, kèm ví dụ thực tế dựa trên nghiệp vụ của dự án **Hotel Booking System**.

---

### Mẫu 1: Tài liệu dữ liệu Unit Test (Unit Test Data)

* **Nguyên tắc:** Dữ liệu nhỏ gọn, mô phỏng các trường hợp biên (edge cases), dùng Mockito để stubbing kết quả DB.

```markdown
### [UNIT-TEST-DATA-01]: Đăng đánh giá khách sạn (ReviewServiceImplTest)

*   **Mục đích:** Xác thực nghiệp vụ cho phép khách hàng đăng đánh giá cho phòng đã hoàn thành.
*   **Trạng thái DB giả lập (Mockito Stubbing):**
    *   `bookingRepository.findById(25)` $\rightarrow$ trả về đối tượng `Booking` có:
        *   `bookingId = 25`
        *   `userId = 1` (trùng với ID user đang đăng nhập)
        *   `status = COMPLETED`
    *   `reviewRepository.existsByBookingBookingId(25)` $\rightarrow$ trả về `false` (chưa có đánh giá nào).
    *   `reviewRepository.getAverageRatingForHotel(1)` $\rightarrow$ trả về `4.5` (điểm trung bình hiện tại).
*   **Dữ liệu Input (Request DTO):**
    ```json
    {
      "bookingId": 25,
      "rating": 5,
      "comment": "Dịch vụ phòng tuyệt vời, nhân viên nhiệt tình!"
    }
    ```
*   **Kết quả kỳ vọng (Expected Output):**
    *   Hàm `createReview` chạy thành công, trả về đối tượng `Review` có `status = VISIBLE`.
    *   Điểm trung bình khách sạn được cập nhật lại trên Entity `Hotel` và lưu vào DB.
*   **Dữ liệu ca biên/lỗi (Edge Cases):**
    *   *Trường hợp 1 (Sai quyền sở hữu):* `booking.getUserId() = 99` (Khác 1) $\rightarrow$ Kỳ vọng lỗi: `AccessDeniedException`.
    *   *Trường hợp 2 (Đơn chưa hoàn thành):* `booking.getStatus() = PENDING` $\rightarrow$ Kỳ vọng lỗi: `IllegalArgumentException` ("Booking must be COMPLETED").
    *   *Trường hợp 3 (Trùng lặp):* `reviewRepository.existsByBookingBookingId(25)` trả về `true` $\rightarrow$ Kỳ vọng lỗi: `IllegalArgumentException` ("Booking already reviewed").
```

---

### Mẫu 2: Tài liệu dữ liệu Integration Test (Integration Test Data)

* **Nguyên tắc:** Dữ liệu đại diện cho payload API thực tế và trạng thái thực tế trong database trước/sau khi chạy test.

```markdown
### [INT-TEST-DATA-01]: Áp dụng Voucher Khuyến mãi (VoucherController/Service)

*   **Endpoint:** `POST /api/v1/vouchers/apply`
*   **Môi trường Cơ sở dữ liệu trước khi test (Pre-conditions DB Script):**
    *   Bảng `users`: có user `customer@example.com` (ID: 1).
    *   Bảng `bookings`: có booking `bookingId = 10`, `userId = 1`, `totalPrice = 500000` (500k VNĐ).
    *   Bảng `vouchers`: có voucher `SUMMER2026`:
        *   `code = "SUMMER2026"`
        *   `discountType = "PERCENTAGE"`
        *   `discountValue = 10` (giảm 10%)
        *   `maxUsage = 100`, `usedCount = 10`
        *   `minBookingValue = 300000` (tối thiểu 300k)
        *   `expiryDate = 2026-12-31`
*   **Dữ liệu yêu cầu gửi lên (HTTP Request Payload):**
    *   **Headers:** `Authorization: Bearer <JWT_TOKEN_CUSTOMER>`
    *   **Body:**
        ```json
        {
          "bookingId": 10,
          "voucherCode": "SUMMER2026"
        }
        ```
*   **Trạng thái Cơ sở dữ liệu kỳ vọng sau khi test (Post-conditions DB State):**
    *   Bảng `bookings`: cột `totalPrice` của booking ID 10 được cập nhật thành `450000` (giảm 50k).
    *   Bảng `vouchers`: cột `usedCount` của voucher "SUMMER2026" tăng lên thành `11`.
*   **Phản hồi API kỳ vọng (Expected Response):**
    *   **HTTP Status:** `200 OK`
    *   **Body:** `"Voucher applied successfully. New total: 450000"`
```

---

### Mẫu 3: Tài liệu dữ liệu System Test (System/End-to-End Test Data)

* **Nguyên tắc:** Dữ liệu đóng vai trò giả lập các thao tác thực tế của người dùng trên toàn bộ hệ thống từ đầu đến cuối.

```markdown
### [SYS-TEST-DATA-01]: Luồng đặt phòng & thanh toán trực tuyến qua VNPay

*   **Thông tin tài khoản kiểm thử (Actors):**
    *   Tài khoản khách hàng: `customer@hotel.com` / `Password123`
*   **Dữ liệu nhập kịch bản (Scenario Inputs):**
    1.  *Tìm kiếm phòng trống:* Địa điểm = "Hạ Long", Check-in = 2026-07-10, Check-out = 2026-07-12, Số khách = 2.
    2.  *Chọn phòng:* Chọn phòng Deluxe số 201 (Giá: 1,200,000 VND / đêm).
    3.  *Áp dụng voucher:* Nhập mã `WELCOME2026` (Giảm 100,000 VND).
    4.  *Chọn phương thức thanh toán:* Trực tuyến qua `VNPAY`.
*   **Dữ liệu giả lập Cổng thanh toán (VNPay Sandbox Credentials):**
    *   *Ngân hàng:* `NCB`
    *   *Số thẻ:* `9704198526191432185`
    *   *Tên chủ thẻ:* `NGUYEN VAN A`
    *   *Ngày phát hành:* `07/15`
    *   *Mã OTP xác thực:* `123456`
*   **Kết quả hệ thống ghi nhận kỳ vọng (Expected System State):**
    *   Lịch đặt phòng (`booking`) được chuyển trạng thái sang `CONFIRMED`.
    *   Khóa phòng tạm thời (`room_locks`) được giải phóng sạch sẽ.
    *   Hệ thống gửi thư xác nhận đặt phòng tới email `customer@hotel.com`.
    *   Bảng `payments` ghi nhận mã giao dịch `transaction_id` từ VNPay.
```

---

### Mẫu 4: Tài liệu dữ liệu Acceptance Test (Acceptance Test / UAT)

* **Nguyên tắc:** Tập trung vào câu chuyện người dùng (User Stories) để chứng minh phần mềm đáp ứng đúng thỏa thuận nghiệp vụ.

```markdown
### [ACC-TEST-DATA-01]: Quy trình điều hành dọn phòng của nhân viên buồng phòng

*   **Mô tả kịch bản (User Story):**
    *   "Là một Nhân viên buồng phòng (Housekeeper), tôi muốn cập nhật trạng thái phòng bẩn sau khi khách check-out thành phòng sạch, để phòng đó xuất hiện trên danh sách tìm kiếm của khách đặt phòng mới."
*   **Tài khoản thực hiện (Persona):**
    *   Nhân viên dọn phòng: Cô Hoa (Tài khoản: `housekeeper@hotel.com` / `Password123`, Role: `HOUSEKEEPER`).
*   **Hiện trạng ban đầu (Pre-conditions):**
    *   Phòng 302 của khách sạn Luxury Hotel vừa có khách trả phòng lúc 12:00.
    *   Lễ tân đã cập nhật trạng thái phòng sang `UNAVAILABLE` (phòng bẩn, cần dọn dẹp).
    *   Khách đặt phòng mới lên trang chủ tìm phòng trống vào ngày tiếp theo không nhìn thấy phòng 302 khả dụng.
*   **Các bước kiểm thử & Dữ liệu thực tế:**
    1.  Cô Hoa đăng nhập vào giao diện dành cho nhân viên dọn phòng.
    2.  Tại dòng hiển thị phòng 302, Cô Hoa nhấn vào nút **"Xác nhận đã dọn sạch"** (Gửi request API: `PUT /api/v1/rooms/302/availability?available=true`).
*   **Tiêu chuẩn chấp nhận (Acceptance Criteria):**
    *   Hệ thống chuyển trạng thái phòng 302 thành `AVAILABLE` (Sạch sẽ/Sẵn sàng đón khách).
    *   Khách hàng lên trang chủ tìm kiếm phòng trống cho chu kỳ tiếp theo lập tức nhìn thấy phòng 302 xuất hiện.
    *   Quyền truy cập API được kiểm soát chặt chẽ: nếu tài khoản của Khách hàng (`CUSTOMER`) cố gắng gọi API này, hệ thống phải chặn lại và báo lỗi `403 Forbidden`.
```
