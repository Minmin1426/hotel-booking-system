# Tài liệu Đặc tả REST API (REST API Specification Document)
**Dự án:** Hotel Booking System
**Địa chỉ gốc (Base URL):** `/api/v1`
**Định dạng dữ liệu:** `application/json`
**Trạng thái:** Hoàn chỉnh
**Quy trình quản lý:** Specification-Driven Development (SDD)

Tài liệu này cung cấp đầy đủ thông tin về các cổng kết nối REST API của hệ thống Đặt phòng Khách sạn, bao gồm các tham số đầu vào, định dạng payload, quyền truy cập và mã lỗi phản hồi.

---

## 1. Định dạng Phản hồi Chuẩn hóa (Standard Response Formats)

Hệ thống bọc toàn bộ dữ liệu phản hồi API thành một cấu trúc JSON thống nhất để đơn giản hóa quá trình xử lý ở phía Client.

### 1.1. Phản hồi thành công (Standard Success Response)
Đối với các thao tác thành công, dữ liệu được bọc trong đối tượng `ApiResponse`:
```json
{
  "success": true,
  "message": "Thông điệp mô tả kết quả xử lý thành công",
  "data": {
    // Payload kết quả (Object hoặc Array)
  }
}
```

### 1.2. Phản hồi lỗi (Standard Error Response)
Khi hệ thống phát hiện lỗi (lỗi nghiệp vụ, xác thực hoặc lỗi hệ thống), phản hồi sẽ trả về định dạng lỗi chuẩn hóa:
```json
{
  "timestamp": "2026-06-22T09:23:06Z",
  "status": 400,
  "error": "Tên mã lỗi (ví dụ: Bad Request)",
  "message": "Mô tả chi tiết nguyên nhân gây lỗi",
  "path": "Đường dẫn API gây lỗi (ví dụ: /api/v1/bookings)"
}
```

---

## 2. Danh mục API theo Phân hệ (API Endpoint Directory)

---

### 2.1. Phân hệ Xác thực & Đăng nhập (`/auth`)

#### 1. Đăng ký tài khoản mới (Register)
- **HTTP Method & Path:** `POST /auth/register`
- **Vai trò cho phép:** Public (Guest)
- **Mô tả:** Đăng ký tài khoản khách hàng mới.
- **Request Body:**
  ```json
  {
    "email": "customer@hotel.com",       // Bắt buộc, định dạng email
    "password": "Password123",           // Bắt buộc, tối thiểu 8 ký tự
    "fullName": "Nguyen Van A"           // Bắt buộc
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "success": true,
    "message": "User registered successfully",
    "data": {
      "userId": 12,
      "email": "customer@hotel.com"
    }
  }
  ```

#### 2. Đăng nhập hệ thống (Login)
- **HTTP Method & Path:** `POST /auth/login`
- **Vai trò cho phép:** Public (Guest)
- **Mô tả:** Nhận JWT Token khi đăng nhập bằng email và mật khẩu.
- **Request Body:**
  ```json
  {
    "email": "customer@hotel.com",
    "password": "Password123"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Login successful",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
      "tokenType": "Bearer",
      "role": "CUSTOMER",
      "email": "customer@hotel.com"
    }
  }
  ```

#### 3. Đăng xuất hệ thống (Logout)
- **HTTP Method & Path:** `POST /auth/logout`
- **Vai trò cho phép:** Yêu cầu đăng nhập (Authorization Token)
- **Mô tả:** Đăng xuất tài khoản và đưa token hiện tại vào danh sách đen (Blacklist).
- **Headers:** `Authorization: Bearer <JWT_Token>`
- **Request Body:**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsIn..."  // Access Token cần thu hồi
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Logout successful",
    "data": null
  }
  ```

#### 4. Quên mật khẩu (Forgot Password)
- **HTTP Method & Path:** `POST /auth/forgot-password`
- **Vai trò cho phép:** Public
- **Request Body:**
  ```json
  {
    "email": "customer@hotel.com"
  }
  ```
- **Response (200 OK):** Trả về thông báo đã gửi OTP/link đặt lại mật khẩu qua email.

#### 5. Đặt lại mật khẩu (Reset Password)
- **HTTP Method & Path:** `POST /auth/reset-password`
- **Vai trò cho phép:** Public
- **Request Body:**
  ```json
  {
    "token": "reset-token-uuid",
    "newPassword": "NewPassword123"
  }
  ```
- **Response (200 OK):** Trả về thông báo cập nhật mật khẩu thành công.

---

### 2.2. Phân hệ Quản lý Hồ sơ (`/users`)

#### 1. Lấy thông tin cá nhân (Profile)
- **HTTP Method & Path:** `GET /users/me/profile`
- **Vai trò cho phép:** Đã đăng nhập (`CUSTOMER`, `STAFF`, `ADMIN`, `DIRECTOR`)
- **Headers:** `Authorization: Bearer <JWT_Token>`
- **Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Profile retrieved successfully",
    "data": {
      "userId": 12,
      "email": "customer@hotel.com",
      "fullName": "Nguyen Van A",
      "role": "CUSTOMER",
      "phoneNumber": "0987654321",
      "identificationNumber": "123456789"
    }
  }
  ```

