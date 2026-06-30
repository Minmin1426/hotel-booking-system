# Kịch Bản Kiểm Thử & Dữ Liệu Test: Đặt Phòng & Khóa Phòng
# Phân hệ: com.hotelbooking.booking, com.hotelbooking.room (Locking Part)

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho phân hệ Đặt phòng và cơ chế Khóa giữ phòng tạm thời của hệ thống Hotel Booking.

---

## 1. Unit Test Cases (Kiểm thử Đơn vị)

### [UNIT-BOOKING-01]: Kiểm tra tính hợp lệ của thời gian lưu trú (Stay Period Validation)
*   **Mục đích:** Đảm bảo hệ thống từ chối các khoảng thời gian đặt phòng không hợp lệ bằng cách ném ra lỗi.
*   **Dữ liệu Input (BookingServiceImpl.createBooking):**
    *   *Trường hợp 1 (Nhận phòng ở quá khứ):*
        *   `checkIn = 2026-05-10` (Trong khi ngày hiện tại là `2026-06-30`).
        *   `checkOut = 2026-07-05`.
        *   *Kết quả kì vọng:* Ném lỗi `IllegalArgumentException` ("Check-in date cannot be in the past").
    *   *Trường hợp 2 (Trả phòng trước nhận phòng):*
        *   `checkIn = 2026-07-10`.
        *   `checkOut = 2026-07-08`.
        *   *Kết quả kì vọng:* Ném lỗi `IllegalArgumentException` ("Check-out date must be after check-in date").
    *   *Trường hợp 3 (Lưu trú dưới 1 đêm):*
        *   `checkIn = 2026-07-10`.
        *   `checkOut = 2026-07-10`.
        *   *Kết quả kì vọng:* Ném lỗi `IllegalArgumentException` ("Stay duration must be at least 1 night").

### [UNIT-BOOKING-02]: Cơ chế khóa giữ phòng tạm thời khi tạo đơn hàng
*   **Mục đích:** Khi người dùng bắt đầu quy trình thanh toán, hệ thống tự động khóa giữ phòng 10 phút để tránh người khác đặt mất.
*   **Trạng thái DB giả lập (Mockito Stubbing):**
    *   `roomLockRepository.save(any(RoomLock.class))` $\rightarrow$ Trả về đối tượng `RoomLock` mới có `lockedUntil` là ngày hiện tại cộng thêm 10 phút.
*   **Dữ liệu Output kiểm tra:**
    *   Hàm `roomLockService.createLock(roomId, bookingId)` được gọi đúng 1 lần với đúng tham số.
    *   Thời gian khóa `lockedUntil` đúng bằng `createdAt + 10 phút`.

---

## 2. Integration Test Cases (Kiểm thử Tích hợp)

### [INT-BOOKING-01]: Đặt phòng thành công và tự động tạo Khóa phòng (MockMvc)
*   **Endpoint:** `POST /api/v1/bookings`
*   **Dữ liệu DB hiện tại (Pre-conditions):**
    *   Phòng ID `101` trống, có giá `1,000,000 VND`.
*   **Dữ liệu Yêu cầu gửi đi (Request Payload):**
    ```json
    {
      "hotelId": 1,
      "roomId": 101,
      "checkIn": "2026-07-15",
      "checkOut": "2026-07-18",
      "guests": 2
    }
    ```
*   **Trạng thái DB thay đổi kỳ vọng (Post-conditions):**
    *   Bảng `bookings` xuất hiện đơn đặt hàng mới có `status = 'PENDING'`, `totalPrice = 3000000` (3 đêm).
    *   Bảng `room_locks` xuất hiện dòng khóa giữ phòng `101` liên kết với đơn hàng mới, thời hạn khóa là 10 phút kể từ thời điểm đặt.
*   **Phản hồi API kỳ vọng (Expected Response):**
    *   **HTTP Status:** `210 Created`
    *   **Body:** Chứa thông tin đơn hàng vừa tạo kèm `bookingId` và mã `bookingCode`.

