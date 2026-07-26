# Kịch Bản Kiểm Thử: Đặt Phòng Đoàn (>5 Phòng), Đặt Cọc 30% & Hóa Đơn Thuế CTP
# Phân hệ: com.hotelbooking.booking, com.hotelbooking.customerportal (Group Booking Engine)

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho nghiệp vụ Đặt phòng đoàn (>5 phòng), Chiết khấu 25%, Đặt cọc 30% Deposit, Đăng ký Hồ sơ Thuế Doanh nghiệp CTP xuất Hóa đơn Red VAT, và Import danh sách đoàn từ file Excel.

---

## 1. Unit Test Cases (Kiểm thử Đơn vị)

### [UNIT-GROUP-01]: Tự động áp dụng chiết khấu 25% cho đơn đặt đoàn (>= 5 phòng)
*   **Mục đích:** Đảm bảo hệ thống tự động tính toán chiết khấu 25% khi số lượng phòng đặt >= 5.
*   **Dữ liệu Input (BookingServiceImpl.calculateGroupPricing):**
    *   Số lượng phòng: `6 phòng Standard`.
    *   Giá phòng cơ bản: `$100 USD / đêm`. Số đêm: `2 đêm`.
    *   Tổng tiền gốc: `6 * 100 * 2 = $1,200 USD`.
*   **Kết quả kỳ vọng:**
    *   `discountAmount = $1,200 * 0.25 = $300 USD`.
    *   `finalPrice = $1,200 - $300 = $900 USD`.
    *   `isGroupBooking = true`.

### [UNIT-GROUP-02]: Tính toán số tiền đặt cọc 30% (30% Partial Deposit)
*   **Mục đích:** Kiểm tra tính chính xác của số tiền cọc 30% khi trưởng đoàn chọn phương thức thanh toán cọc giữ chỗ.
*   **Dữ liệu Input:**
    *   `finalPrice = $900 USD`.
    *   Loại thanh toán: `DEPOSIT_30`.
*   **Kết quả kỳ vọng:**
    *   `requiredDeposit = $900 * 0.30 = $270 USD`.
    *   `remainingBalance = $900 - $270 = $630 USD`.
    *   Trạng thái cọc: `DEPOSIT_30_PAID` sau khi chuyển khoản thành công $270 USD.

---

## 2. Integration Test Cases (Kiểm thử Tích hợp)

### [INT-GROUP-01]: Nhập Hồ sơ Thuế Doanh nghiệp CTP & Validate Mã Số Thuế (MST)
*   **Endpoint:** `POST /api/v1/customer-portal/ctp`
*   **Request Payload:**
    ```json
    {
      "bookingId": 8812,
      "companyName": "CÔNG TY TNHH CÔNG NGHỆ LUXURY STAY VIỆT NAM",
      "taxCode": "0109887766-CTP",
      "address": "Tầng 18, Tòa Keangnam Landmark 72, Hà Nội",
      "invoiceEmail": "ketoan@luxurystay.vn"
    }
    ```
*   **Phản hồi API kỳ vọng:**
    *   **HTTP Status:** `200 OK`
    *   `status = 'APPROVED'` (Hoặc `PENDING` chờ Admin phê duyệt trên màn hình SCR-408).

### [INT-GROUP-02]: Import Danh sách Thành viên Đoàn từ file Excel (.xlsx)
*   **Endpoint:** `POST /api/v1/customer-portal/group-manifest/import`
*   **Input File:** `Danh_Sach_Doan_Cong_Ty_LuxuryStay.xlsx` (10 dòng dữ liệu).
*   **Kết quả kỳ vọng:**
    *   Tự động parse 10 dòng khách gồm Họ tên, Số CMND/PassPort, Phòng gán (101, 102...).
    *   Bảng `group_member_manifests` lưu đủ 10 bản ghi đính kèm `bookingId`.

---

## 3. Acceptance Test Cases (UAT - 50 Screens Alignment)

### [ACC-GROUP-01]: Trưởng đoàn Đặt cọc 30% & Xuất Hóa đơn VAT Doanh nghiệp
*   **Actor:** Anh Hoàng (Trưởng đoàn Du lịch Doanh nghiệp).
*   **Quy trình UAT:**
    1. Truy cập màn hình `SCR-304` (`/hotels/15?tab=group`), chọn 10 phòng Standard cho 3 ngày.
    2. Giao diện tự động giảm 25% tổng tiền ($3,000 ➔ $2,250).
    3. Anh Hoàng tick chọn "Thanh toán Đặt cọc 30%" ($675 USD).
    4. Nhập Mã số thuế CTP: `0109887766-CTP`, Tên công ty.
    5. Nhấn **Xác nhận Đặt phòng Đoàn**.
*   **Tiêu chuẩn Chấp nhận:**
    *   Mã đơn `BK-GROUP-991` tạo thành công ở trạng thái `DEPOSIT_30_PAID`.
    *   10 phòng lập tức chuyển sang trạng thái tạm giữ (Room Lock) trên sơ đồ phòng Lễ tân (`SCR-204`).
    *   Hóa đơn CTP hiển thị trên màn hình Admin duyệt Hóa đơn VAT (`SCR-408`).