#### 2. Cập nhật hồ sơ cá nhân
- **HTTP Method & Path:** `PUT /users/me/profile`
- **Request Body:**
  ```json
  {
    "fullName": "Nguyen Van B",
    "phoneNumber": "0987654321",
    "identificationNumber": "123456789"
  }
  ```
- **Response (200 OK):** Trả về thông tin cá nhân sau cập nhật.

---

### 2.3. Phân hệ Khám phá Khách sạn & Phòng (`/hotels`, `/rooms`)

#### 1. Lọc và Tìm kiếm Khách sạn
- **HTTP Method & Path:** `GET /hotels/search`
- **Vai trò cho phép:** Public
- **Query Parameters:**
  - `location` (String, Optional): Vị trí cần tìm (ví dụ: `Danang`)
  - `name` (String, Optional): Tên khách sạn cần tìm
  - `minRating` (Double, Optional): Điểm đánh giá tối thiểu (ví dụ: `4.0`)
  - `page` (Int, Default: 0): Số thứ tự trang
  - `size` (Int, Default: 20): Số lượng bản ghi/trang
- **Response (200 OK):** Danh sách khách sạn phân trang thỏa mãn điều kiện.

#### 2. Xem chi tiết khách sạn
- **HTTP Method & Path:** `GET /hotels/{id}`
- **Vai trò cho phép:** Public
- **Response (200 OK):** Chi tiết khách sạn bao gồm mô tả, điểm rating trung bình, và danh sách ảnh (`hotel_images`).

#### 3. Tìm phòng trống khả dụng (Room Search)
- **HTTP Method & Path:** `GET /rooms/search`
- **Vai trò cho phép:** Public
- **Query Parameters:**
  - `hotelId` (Long, Required): ID khách sạn
  - `checkIn` (DateTime, Required, ISO Format: `yyyy-MM-ddTHH:mm:ss`): Ngày nhận phòng
  - `checkOut` (DateTime, Required, ISO Format): Ngày trả phòng
- **Response (200 OK):** Trả về danh sách các phòng khả dụng (không bị trùng lịch đặt confirmed hoặc giữ chỗ room lock).

---

### 2.4. Phân hệ Nghiệp vụ Đặt phòng (`/bookings`)

#### 1. Kiểm tra tính hợp lệ của ngày đặt (Validate Dates)
- **HTTP Method & Path:** `POST /bookings/validate-dates`
- **Vai trò cho phép:** Public
- **Request Body:**
  ```json
  {
    "checkInDate": "2026-07-01T14:00:00",
    "checkOutDate": "2026-07-05T12:00:00"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Date validation completed",
    "data": {
      "valid": true,
      "numberOfNights": 4,
      "message": "Stay period is valid"
    }
  }
  ```

#### 2. Tạo đơn đặt phòng & Khóa giữ phòng (Create Booking)
- **HTTP Method & Path:** `POST /bookings`
- **Vai trò cho phép:** Khách hàng (`CUSTOMER`, `STAFF`, `ADMIN`, `DIRECTOR`)
- **Headers:** `Authorization: Bearer <JWT_Token>`
- **Request Body:**
  ```json
  {
    "hotelId": 1,
    "checkInDate": "2026-07-01T14:00:00",
    "checkOutDate": "2026-07-05T12:00:00",
    "roomIds": [1, 2],
    "notes": "Yêu cầu phòng không hút thuốc"
  }
  ```
- **Response (210 Created):**
  ```json
  {
    "success": true,
    "message": "Booking created and room(s) locked successfully",
    "data": {
      "bookingId": 25,
      "bookingCode": "BK-20260622-94817",
      "totalAmount": 400.00,
      "finalPrice": 400.00,
      "status": "PENDING",
      "expiresAt": "2026-06-22T09:33:06"  // Hết hạn giữ phòng sau 10 phút
    }
  }
  ```