### [INT-BOOKING-02]: Giải quyết tranh chấp phòng đồng thời (Race Condition)
*   **Mục đích:** Hai khách hàng gửi request đặt cùng 1 phòng, cùng 1 khoảng thời gian tại cùng một mili giây.
*   **Kiểm thử mô phỏng (Concurrency Test):**
    *   Sử dụng ThreadPool phát đồng thời 2 luồng gọi API `POST /api/v1/bookings` với cùng dữ liệu phòng `101` và ngày từ `15/07` đến `18/07`.
*   **Kết quả kỳ vọng:**
    *   Chỉ có **1 luồng duy nhất** thành công (`210 Created`) và giữ được phòng.
    *   Luồng còn lại thất bại (`400 Bad Request` hoặc `409 Conflict`) với thông báo lỗi `"Room is already booked or locked for this period"`.
    *   Bảng `bookings` chỉ lưu duy nhất 1 đơn hàng thành công, không có đơn hàng trùng lặp.

---

## 3. System Test Cases (Kiểm thử Hệ thống E2E)

### [SYS-BOOKING-01]: Tác vụ dọn dẹp khóa hết hạn chạy ngầm (RoomLockCleanupScheduler)
1.  **Bước 1:** Khách hàng đặt phòng thành công, tạo đơn đặt phòng `PENDING` và khóa phòng trong 10 phút.
2.  **Bước 2:** Khách hàng tắt trình duyệt, không thực hiện thanh toán.
3.  **Bước 3 (Giả lập thời gian trôi qua):** Đợi quá 10 phút.
4.  **Bước 4 (Hệ thống chạy ngầm):** Lớp `RoomLockCleanupScheduler` (chạy định kỳ 30 giây) thực thi câu lệnh quét:
    *   *Kỳ vọng:* Xóa dòng khóa hết hạn khỏi bảng `room_locks`.
    *   *Kỳ vọng:* Cập nhật cột `status` của đơn đặt phòng liên quan trong bảng `bookings` từ `PENDING` thành `FAILED` (hoặc `CANCELLED`).
    *   *Kiểm tra:* Phòng ngay lập tức trở lại trạng thái trống và khả dụng cho các khách hàng khác tìm kiếm.

---

## 4. Acceptance Test Cases (Kiểm thử Chấp nhận - UAT)

### [ACC-BOOKING-01]: Lễ tân tạo đơn đặt phòng trực tiếp tại quầy (Offline Booking)
*   **User Story:** "Là một Lễ tân (Receptionist), tôi muốn có thể tạo và xác nhận đơn đặt phòng trực tiếp cho khách hàng vãng lai đến thuê trực tiếp tại quầy mà không cần thông qua cổng thanh toán online."
*   **Tài khoản thực hiện (Persona):** Nhân viên lễ tân: Anh Nam (Tài khoản: `receptionist@hotel.com`, Role: `RECEPTIONIST`).
*   **Các bước thực hiện:**
    1.  Anh Nam đăng nhập cổng nhân viên, chọn phòng `202` trống theo yêu cầu của khách.
    2.  Nhập thông tin khách hàng, số CMND/CCCD, ngày lưu trú.
    3.  Nhấn nút **"Tạo đơn đặt trực tiếp"** (Gửi request API: `POST /api/v1/admin/bookings` với thông tin thanh toán tiền mặt offline).
    4.  Anh Nam xác nhận đã thu tiền mặt $\rightarrow$ chuyển trạng thái đơn hàng sang `CONFIRMED`.
*   **Tiêu chuẩn chấp nhận:**
    *   Đơn hàng tạo thành công trực tiếp ở trạng thái `CONFIRMED`.
    *   Phòng 202 bị khóa trên lịch để không cho khách đặt online đặt trùng.
    *   Hệ thống không sinh mã liên kết thanh toán sang VNPay.
