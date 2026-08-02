# Kịch Bản Kiểm Thử & Dữ Liệu Test: Trạng Thái Phòng & Dọn Dẹp
# Phân hệ: com.hotelbooking.room (Cleaning & Availability Update Part)

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho phân hệ Quản lý phòng, cập nhật trạng thái dọn dẹp và phân quyền nhân viên dọn phòng/lễ tân trong hệ thống Hotel Booking.

---

## 1. Unit Test Cases (Kiểm thử Đơn vị)

### [UNIT-ROOM-01]: Cập nhật trạng thái phòng thành công (RoomServiceImpl)
*   **Mục đích:** Đảm bảo nhân viên có thể thay đổi trạng thái dọn dẹp/khả dụng của phòng sạch hoặc bẩn thành công.
*   **Trạng thái DB giả lập (Mockito Stubbing):**
    *   `roomRepository.findById(101)` $\rightarrow$ Trả về đối tượng `Room` có:
        *   `roomId = 101`, `roomNumber = "101"`, `status = AVAILABLE`.
*   **Dữ liệu Input (RoomServiceImpl.updateRoomAvailability):**
    *   `roomId = 101`
    *   `available = false` (Đánh dấu phòng bẩn hoặc đang sửa)
*   **Kết quả kỳ vọng (Expected Output):**
    *   Cột `status` của đối tượng `Room` được chuyển đổi thành `UNAVAILABLE` và lưu xuống DB.
    *   Không ném ra bất kỳ ngoại lệ nào.

### [UNIT-ROOM-02]: Ràng buộc đặt phòng trước khi khóa phòng bảo trì
*   **Mục đích:** Ngăn chặn việc khóa phòng sang trạng thái `UNAVAILABLE` / bảo trì nếu phòng đó đang nằm trong lịch check-in của một booking khác sắp tới.
*   **Trạng thái DB giả lập (Mockito Stubbing):**
    *   `bookingRepository.findActiveBookingsForRoomFromDate(101, LocalDate.now())` $\rightarrow$ Trả về danh sách chứa 1 đơn đặt phòng của ngày mai có trạng thái `CONFIRMED`.
*   **Dữ liệu Input:**
    *   `roomId = 101`, `available = false`.
*   **Kết quả kỳ vọng:**
    *   Hệ thống từ chối cập nhật và ném ra ngoại lệ `IllegalArgumentException` ("Cannot make room unavailable. Active future bookings exist for this room").

---

## 2. Integration Test Cases (Kiểm thử Tích hợp)

### [INT-ROOM-01]: Nhân viên buồng phòng cập nhật trạng thái dọn phòng (MockMvc)
*   **Endpoint:** `PUT /api/v1/rooms/101/availability?available=true`
*   **Kịch bản 1: Gọi bằng tài khoản HOUSEKEEPER**
    *   **Headers:** `Authorization: Bearer <TOKEN_HOUSEKEEPER>`
    *   **Kết quả kỳ vọng:**
        *   **HTTP Status:** `200 OK`
        *   **Response Body:** `"Room availability updated successfully"`
        *   *DB kiểm tra:* Cột `status` của phòng 101 trong DB chuyển thành `AVAILABLE`.
*   **Kịch bản 2: Gọi bằng tài khoản CUSTOMER (Sai phân quyền)**
    *   **Headers:** `Authorization: Bearer <TOKEN_CUSTOMER>`
    *   **Kết quả kỳ vọng:**
        *   **HTTP Status:** `403 Forbidden`
        *   *DB kiểm tra:* Trạng thái phòng không đổi.

---

## 3. System Test Cases (Kiểm thử Hệ thống E2E)

### [SYS-ROOM-01]: Quy trình đồng bộ trạng thái phòng từ buồng phòng lên trang tìm kiếm khách hàng
1.  **Bước 1:** Khách hàng tìm kiếm phòng cho ngày hôm nay $\rightarrow$ Hệ thống báo khách sạn Luxury hết phòng (vì phòng duy nhất còn lại là 102 đang ở trạng thái `UNAVAILABLE` - bẩn).
2.  **Bước 2:** Nhân viên dọn phòng dọn dẹp sạch sẽ phòng 102, truy cập giao diện console nhân viên buồng phòng, nhấn nút **"Xác nhận đã dọn sạch"** (Giao diện gửi request `PUT /api/v1/rooms/102/availability?available=true`).
3.  **Bước 3 (Người dùng kiểm tra):** Khách hàng nhấn lại nút Tìm kiếm phòng trống cho ngày hôm nay trên điện thoại.
    *   *Kỳ vọng:* Phòng 102 lập tức xuất hiện khả dụng với nút **"Đặt phòng"** sẵn sàng tương tác.

---

## 4. Acceptance Test Cases (Kiểm thử Chấp nhận - UAT)

### [ACC-ROOM-01]: Luồng khép kín Check-out -> Dọn phòng -> Check-in
*   **Mô tả kịch bản:** Kiểm thử luồng vận hành thực tế tại khách sạn của hai nhân viên nghiệp vụ: Lễ tân (Anh Nam) và Buồng phòng (Chị Hoa).
*   **Các bước kiểm thử:**
    1.  Khách hàng trả phòng 204. Lễ tân (Anh Nam) thực hiện Check-out đơn hàng trên phần mềm quản lý $\rightarrow$ Hệ thống tự động chuyển trạng thái phòng 204 sang `UNAVAILABLE` (phòng bẩn, cần dọn dẹp).
    2.  Hệ thống cập nhật danh sách cần dọn dẹp cho tổ buồng phòng.
    3.  Chị Hoa (Nhân viên dọn phòng) nhìn thấy phòng 204 chuyển sang màu đỏ (Cần dọn) trên máy tính bảng của mình.
    4.  Chị Hoa dọn dẹp sạch sẽ phòng, thay ga giường mới $\rightarrow$ nhấn **"Xác nhận sạch"** trên màn hình.
    5.  Anh Nam (Lễ tân) tại quầy lập tức nhìn thấy phòng 204 chuyển sang màu xanh (Sạch sẽ/Sẵn sàng đón khách mới) $\rightarrow$ Anh Nam thực hiện check-in cho một khách hàng mới vừa đến thuê trực tiếp tại quầy vào phòng 204.
*   **Tiêu chuẩn chấp nhận:**
    *   Trạng thái phòng chuyển dịch tự động: `AVAILABLE (Sạch)` $\rightarrow$ Khách check-out $\rightarrow$ `UNAVAILABLE (Bẩn)` $\rightarrow$ Dọn dẹp xong $\rightarrow$ `AVAILABLE (Sạch)`.
    *   Dữ liệu được cập nhật tức thời (Real-time) giữa giao diện của Lễ tân và Nhân viên buồng phòng.