#### 3. Gia hạn thời gian khóa giữ phòng (Renew Room Lock)
- **HTTP Method & Path:** `PUT /bookings/{id}/lock/renew`
- **Vai trò cho phép:** `CUSTOMER`, `STAFF`, `ADMIN`, `DIRECTOR`
- **Mô tả:** Gia hạn giữ phòng thêm 10 phút để người dùng thanh toán.
- **Response (200 OK):** Trả về thông báo gia hạn khóa thành công.

#### 4. Xem lịch sử đặt phòng cá nhân
- **HTTP Method & Path:** `GET /bookings/my-history`
- **Vai trò cho phép:** `CUSTOMER`, `ADMIN`
- **Query Parameters:** `page` (default 0), `size` (default 20)
- **Response (200 OK):** Danh sách lịch sử đặt phòng dạng phân trang.

#### 5. Khách hàng hủy đặt phòng
- **HTTP Method & Path:** `POST /bookings/{id}/cancel`
- **Vai trò cho phép:** `CUSTOMER`, `ADMIN`
- **Response (200 OK):** Trả về thông báo hủy đặt phòng thành công và thông tin hoàn tiền tự động (nếu có).

---

### 2.5. Phân hệ Thanh toán & Khuyến mãi (`/payments`, `/vouchers`)

#### 1. Áp dụng mã giảm giá (Voucher)
- **HTTP Method & Path:** `POST /vouchers/apply`
- **Vai trò cho phép:** `CUSTOMER`, `STAFF`, `ADMIN`, `DIRECTOR`, `RECEPTIONIST`
- **Request Body:**
  ```json
  {
    "bookingId": 25,
    "voucherCode": "SUMMER2026"
  }
  ```
- **Response (200 OK):**
  - Trả về thông điệp: `"Voucher applied successfully. New total: <finalPrice>"`

#### 2. Khởi tạo thanh toán trực tuyến
- **HTTP Method & Path:** `POST /payments/create`
- **Vai trò cho phép:** `CUSTOMER`, `STAFF`, `ADMIN`, `DIRECTOR`, `RECEPTIONIST`
- **Request Body:**
  ```json
  {
    "bookingId": 25,
    "gateway": "VNPAY"
  }
  ```
- **Response (200 OK):** Chứa liên kết URL chuyển hướng người dùng tới cổng thanh toán.

#### 3. Nhận dữ liệu Webhook từ Gateway (Payment Webhook)
- **HTTP Method & Path:** `POST /payments/webhook`
- **Vai trò cho phép:** Public (Chỉ IP của Cổng thanh toán gọi hoặc System)
- **Mô tả:** Nhận thông tin đối soát giao dịch để tự động xác nhận đơn phòng.
- **Response (200 OK):** Trả về trạng thái phản hồi chuẩn (thường để gateway xác nhận không cần gửi lại).

---

### 2.6. Phân hệ Quản trị & Báo cáo (`/admin`, `/reports`)

#### 1. Quản lý Đặt phòng (Admin Booking Search & Operations)
- **HTTP Method & Path:** `GET /admin/bookings`
- **Vai trò cho phép:** `ADMIN`, `RECEPTIONIST`
- **Response (200 OK):** Danh sách đặt phòng toàn hệ thống.

- **HTTP Method & Path (Admin Create Booking):** `POST /admin/bookings`
- **Vai trò cho phép:** `ADMIN`, `RECEPTIONIST`
- **Request Body (AdminCreateBookingRequest):** Đặt phòng thay cho khách hàng.

- **HTTP Method & Path (Admin Update Booking):** `PUT /admin/bookings/{bookingId}`
- **Vai trò cho phép:** `ADMIN`, `RECEPTIONIST`

- **HTTP Method & Path (Admin Delete Booking):** `DELETE /admin/bookings/{bookingId}`
- **Vai trò cho phép:** `ADMIN` (Chỉ Admin mới có quyền xóa vật lý booking).

#### 2. Cập nhật thủ công trạng thái đơn đặt phòng
- **HTTP Method & Path:** `PATCH /admin/bookings/{bookingId}/status`
- **Vai trò cho phép:** `ADMIN`, `RECEPTIONIST`
- **Request Body:** `{ "status": "CONFIRMED" | "CANCELLED" }`
- **Response (200 OK):** Trạng thái booking sau khi xử lý thủ công (phục vụ khách đặt tại quầy).

