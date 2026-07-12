# Kịch Bản Kiểm Thử & Dữ Liệu Test: Đánh Giá & Kiểm Duyệt
# Phân hệ: com.hotelbooking.hotel (Review Part), com.hotelbooking.report (Moderation Part)

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho phân hệ Đánh giá (Reviews) của khách hàng và Kiểm duyệt (Moderation) của quản trị viên trong hệ thống Hotel Booking.

---

## 1. Unit Test Cases (Kiểm thử Đơn vị)

### [UNIT-REVIEW-01]: Đăng đánh giá thành công và cập nhật điểm khách sạn (ReviewServiceImpl)
*   **Mục đích:** Khách hàng gửi đánh giá hợp lệ sẽ lưu vào DB và tự động kích hoạt tính lại điểm rating trung bình của khách sạn.
*   **Trạng thái DB giả lập (Mockito Stubbing):**
    *   `bookingRepository.findById(15)` $\rightarrow$ Trả về đối tượng `Booking` có:
        *   `bookingId = 15`, `userId = 1` (Khớp với user đăng nhập), `status = COMPLETED`, `hotelId = 10`.
    *   `reviewRepository.existsByBookingBookingId(15)` $\rightarrow$ Trả về `false` (Chưa đánh giá).
    *   `hotelRepository.findById(10)` $\rightarrow$ Trả về đối tượng `Hotel` có ID 10, điểm rating hiện tại là `4.0`.
    *   `reviewRepository.getAverageRatingForHotel(10)` $\rightarrow$ Trả về giá trị trung bình mới là `4.5` (Sau khi đã cộng gộp điểm 5 sao mới đăng).
*   **Dữ liệu Input (ReviewServiceImpl.createReview):**
    ```json
    {
      "bookingId": 15,
      "rating": 5,
      "comment": "Khách sạn rất sạch đẹp, đồ ăn ngon!"
    }
    ```
*   **Kết quả kỳ vọng (Expected Output):**
    *   Đối tượng `Review` được lưu xuống DB có trạng thái mặc định là `VISIBLE`.
    *   Điểm rating của Entity `Hotel` (ID 10) được cập nhật thành `4.5` và lưu thành công.

### [UNIT-REVIEW-02]: Ràng buộc nghiệp vụ tạo đánh giá
*   **Mục đích:** Ngăn chặn các đánh giá không hợp lệ.
*   **Kịch bản 1: Booking chưa hoàn thành (Chưa Check-out)**
    *   *Giả lập:* `booking.getStatus()` trả về `CONFIRMED` (chưa chuyển sang `COMPLETED`).
    *   *Kết quả kì vọng:* Ném lỗi `IllegalArgumentException` ("Booking must be COMPLETED to submit a review").
*   **Kịch bản 2: Mỗi đơn đặt chỉ được đánh giá 1 lần**
    *   *Giả lập:* `reviewRepository.existsByBookingBookingId(15)` trả về `true`.
    *   *Kết quả kì vọng:* Ném lỗi `IllegalArgumentException` ("This booking has already been reviewed").

---

## 2. Integration Test Cases (Kiểm thử Tích hợp)

### [INT-REVIEW-01]: Tạo đánh giá qua REST API (MockMvc)
*   **Endpoint:** `POST /api/v1/reviews`
*   **Headers:** `Authorization: Bearer <TOKEN_CUSTOMER>`
*   **Dữ liệu Yêu cầu gửi đi (Request Payload):**
    ```json
    {
      "bookingId": 15,
      "rating": 4,
      "comment": "Phòng sạch sẽ, nhân viên phục vụ tốt."
    }
    ```
*   **Phản hồi API kỳ vọng (Expected Response):**
    *   **HTTP Status:** `210 Created`
    *   **Body:**
        ```json
        {
          "success": true,
          "message": "Review submitted successfully",
          "data": {
            "reviewId": 101,
            "customerName": "Nguyen Van A",
            "rating": 4,
            "comment": "Phòng sạch sẽ, nhân viên phục vụ tốt.",
            "status": "VISIBLE"
          }
        }
        ```

### [INT-REVIEW-02]: Admin kiểm duyệt Ẩn đánh giá (Review Moderation API)
*   **Endpoint:** `PATCH /api/v1/reports/reviews/{reviewId}/moderate`
*   **Headers:** `Authorization: Bearer <TOKEN_ADMIN>`
*   **Dữ liệu Yêu cầu gửi đi (Request Payload):**
    ```json
    {
      "action": "HIDE",
      "reason": "Chứa từ ngữ không lịch sự, thô tục."
    }
    ```
*   **Trạng thái DB thay đổi kỳ vọng (Post-conditions):**
    *   Bảng `reviews` cập nhật dòng review ID tương ứng: cột `status` chuyển thành `'HIDDEN'`, cập nhật thông tin cột `moderated_by = admin_id`, `moderated_at = current_time`, `moderation_reason = "Chứa từ ngữ..."`.
*   **Phản hồi API kỳ vọng (Expected Response):**
    *   **HTTP Status:** `200 OK`
    *   **Body:** Chứa thông tin review sau kiểm duyệt với `status = "HIDDEN"`.

---

## 3. System Test Cases (Kiểm thử Hệ thống E2E)

### [SYS-REVIEW-01]: Tự động cập nhật số sao khách sạn sau khi kiểm duyệt
1.  **Bước 1:** Khách sạn Luxury có 2 đánh giá công khai có điểm số là `5.0` và `4.0` $\rightarrow$ Điểm trung bình hiển thị là `4.5`.
2.  **Bước 2:** Đánh giá `4.0` vi phạm nội quy và bị Admin ẩn (`status` đổi thành `HIDDEN` qua trang điều hành Admin).
3.  **Bước 3 (Người dùng kiểm tra):** Khách hàng vãng lai truy cập vào trang chi tiết khách sạn Luxury.
    *   *Kỳ vọng:* Danh sách đánh giá chỉ còn hiển thị duy nhất 1 review `5.0`.
    *   *Kỳ vọng:* Số sao trung bình của khách sạn trên tiêu đề trang chi tiết cập nhật lại thành `5.0` (Do hệ thống chỉ tính điểm trung bình dựa trên các reviews có `status = 'VISIBLE'`).

---

## 4. Acceptance Test Cases (Kiểm thử Chấp nhận - UAT)

### [ACC-REVIEW-01]: Kiểm duyệt nội dung đánh giá của Admin
*   **User Story:** "Là một Quản trị viên (Admin), tôi muốn có thể ẩn đi các đánh giá thô tục, spam hoặc không đúng sự thật để đảm bảo danh tiếng của khách sạn không bị ảnh hưởng bởi các bình luận tiêu cực cố ý phá hoại."
*   **Kiểm tra tính năng:**
    1.  Admin vào danh sách kiểm duyệt đánh giá (`GET /api/v1/reports/reviews?status=ALL`).
    2.  Phát hiện đánh giá ID 12 có nội dung thô tục, quảng cáo link ngoài.
    3.  Admin nhấn nút **"Ẩn đánh giá"** và điền lý do: "Bình luận chứa link spam".
*   **Tiêu chuẩn chấp nhận:**
    *   Review ID 12 biến mất khỏi danh sách hiển thị trên trang chi tiết khách sạn của người dùng.
    *   Điểm rating trung bình của khách sạn liên quan được tính toán lại ngay lập tức.
    *   Lý do ẩn được ghi nhận vào nhật ký hệ thống để đối soát sau này.
