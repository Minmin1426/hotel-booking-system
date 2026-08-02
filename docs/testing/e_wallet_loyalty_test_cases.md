# Kịch Bản Kiểm Thử: Ví Điện Tử, Hạn Mức Chi Tiêu, Engine Hoàn Tiền & Thẻ Hội Viên Loyalty
# Phân hệ: com.hotelbooking.payment, com.hotelbooking.user (E-Wallet & Loyalty Tiers)

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho nghiệp vụ Ví Điện Tử cá nhân/đoàn, Nạp tiền trực tuyến, Hạn mức chi tiêu ngày, Engine Tự Động Hoàn Tiền Hủy Phòng, và Thẻ Hội Viên Thân Thiết Loyalty Tiers.

---

## 1. Unit Test Cases (Kiểm thử Đơn vị)

### [UNIT-WALLET-01]: Trừ tiền Ví Điện Tử & Kiểm tra Hạn mức Chi tiêu Ngày (Daily Spending Limit)
*   **Mục đích:** Đảm bảo hệ thống chặn các giao dịch chi tiêu vượt quá Hạn mức ngày đã cài đặt.
*   **Dữ liệu Input:**
    *   Số dư ví hiện tại: `$1,250.00 USD`.
    *   Hạn mức chi tiêu ngày: `$500.00 USD`. Đã chi tiêu trong ngày: `$400.00 USD`.
    *   Yêu cầu thanh toán mới: `$200.00 USD`.
*   **Kết quả kỳ vọng:**
    *   Tổng chi tiêu ngày nếu thực hiện: `$400 + $200 = $600 USD` (Vượt hạn mức `$500 USD`).
    *   Hệ thống ném lỗi `BusinessException` ("Daily spending limit exceeded. Limit: $500.00, Attempted: $600.00").
    *   Giao dịch bị từ chối, số dư ví giữ nguyên `$1,250.00 USD`.

### [UNIT-REFUND-01]: Thuật toán Engine Hoàn Tiền Hủy Phòng (Refund Policy Engine)
*   **Mục đích:** Kiểm tra tính chính xác của tỷ lệ hoàn tiền dựa trên số giờ còn lại trước giờ Check-in.
*   **Dữ liệu Input (BookingServiceImpl.calculateRefund):**
    *   Tổng giá trị đơn: `$500.00 USD`.
    *   *Trường hợp 1 (Hủy trước >72 giờ):* Tỷ lệ 100% ➔ Hoàn `$500.00 USD` vào Ví.
    *   *Trường hợp 2 (Hủy trước 24-72 giờ):* Tỷ lệ 80% ➔ Hoàn `$400.00 USD` vào Ví.
    *   *Trường hợp 3 (Hủy trước 12-24 giờ):* Tỷ lệ 50% ➔ Hoàn `$250.00 USD` vào Ví.
    *   *Trường hợp 4 (Hủy dưới <12 giờ):* Tỷ lệ 0% ➔ Không hoàn tiền (`$0 USD`).

---

## 2. Integration Test Cases (Kiểm thử Tích hợp)

### [INT-WALLET-01]: Nạp tiền vào Ví Điện Tử qua Cổng Thanh Toán Stripe / VNPay
*   **Endpoint:** `POST /api/v1/payments/wallet/topup`
*   **Payload:** `{ "amount": 500.00, "paymentMethod": "STRIPE" }`
*   **Kết quả kỳ vọng:**
    *   Trả về liên kết thanh toán `checkoutUrl`.
    *   Sau khi nhận Webhook/IPN xác nhận thanh toán thành công, số dư ví tăng từ `$750.00` lên `$1,250.00 USD`.
    *   Bảng `wallet_transactions` ghi nhận 1 dòng cộng tiền `+$500.00 USD` với mã `TXN-TOPUP-901`.

### [INT-LOYALTY-01]: Tự động nâng hạng thẻ Hội Viên Platinum VIP & Cộng điểm Thưởng
*   **Mục đích:** Tích điểm sau khi đơn đặt phòng chuyển sang `COMPLETED`.
*   **Quy tắc:** `$1 USD = 1 Point`. Đơn đặt `$1,000 USD` ➔ Cộng `1,000 Points`.
*   **Kết quả kỳ vọng:**
    *   Tổng điểm tích lũy vượt `2,000 Points` ➔ Nâng hạng từ `GOLD` lên `PLATINUM VIP`.
    *   Màn hình `SCR-104` hiển thị Badge `🥇 PLATINUM VIP` và tự động kích hoạt ưu đãi Giảm 10% cho các lần đặt tiếp theo.
