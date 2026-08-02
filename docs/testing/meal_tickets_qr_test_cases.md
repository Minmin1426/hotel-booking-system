# Kịch Bản Kiểm Thử: Vé Ăn Buffet, Mã QR Code Suất Ăn & Kiểm Toán QR Scanner
# Phân hệ: com.hotelbooking.customerportal (Meal Tickets & QR Audit Log)

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho nghiệp vụ Mua gói vé ăn lẻ/theo đoàn, Sinh mã QR Code điện tử cho từng suất ăn, và Kiểm toán nhà hàng quét mã QR Code tại quầy Buffet real-time.

---

## 1. Unit Test Cases (Kiểm thử Đơn vị)

### [UNIT-MEAL-01]: Sinh mã Token QR Code độc nhất cho từng Vé ăn (QR Token Generation)
*   **Mục đích:** Đảm bảo mỗi vé ăn Buffet sáng/tối có một mã QR Code độc nhất không bị trùng lặp.
*   **Dữ liệu Input:**
    *   `bookingId = 9918`.
    *   Số lượng vé mua: `3 vé Buffet Sáng High-Class`.
*   **Kết quả kỳ vọng:**
    *   Tạo 3 bản ghi trong bảng `meal_tickets`.
    *   Mỗi bản ghi có `qrCode` dạng `TICKET-QR-889123`, `TICKET-QR-889124`, `TICKET-QR-889125`.
    *   Trạng thái ban đầu: `status = 'UNUSED'`.

---

## 2. Integration Test Cases (Kiểm thử Tích hợp)

### [INT-MEAL-01]: Nhân viên Nhà hàng quét mã QR Vé ăn tại quầy Buffet (QR Scanner Validation)
*   **Endpoint:** `POST /api/v1/customer-portal/meal-tickets/scan`
*   **Payload:** `{ "qrCode": "TICKET-QR-889123", "restaurantId": 5 }`
*   **Xử lý Backend:**
    1. Kiểm tra mã `TICKET-QR-889123` có tồn tại trong cơ sở dữ liệu không.
    2. Kiểm tra trạng thái: Nếu `status == 'USED'` ➔ Ném lỗi `BusinessException` ("Mã QR vé ăn này đã được sử dụng lúc 07:15:22").
    3. Nếu `status == 'UNUSED'` ➔ Chuyển trạng thái sang `USED`, lưu `scannedAt = NOW()`.
    4. Ghi 1 bản ghi vào bảng `qr_scan_audits` phục vụ kiểm toán đối soát doanh thu nhà hàng.
*   **Phản hồi API kỳ vọng:**
    *   **HTTP Status:** `200 OK`
    *   `message = "Xác thực vé ăn thành công! Khách hàng: Nguyễn Nhật Minh - Suất Buffet Sáng High-Class"`.

---

## 3. Acceptance Test Cases (UAT - 50 Screens Alignment)

### [ACC-MEAL-01]: Khách hàng dùng Vé ăn QR trên Mobile & Nhân viên quét thành công
*   **Actor:** Khách hàng Nguyễn Nhật Minh & Nhân viên Nhà hàng.
*   **Quy trình UAT:**
    1. Anh Minh mở màn hình Kho Vé Ăn `SCR-107` (`/profile?tab=mealtickets`).
    2. Nhấn xem mã QR Code của vé `TICKET-QR-889123`.
    3. Nhân viên nhà hàng mở ứng dụng trên màn hình `SCR-207` (`/staff/rooms`), nhấn nút **"📷 Quét Mã QR Vé Ăn"**.
    4. Quét mã QR từ màn hình điện thoại của Anh Minh.
*   **Tiêu chuẩn Chấp nhận:**
    *   Giao diện nhà hàng phát tiếng bíp báo thành công và hiển thị Badge xanh `✓ VÉ HỢP LỆ`.
    *   Vé ăn trên máy Anh Minh lập tức chuyển sang trạng thái `ĐÃ SỬ DỤNG (USED)`.
    *   Nhật ký kiểm toán xuất hiện trên Dashboard Admin `SCR-207` / `SCR-409`.
