# Kịch Bản Kiểm Thử & Dữ Liệu Test: Khuyến Mãi & Thanh Toán
# Phân hệ: com.hotelbooking.voucher, com.hotelbooking.payment

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho phân hệ mã giảm giá (Vouchers) và xử lý giao dịch thanh toán (Payments) của hệ thống Hotel Booking.

---

## 1. Unit Test Cases (Kiểm thử Đơn vị)

### [UNIT-PROMO-01]: Tính toán giá trị giảm giá của Voucher (VoucherServiceImpl)
*   **Mục đích:** Đảm bảo hệ thống tính toán chính xác số tiền được giảm theo hai cơ chế: Giảm theo phần trăm và giảm số tiền cố định.
*   **Dữ liệu Voucher cấu hình (Voucher entities):**
    *   *Voucher A (Phần trăm):* `code = "PROMO10"`, `discountType = PERCENTAGE`, `discountValue = 10` (Giảm 10%), `maxDiscountAmount = 50000` (Giảm tối đa 50k).
    *   *Voucher B (Cố định):* `code = "CASH100"`, `discountType = FIXED`, `discountValue = 100000` (Giảm 100k).
*   **Dữ liệu Input & Kết quả kỳ vọng:**
    *   *Kịch bản 1:* Áp dụng Voucher A cho đơn hàng `1,000,000 VND`.
        *   *Tính toán:* $10\% \times 1,000,000 = 100,000$ VND. Vì lớn hơn tối đa 50k $\rightarrow$ Số tiền giảm thực tế: `50,000 VND`.
    *   *Kịch bản 2:* Áp dụng Voucher B cho đơn hàng `300,000 VND`.
        *   *Tính toán:* Giảm cố định `100,000 VND` $\rightarrow$ Số tiền giảm thực tế: `100,000 VND`.
    *   *Kịch bản 3:* Áp dụng Voucher B cho đơn hàng `80,000 VND`.
        *   *Tính toán:* Giá trị giảm vượt quá tổng tiền $\rightarrow$ Số tiền giảm thực tế: `80,000 VND` (Đơn hàng về 0đ).

---

## 2. Integration Test Cases (Kiểm thử Tích hợp)

### [INT-PROMO-01]: Ràng buộc nghiệp vụ áp dụng Voucher (Voucher Controller)
*   **Endpoint:** `POST /api/v1/vouchers/apply`
*   **Dữ liệu DB hiện tại (Pre-conditions):**
    *   Voucher `SUMMER2026` cấu hình: `minBookingValue = 500000` (Đơn hàng tối thiểu 500k), `expiryDate = 2026-12-31`.
    *   Booking `10` có `totalPrice = 400000` (400k VND, không đủ điều kiện tối thiểu).
    *   Booking `11` có `totalPrice = 600000` (600k VND, đủ điều kiện).
*   **Kịch bản 1: Không đạt giá trị đơn hàng tối thiểu**
    *   *Input:* `bookingId = 10`, `voucherCode = "SUMMER2026"`
    *   *Kết quả kì vọng:* `400 Bad Request` ("Booking value is below minimum requirement for this voucher").
*   **Kịch bản 2: Áp dụng thành công**
    *   *Input:* `bookingId = 11`, `voucherCode = "SUMMER2026"`
    *   *Kết quả kì vọng:* `200 OK` ("Voucher applied successfully. New total: 540000").
    *   *DB kiểm tra:* `usedCount` của Voucher tăng thêm 1; `totalPrice` của Booking 11 đổi thành 540k.

---

## 3. System Test Cases (Kiểm thử Hệ thống E2E)

### [SYS-PAY-01]: Xử lý Webhook (IPN API) cập nhật trạng thái đơn hàng khi thanh toán
*   **Mục đích:** Hệ thống nhận callback ngầm từ VNPay sau khi khách hàng hoàn tất giao dịch để cập nhật trạng thái đơn hàng.
*   **Endpoint:** `POST /api/v1/payments/webhook`
*   **Môi trường Sandbox (VNPay Mock Webhook Parameters):**
    ```json
    {
      "vnp_TxnRef": "BK-20260630-94817",
      "vnp_Amount": "50000000", // 500k VNĐ (VNPay nhân 100)
      "vnp_ResponseCode": "00", // Giao dịch thành công
      "vnp_SecureHash": "abcdef123456..." // Chữ ký SHA512 hợp lệ
    }
    ```
*   **Kịch bản 1: Webhook hợp lệ**
    *   *Hành vi:* Hệ thống xác thực chữ ký SHA512 thành công.
    *   *Kết quả:* Phản hồi `200 OK` (hoặc JSON phản hồi theo chuẩn VNPay `{"RspCode":"00","Message":"Confirm Success"}`).
    *   *DB kiểm tra:* Trạng thái `Booking` liên quan chuyển sang `CONFIRMED`, `Payment` chuyển sang `SUCCESS`.
*   **Kịch bản 2: Webhook sai chữ ký (Giả mạo giao dịch)**
    *   *Input:* Đổi `vnp_SecureHash` thành chuỗi rác.
    *   *Hành vi:* Xác thực chữ ký thất bại.
    *   *Kết quả:* Phản hồi `400 Bad Request` (hoặc lỗi bảo mật). Trạng thái đơn hàng không bị thay đổi. Tạo bản ghi lỗi trong `payment_audit_logs`.
*   **Kịch bản 3: Webhook gọi trùng lặp (Duplicate Webhook Check)**
    *   *Input:* Gửi lại y hệt request của Kịch bản 1 sau khi đơn hàng đã chuyển sang `CONFIRMED`.
    *   *Kết quả:* Hệ thống kiểm tra thấy đơn hàng đã được xử lý $\rightarrow$ Bỏ qua không cập nhật lại và phản hồi thành công lập tức (Tránh ghi đè hoặc gửi email trùng lặp).

---

## 4. Acceptance Test Cases (Kiểm thử Chấp nhận - UAT)

### [ACC-PAY-01]: Hủy đơn hàng tự động và hoàn tiền (Refund Policy)
*   **User Story:** "Là khách hàng, tôi muốn có thể tự động hủy đơn đặt phòng trước thời điểm nhận phòng tối thiểu 24h và nhận lại tiền hoàn ngay lập tức vào thẻ thanh toán."
*   **Điều kiện nghiệm thu:**
    1.  Khách hàng truy cập trang cá nhân, nhấn **"Hủy phòng"** cho đơn hàng `CONFIRMED` có ngày check-in là 10/07/2026 (Thời gian thực hiện hủy: 05/07/2026, cách 5 ngày $\ge 24h$).
    2.  Hệ thống chuyển trạng thái đơn hàng sang `CANCELLED`.
    3.  Tạo bản ghi trong `payments` ghi nhận số tiền hoàn trả (`refundAmount = 100%`).
    4.  Gọi API hoàn tiền sang cổng VNPay $\rightarrow$ VNPay trả về mã hoàn tiền thành công.