#### 3. Báo cáo Thống kê đặt phòng hàng ngày
- **HTTP Method & Path:** `GET /reports/bookings/statistics`
- **Vai trò cho phép:** `ADMIN`
- **Query Parameters:** `startDate` và `endDate` (định dạng `yyyy-MM-dd`)
- **Response (200 OK):** Thống kê số lượng đơn hàng theo ngày.

#### 4. Báo cáo Doanh thu chiến lược (Strategic Revenue)
- **HTTP Method & Path:** `GET /reports/revenue`
- **Vai trò cho phép:** `DIRECTOR`
- **Query Parameters:** `startDate`, `endDate`, `period` (`DAY` | `MONTH` | `YEAR`)
- **Response (200 OK):** Biểu đồ/Danh sách tổng tiền doanh thu thực nhận đã trừ hoàn tiền.

#### 5. Báo cáo Hiệu suất sử dụng phòng & Xuất Excel
- **HTTP Method & Path:** `GET /reports/room-usage`
- **HTTP Method & Path (Export Excel):** `GET /reports/room-usage/export`
- **Vai trò cho phép:** `DIRECTOR` hoặc `ADMIN`
- **Response (200 OK):**
  - API room-usage trả về cấu trúc JSON tỷ lệ occupancy.
  - API export trả về file binary stream (Excel file) tải xuống.

#### 6. Lấy danh sách kiểm duyệt reviews
- **HTTP Method & Path:** `GET /reports/reviews`
- **Vai trò cho phép:** `ADMIN`
- **Query Parameters:** `status` (`ALL` | `VISIBLE` | `HIDDEN`), `page`, `size`
- **Response (200 OK):** Danh sách reviews cần kiểm duyệt phân trang.

#### 7. Thực hiện kiểm duyệt Ẩn/Hiện đánh giá
- **HTTP Method & Path:** `PATCH /reports/reviews/{id}/moderate`
- **Vai trò cho phép:** `ADMIN`
- **Request Body:**
  ```json
  {
    "action": "HIDE",     // HIDE hoặc SHOW
    "reason": "Chứa bình luận thô tục, không đúng sự thật"
  }
  ```
- **Response (200 OK):** Trả về thông tin review sau kiểm duyệt.

---

### 2.7. Phân hệ Buồng phòng & Đánh giá của Khách hàng (`/rooms`, `/reviews`)

#### 1. Cập nhật trạng thái dọn dẹp phòng (Staff Room Status Update)
- **HTTP Method & Path:** `PUT /rooms/{id}/availability`
- **Vai trò cho phép:** `ADMIN`, `RECEPTIONIST`, `HOUSEKEEPER`
- **Query Parameters:** `available` (boolean, `true` để mở phòng sạch, `false` để đánh dấu phòng bẩn/đang sửa)
- **Response (200 OK):** Cập nhật trạng thái thành công.

#### 2. Lấy toàn bộ danh sách phòng của khách sạn (Staff Room Catalog)
- **HTTP Method & Path:** `GET /rooms/hotel/{hotelId}`
- **Vai trò cho phép:** `ADMIN`, `STAFF`, `RECEPTIONIST`, `HOUSEKEEPER`
- **Response (200 OK):** Trả về danh sách tất cả các phòng thuộc khách sạn phục vụ phân phối/dọn dẹp.

#### 3. Khách hàng đăng đánh giá khách sạn (Create Review)
- **HTTP Method & Path:** `POST /reviews`
- **Vai trò cho phép:** Yêu cầu đăng nhập (`CUSTOMER`, etc.)
- **Request Body:**
  ```json
  {
    "bookingId": 25,
    "rating": 5,
    "comment": "Dịch vụ phòng rất tuyệt vời!"
  }
  ```
- **Response (210 Created):**
  ```json
  {
    "success": true,
    "message": "Review submitted successfully",
    "data": {
      "reviewId": 1,
      "customerName": "Nguyen Van A",
      "customerEmail": "customer@hotel.com",
      "hotelName": "Luxury Hotel",
      "bookingCode": "BK-20260622-94817",
      "rating": 5,
      "comment": "Dịch vụ phòng rất tuyệt vời!",
      "status": "VISIBLE",
      "createdAt": "2026-06-29T21:20:00"
    }
  }
  ```

#### 4. Xem danh sách đánh giá của khách sạn (Get Hotel Reviews)
- **HTTP Method & Path:** `GET /hotels/{hotelId}/reviews`
- **Vai trò cho phép:** Public
- **Query Parameters:** `page` (mặc định 0), `size` (mặc định 10)
- **Response (200 OK):** Paged response chứa danh sách đánh giá có trạng thái `VISIBLE`.
