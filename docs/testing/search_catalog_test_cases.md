# Kịch Bản Kiểm Thử & Dữ Liệu Test: Danh Mục Khách Sạn & Tìm Kiếm
# Phân hệ: com.hotelbooking.hotel, com.hotelbooking.room (Catalog & Search Part)

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho phân hệ Danh mục khách sạn, phòng trống và bộ máy tìm kiếm của hệ thống Hotel Booking.

---

## 1. Unit Test Cases (Kiểm thử Đơn vị)

### [UNIT-SEARCH-01]: Tìm kiếm phòng trống theo thời gian khả dụng (RoomServiceImpl)
*   **Mục đích:** Đảm bảo hệ thống trả về chính xác danh sách phòng trống của khách sạn trong khoảng thời gian khách yêu cầu, loại trừ các phòng đã được đặt hoặc bị khóa giữ chỗ.
*   **Trạng thái DB giả lập (Mockito Stubbing):**
    *   `hotelRepository.existsById(1)` $\rightarrow$ Trả về `true`.
    *   `bookingRepository.findBookedRoomIdsForPeriod(1, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15))` $\rightarrow$ Trả về danh sách Room ID đã có khách đặt thành công: `[101, 102]`.
    *   `roomLockRepository.findLockedRoomIdsForPeriod(1, LocalDateTime.of(2026, 7, 10, 14, 0), LocalDateTime.of(2026, 7, 15, 12, 0))` $\rightarrow$ Trả về danh sách Room ID đang bị khóa giữ chỗ tạm thời: `[103]`.
    *   `roomRepository.findAllRoomsByHotelIdAndStatus(1, RoomStatus.AVAILABLE)` $\rightarrow$ Trả về danh sách toàn bộ phòng đang hoạt động sạch sẽ: `[101, 102, 103, 104, 105]`.
*   **Dữ liệu Input (Search Parameters):**
    *   `hotelId = 1`
    *   `checkIn = 2026-07-10`
    *   `checkOut = 2026-07-15`
    *   `guests = 2`
*   **Kết quả kỳ vọng (Expected Output):**
    *   Trả về danh sách chứa phòng trống khả dụng: `[104, 105]` (Vì phòng 101, 102 đã được đặt, phòng 103 đang bị khóa tạm thời).

---

## 2. Integration Test Cases (Kiểm thử Tích hợp)

### [INT-SEARCH-01]: Xem chi tiết khách sạn và danh mục phòng (MockMvc)
*   **Endpoint:** `GET /api/v1/hotels/{hotelId}`
*   **Dữ liệu DB hiện tại (Pre-conditions):**
    *   Bảng `hotels`: tồn tại khách sạn ID `1` tên `"Luxury Hotel Hạ Long"`, `rating = 4.8`, `isActive = true`.
    *   Bảng `hotel_images`: có 2 ảnh liên kết với hotel ID 1.
    *   Bảng `rooms`: có 3 phòng liên kết với hotel ID 1, gồm 2 phòng `AVAILABLE` (giá 1,000,000 VND và 1,500,000 VND) và 1 phòng `UNAVAILABLE` (phòng hỏng/đang sửa).
*   **Phản hồi API kỳ vọng (Expected Response):**
    *   **HTTP Status:** `200 OK`
    *   **JSON Body Structure:**
        ```json
        {
          "hotelId": 1,
          "name": "Luxury Hotel Hạ Long",
          "rating": 4.8,
          "images": [
            "https://cdn.example.com/h1.jpg",
            "https://cdn.example.com/h2.jpg"
          ],
          "rooms": [
            { "roomId": 101, "roomNumber": "101", "type": "STANDARD", "price": 1000000 },
            { "roomId": 102, "roomNumber": "102", "type": "DELUXE", "price": 1500000 }
          ]
        }
        ```
    *   *Lưu ý:* Phòng ở trạng thái `UNAVAILABLE` không được xuất hiện trong danh mục trả về cho khách hàng.

---

## 3. System Test Cases (Kiểm thử Hệ thống E2E)

### [SYS-SEARCH-01]: Lọc tìm kiếm khách sạn theo nhiều điều kiện kết hợp
1.  **Bước 1:** Khách hàng mở trang chủ và nhập:
    *   Địa điểm: "Hà Nội"
    *   Ngày nhận/trả phòng: 10/07/2026 đến 12/07/2026.
    *   Số lượng khách: 3 người.
    *   Khoảng giá: từ 500,000 VND đến 2,000,000 VND.
2.  **Bước 2:** Nhấn nút Tìm kiếm.
    *   *Kỳ vọng:* Hệ thống hiển thị danh sách các khách sạn tại Hà Nội có ít nhất 1 phòng trống trong khoảng thời gian trên, có giá phòng nằm trong khoảng 500k - 2tr, và phòng đó có sức chứa tối thiểu là 3 khách.
    *   *DB đối chiếu:* Câu lệnh SQL sinh ra chứa mệnh đề `WHERE location LIKE '%Hà Nội%' AND price BETWEEN 500000 AND 2000000 AND max_capacity >= 3`.

---

## 4. Acceptance Test Cases (Kiểm thử Chấp nhận - UAT)

### [ACC-SEARCH-01]: Tránh hiển thị khách sạn đã bị ẩn (Soft-delete Validation)
*   **User Story:** "Là một Quản trị viên (Admin), tôi muốn có thể ẩn (soft-delete) các khách sạn đã ngừng hợp tác để khách hàng không thể tìm kiếm thấy trên ứng dụng, nhưng vẫn giữ lại lịch sử các đơn đặt phòng của khách sạn đó cho mục đích thống kê doanh thu."
*   **Dữ liệu Test:**
    *   Khách sạn A (ID: 5) dừng hoạt động $\rightarrow$ Cột `isActive` trong bảng `hotels` cập nhật thành `false`.
    *   Khách sạn A vẫn có đơn đặt phòng lịch sử trị giá `50,000,000 VND` trong DB.
*   **Kiểm tra tính năng:**
    1.  Khách hàng gõ tìm kiếm khách sạn A trên giao diện $\rightarrow$ Hệ thống báo không tìm thấy kết quả phù hợp.
    2.  Giám đốc (`DIRECTOR`) vào trang Dashboard xem báo cáo doanh thu tháng trước $\rightarrow$ Doanh thu `50,000,000 VND` của khách sạn A vẫn được cộng gộp chính xác.
