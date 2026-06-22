# Đặc tả Hệ thống (System Specification) — Hotel Booking System
**Phiên bản:** 1.0.0 | **Ngày cập nhật:** 2026-06-22 | **Trạng thái:** Hoàn chỉnh
**Phương pháp áp dụng:** Specification-Driven Development (SDD)

Tài liệu này đóng vai trò là **Nguồn Sự Thật Duy Nhất (Single Source of Truth - SSoT)** cho toàn bộ dự án Đặt phòng Khách sạn (Hotel Booking System). Tất cả mã nguồn, kiểm thử, và cấu trúc cơ sở dữ liệu đều được sinh ra và đối chiếu dựa trên tài liệu đặc tả này.

---

## 1. Giới thiệu & Mục tiêu Hệ thống (Introduction & System Goals)

Hệ thống Đặt phòng Khách sạn là một nền tảng trực tuyến cho phép:
- **Khách hàng (Customer)** tìm kiếm khách sạn, kiểm tra phòng trống theo thời gian thực, thực hiện đặt phòng, thanh toán trực tuyến, áp dụng mã giảm giá và quản lý lịch sử đặt phòng.
- **Quản trị viên (Admin)** quản lý thông tin khách sạn, quản lý danh sách phòng, kiểm duyệt đánh giá, phê duyệt thủ công các đơn đặt phòng ngoại tuyến, và khóa/mở tài khoản người dùng.
- **Giám đốc (Director)** theo dõi các báo cáo doanh thu, hiệu suất sử dụng phòng của hệ thống dưới dạng số liệu thống kê trực quan và xuất dữ liệu báo cáo ra Excel.
- **Hệ thống (System)** tự động xử lý việc tạm giữ phòng (Room Lock), tự động xác nhận đặt phòng khi nhận tín hiệu từ cổng thanh toán, và tự động xử lý/retry hoàn tiền nếu gặp sự cố.

---

## 2. Kiến trúc & Công nghệ (Architecture & Tech Stack)

Hệ thống được phát triển tuân thủ kiến trúc phân lớp chuẩn của Spring Boot:
- **Kiến trúc phân lớp:** `Controller` (REST API Endpoints) $\rightarrow$ `Service` (Business Logic Layer) $\rightarrow$ `Repository` (Data Access Layer) $\rightarrow$ `Database`.
- **Ràng buộc giao tiếp:** Mỗi lớp chỉ được phép giao tiếp trực tiếp với lớp kế cận nó. Không được phép nhúng Business Logic vào Controller. JPA Entities không được hiển thị trực tiếp qua REST Endpoints (phải thông qua DTOs).

### Công nghệ cốt lõi (Core Tech Stack)
- **Java Development Kit (JDK):** Java 17 LTS.
- **Framework:** Spring Boot 4.x.
- **Database:** Microsoft SQL Server.
- **Migration Tool:** Flyway Migration (Quản lý toàn bộ cấu trúc DB thông qua script SQL).
- **Dependency Management:** Maven.
- **Security:** Spring Security & JWT (JSON Web Token), BCrypt (độ mã hóa vòng lặp $\ge 12$).

---

## 3. Vai trò Người dùng & Phân quyền (Actors & RBAC Matrix)

Hệ thống phân quyền truy cập dựa trên vai trò (Role-Based Access Control - RBAC). Các vai trò được định nghĩa như sau:

| Vai trò (Role) | Mô tả |
| :--- | :--- |
| **Guest** | Người dùng chưa đăng nhập. Chỉ có quyền tìm kiếm, xem chi tiết khách sạn, đăng ký tài khoản, đăng nhập, và yêu cầu cấp lại mật khẩu. |
| **Customer** | Khách hàng đã đăng nhập. Có đầy đủ quyền của Guest, đồng thời có quyền tạo đặt phòng, thanh toán, hủy đặt phòng, quản lý hồ sơ cá nhân và áp dụng voucher. |
| **Staff** | Nhân viên vận hành khách sạn. Có quyền xem danh sách phòng, cập nhật trạng thái phòng, hỗ trợ đặt phòng. |
| **Admin** | Quản trị viên hệ thống. Có quyền quản lý Inventory (Khách sạn/Phòng), cấu hình thời gian tạm giữ phòng, quản lý người dùng, kiểm duyệt đánh giá và xử lý các đơn đặt phòng thủ công. |
| **Director** | Ban giám đốc. Có quyền truy cập các báo cáo doanh thu chiến lược và hiệu suất sử dụng phòng toàn hệ thống. |
| **System** | Hệ thống tự động xử lý ngầm (cron-jobs, event listeners, payment webhooks). |

### Bảng ma trận phân quyền (RBAC Matrix)

| API Path Prefix | Guest | Customer | Staff | Admin | Director | System |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| `/api/v1/auth/**` | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| `/api/v1/hotels/search` | ✔ | ✔ | ✔ | ✔ | ✔ | - |
| `/api/v1/hotels` (GET) | ✔ | ✔ | ✔ | ✔ | ✔ | - |
| `/api/v1/hotels` (POST/PUT/DELETE) | - | - | - | ✔ | - | - |
| `/api/v1/rooms/search` | ✔ | ✔ | ✔ | ✔ | ✔ | - |
| `/api/v1/rooms` (POST/PUT/DELETE) | - | - | ✔ | ✔ | - | - |
| `/api/v1/bookings/validate-dates` | ✔ | ✔ | ✔ | ✔ | ✔ | - |
| `/api/v1/bookings` (POST/GET) | - | ✔ | ✔ | ✔ | ✔ | - |
| `/api/v1/bookings/{id}/cancel` | - | ✔ | - | ✔ | - | - |
| `/api/v1/admin/**` | - | - | - | ✔ | - | - |
| `/api/v1/reports/**` | - | - | - | ✔ | ✔ | - |
| `/api/v1/payments/webhook` | ✔ | - | - | - | - | ✔ |

---

## 4. Đặc tả Cơ sở dữ liệu (Database Schema Details)

Hệ thống sử dụng SQL Server để lưu trữ dữ liệu. Cấu trúc bảng được duy trì bằng các Flyway migration scripts.

### 4.1. Sơ đồ các Bảng Dữ liệu (Tables Description)

#### 1. Bảng `users` (Quản lý người dùng)
- `user_id` (BIGINT, IDENTITY, PRIMARY KEY): Khóa chính tự tăng.
- `email` (VARCHAR(255), NOT NULL, UNIQUE): Email đăng nhập hệ thống.
- `password_hash` (VARCHAR(255), NOT NULL): Mật khẩu đã được mã hóa BCrypt (vòng lặp 12).
- `full_name` (NVARCHAR(255), NOT NULL): Họ và tên của người dùng.
- `role` (VARCHAR(50), NOT NULL): Vai trò (`CUSTOMER`, `STAFF`, `ADMIN`, `DIRECTOR`).
- `status` (VARCHAR(50), NOT NULL, DEFAULT 'ACTIVE'): Trạng thái tài khoản (`ACTIVE`, `LOCKED`, `INACTIVE`).
- `failed_login_attempts` (INT, NOT NULL, DEFAULT 0): Số lần đăng nhập sai liên tiếp.
- `last_login_at` (DATETIME2, NULL): Lần cuối cùng đăng nhập.
- `last_logout_at` (DATETIME2, NULL): Lần cuối cùng đăng xuất.
- `phone_number` (VARCHAR(20), NULL): Số điện thoại liên lạc.
- `identification_number` (VARCHAR(50), NULL): Số CMND/CCCD/Passport.
- `created_at`, `updated_at` (DATETIME2, DEFAULT CURRENT_TIMESTAMP).

#### 2. Bảng `hotels` (Thông tin khách sạn)
- `hotel_id` (BIGINT, IDENTITY, PRIMARY KEY): Khóa chính tự tăng.
- `name` (NVARCHAR(255), NOT NULL): Tên khách sạn.
- `location` (NVARCHAR(MAX), NOT NULL): Địa chỉ/vị trí địa lý.
- `description` (NVARCHAR(MAX), NULL): Mô tả về khách sạn.
- `is_active` (BIT, NOT NULL, DEFAULT 1): Trạng thái hoạt động (1: Active, 0: Soft-deleted/Inactive).
- `rating` (DECIMAL(3, 2), NULL): Điểm đánh giá trung bình từ reviews khách hàng.
- `created_at`, `updated_at` (DATETIME2).

#### 3. Bảng `hotel_images` (Bộ sưu tập hình ảnh khách sạn)
- `image_id` (BIGINT, IDENTITY, PRIMARY KEY): Khóa chính.
- `hotel_id` (BIGINT, NOT NULL, FOREIGN KEY $\rightarrow$ `hotels.hotel_id`): ID khách sạn liên quan.
- `image_url` (VARCHAR(MAX), NOT NULL): URL hoặc đường dẫn lưu trữ ảnh.
- `image_format` (VARCHAR(50), NULL): Định dạng ảnh (`jpg`, `png`, `webp`).

#### 4. Bảng `rooms` (Danh sách phòng)
- `room_id` (BIGINT, IDENTITY, PRIMARY KEY): Khóa chính.
- `hotel_id` (BIGINT, NOT NULL, FOREIGN KEY $\rightarrow$ `hotels.hotel_id`): ID khách sạn.
- `room_type` (NVARCHAR(100), NOT NULL): Loại phòng (ví dụ: Deluxe, Suite, Standard).
- `price` (DECIMAL(18,2), NOT NULL): Giá phòng một đêm (chưa giảm giá).
- `room_number` (NVARCHAR(50), NOT NULL): Số phòng thực tế (ví dụ: Room 101, Room 202).
- `status` (VARCHAR(50), NOT NULL, DEFAULT 'AVAILABLE'): Trạng thái phòng (`AVAILABLE`, `UNAVAILABLE`, `MAINTENANCE`).

#### 5. Bảng `bookings` (Thông tin đặt phòng)
- `booking_id` (BIGINT, IDENTITY, PRIMARY KEY): Khóa chính.
- `booking_code` (VARCHAR(100), NOT NULL, UNIQUE): Mã booking duy nhất sinh tự động.
- `user_id` (BIGINT, NOT NULL, FOREIGN KEY $\rightarrow$ `users.user_id`): ID khách đặt phòng.
- `hotel_id` (BIGINT, NOT NULL, FOREIGN KEY $\rightarrow$ `hotels.hotel_id`): ID khách sạn được đặt.
- `check_in_date` (DATETIME2, NOT NULL): Ngày nhận phòng.
- `check_out_date` (DATETIME2, NOT NULL): Ngày trả phòng.
- `total_amount` (DECIMAL(18,2), NOT NULL): Tổng giá trị gốc của đơn đặt phòng.
- `status` (VARCHAR(50), NOT NULL): Trạng thái booking (`PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`).
- `confirmed_at` (DATETIME2, NULL): Thời điểm xác nhận booking (online payment thành công hoặc Admin duyệt).
- `notes` (NVARCHAR(MAX), NULL): Ghi chú bổ sung từ khách hàng.
- `payment_status` (VARCHAR(50), DEFAULT 'PENDING'): Trạng thái thanh toán (`PENDING`, `PAID`, `REFUNDED`, `FAILED`).
- `voucher_id` (BIGINT, NULL, FOREIGN KEY $\rightarrow$ `vouchers.voucher_id`): Mã giảm giá được áp dụng.
- `discount_amount` (DECIMAL(18,2), DEFAULT 0): Số tiền được giảm từ voucher.
- `final_price` (DECIMAL(18,2), NOT NULL): Số tiền cuối cùng cần thanh toán (`total_amount` - `discount_amount`).

#### 6. Bảng `booking_rooms` (Liên kết phòng được đặt trong booking)
- `booking_id` (BIGINT, NOT NULL, FOREIGN KEY): Khóa ngoại $\rightarrow$ `bookings`.
- `room_id` (BIGINT, NOT NULL, FOREIGN KEY): Khóa ngoại $\rightarrow$ `rooms`.
- `quantity` (INT, NOT NULL, DEFAULT 1): Số lượng phòng.
- `price_at_booking` (DECIMAL(18,2), NOT NULL): Giá phòng được ghi nhận tại thời điểm đặt phòng.
- PRIMARY KEY (`booking_id`, `room_id`).

#### 7. Bảng `payments` (Thông tin thanh toán & hoàn tiền)
- `payment_id` (BIGINT, IDENTITY, PRIMARY KEY): Khóa chính.
- `booking_id` (BIGINT, NOT NULL, FOREIGN KEY $\rightarrow$ `bookings.booking_id`): ID đặt phòng.
- `payment_method` (VARCHAR(50), NOT NULL): Phương thức thanh toán (`ONLINE`, `CASH`, `BANK_TRANSFER`).
- `amount` (DECIMAL(18,2), NOT NULL): Số tiền giao dịch.
- `status` (VARCHAR(50), NOT NULL): Trạng thái thanh toán (`PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`).
- `transaction_id` (VARCHAR(255), UNIQUE, NULL): Mã giao dịch của cổng thanh toán.
- `gateway` (VARCHAR(50), NULL): Cổng thanh toán (ví dụ: `VNPAY`, `MOMO`, `PAYPAL`).
- `payment_time` (DATETIME2, NULL): Thời gian thanh toán thành công.
- `refund_amount` (DECIMAL(18,2), NULL): Số tiền hoàn trả khách.
- `refund_time` (DATETIME2, NULL): Thời gian thực hiện hoàn tiền thành công.
- `refund_status` (VARCHAR(50), NULL): Trạng thái hoàn tiền (`PENDING`, `SUCCESS`, `FAILED`).
- `refund_transaction_id` (VARCHAR(100), NULL): Mã giao dịch hoàn tiền từ gateway.
- `refund_retry_count` (INT, DEFAULT 0): Số lần tự động thử lại nếu hoàn tiền thất bại.

#### 8. Bảng `vouchers` (Quản lý mã giảm giá)
- `voucher_id` (BIGINT, IDENTITY, PRIMARY KEY): Khóa chính.
- `code` (VARCHAR(50), NOT NULL, UNIQUE): Mã code giảm giá (ví dụ: `SUMMER2026`).
- `discount_type` (VARCHAR(20), NOT NULL): Loại giảm giá (`PERCENTAGE`, `FIXED_AMOUNT`).
- `discount_value` (DECIMAL(18,2), NOT NULL): Giá trị giảm giá (số phần trăm hoặc số tiền cố định).
- `min_booking_value` (DECIMAL(18,2), DEFAULT 0): Giá trị booking tối thiểu để áp dụng mã.
- `start_date`, `end_date` (DATETIME2, NULL): Ngày bắt đầu và kết thúc hiệu lực của voucher.
- `max_usage` (INT, DEFAULT 0): Số lần sử dụng tối đa (0: Không giới hạn).
- `current_usage` (INT, DEFAULT 0): Số lần đã được khách áp dụng thành công.

#### 9. Bảng `room_locks` (Tạm giữ phòng trong quá trình thanh toán)
- `lock_id` (BIGINT, IDENTITY, PRIMARY KEY): Khóa chính.
- `room_id` (BIGINT, NOT NULL, FOREIGN KEY $\rightarrow$ `rooms.room_id`): ID phòng bị giữ.
- `booking_id` (BIGINT, NOT NULL, FOREIGN KEY $\rightarrow$ `bookings.booking_id`): ID booking liên kết.
- `locked_at` (DATETIME2, DEFAULT CURRENT_TIMESTAMP): Thời điểm khóa phòng.
- `expires_at` (DATETIME2, NOT NULL): Thời điểm hết hạn khóa phòng (Thường là `locked_at` + 10 phút).

#### 10. Bảng `reviews` (Đánh giá khách sạn)
- `review_id` (BIGINT, IDENTITY, PRIMARY KEY): Khóa chính.
- `user_id` (BIGINT, NOT NULL, FOREIGN KEY $\rightarrow$ `users.user_id`): ID người đánh giá.
- `hotel_id` (BIGINT, NOT NULL, FOREIGN KEY $\rightarrow$ `hotels.hotel_id`): ID khách sạn.
- `booking_id` (BIGINT, NOT NULL, UNIQUE, FOREIGN KEY $\rightarrow$ `bookings.booking_id`): 1 booking chỉ được đánh giá 1 lần.
- `rating` (INT, NOT NULL, CHECK 1-5): Điểm đánh giá (1 đến 5 sao).
- `comment` (NVARCHAR(MAX), NULL): Bình luận chi tiết.
- `status` (VARCHAR(20), NOT NULL, DEFAULT 'VISIBLE'): Trạng thái kiểm duyệt (`VISIBLE`, `HIDDEN`).
- `moderated_by` (BIGINT, NULL, FOREIGN KEY $\rightarrow$ `users.user_id`): Admin thực hiện kiểm duyệt ẩn/hiện.
- `moderated_at` (DATETIME2, NULL): Thời điểm kiểm duyệt.
- `moderation_reason` (NVARCHAR(500), NULL): Lý do ẩn review.

#### 11. Bảng `login_audit_logs` & `payment_audit_logs` (Nhật ký kiểm toán)
- Lưu thông tin kỹ thuật phục vụ debug, đối soát giao dịch và giám sát bảo mật.

---

## 5. Đặc tả Chi tiết các Phân hệ Chức năng (Functional Modules Spec)

Dưới đây là mô tả chi tiết của 6 phân hệ chức năng chứa toàn bộ 35 Nghiệp vụ của hệ thống.

---

### 5.1. Phân hệ Xác thực & Quản lý Người dùng (Authentication & Identity)

#### UC-01: Đăng ký tài khoản (Register)
- **Actor:** Guest
- **Happy Path:** Khách truy cập nhập email, password, full_name $\rightarrow$ Hệ thống kiểm tra email chưa tồn tại, password thỏa mãn $\ge 8$ ký tự $\rightarrow$ Mã hóa mật khẩu bằng BCrypt $\rightarrow$ Lưu thông tin người dùng với trạng thái `ACTIVE`, vai trò `CUSTOMER` $\rightarrow$ Trả về mã HTTP 201 Created.
- **Business Rules:**
  - Email là duy nhất, không được trùng lặp.
  - Password phải chứa ít nhất 8 ký tự, bao gồm ít nhất một chữ hoa, một chữ thường, một chữ số.
- **REST API Endpoints:**
  - `POST /api/v1/auth/register`
  - **Request Body (RegisterRequest):**
    ```json
    {
      "email": "customer@hotel.com",
      "password": "Password123",
      "fullName": "Nguyen Van A"
    }
    ```
  - **Response Body (RegisterResponse):**
    ```json
    {
      "userId": 12,
      "email": "customer@hotel.com",
      "message": "User registered successfully"
    }
    ```

#### UC-02: Đăng nhập hệ thống (Login)
- **Actor:** Guest
- **Happy Path:** Nhập email và password $\rightarrow$ Hệ thống đối chiếu dữ liệu $\rightarrow$ Đúng $\rightarrow$ Đặt lại `failed_login_attempts` về 0, ghi nhận `last_login_at`, tạo JWT access token (hạn dùng $\le 24h$) $\rightarrow$ Trả về token và thông tin user $\rightarrow$ Tạo bản ghi trong `login_audit_logs` với status `SUCCESS`.
- **Alternative/Error Flow (Khóa tài khoản):**
  - Đăng nhập sai: Cộng dồn `failed_login_attempts` lên 1. Ghi log `login_audit_logs` với status `FAILED`.
  - Nếu `failed_login_attempts` $\ge 5$: Đổi `status` của user thành `LOCKED`. Các lượt đăng nhập tiếp theo sẽ nhận HTTP 403 Forbidden kèm thông báo `"Account locked due to too many failed attempts"`.
- **REST API Endpoints:**
  - `POST /api/v1/auth/login`
  - **Request Body (LoginRequest):**
    ```json
    {
      "email": "customer@hotel.com",
      "password": "Password123"
    }
    ```
  - **Response Body (LoginResponse):**
    ```json
    {
      "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
      "tokenType": "Bearer",
      "role": "CUSTOMER",
      "email": "customer@hotel.com"
    }
    ```

#### UC-03: Đăng xuất hệ thống (Logout)
- **Actor:** Customer / Admin / Staff / Director
- **Happy Path:** Người dùng gửi request logout kèm Access Token $\rightarrow$ Hệ thống vô hiệu hóa token hiện tại bằng cách thêm vào bảng `revoked_tokens` (expiry_time khớp với hạn dùng của JWT) $\rightarrow$ Cập nhật `last_logout_at` trong bảng `users` $\rightarrow$ Trả về thông báo thành công.
- **REST API Endpoints:**
  - `POST /api/v1/auth/logout`
  - **Headers:** `Authorization: Bearer <token>`
  - **Request Body (LogoutRequest):**
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsIn..."
    }
    ```

#### UC-04 & UC-32: Quên & Đặt lại mật khẩu (Forgot & Reset Password)
- **Actor:** Guest / Customer
- **Happy Path:**
  1. Người dùng yêu cầu reset password qua email $\rightarrow$ Hệ thống sinh mã token ngẫu nhiên, lưu vào bảng `password_reset_tokens` (expiry_time là 5 phút) $\rightarrow$ Hệ thống gửi link/mã OTP qua email.
  2. Người dùng truy cập link, nhập mật khẩu mới $\rightarrow$ Hệ thống kiểm định token khớp, chưa sử dụng, chưa quá 5 phút $\rightarrow$ Cập nhật mật khẩu mới (BCrypt), đánh dấu token đã sử dụng (`used = 1`).
- **REST API Endpoints:**
  - `POST /api/v1/auth/forgot-password` (Nhận `{ "email": "customer@hotel.com" }`)
  - `POST /api/v1/auth/reset-password` (Nhận `{ "token": "uuid-string", "newPassword": "NewPassword123" }`)

#### UC-05: Cập nhật hồ sơ cá nhân
- **Actor:** Customer
- **REST API Endpoints:**
  - `PUT /api/v1/users/me/profile`
  - **Request Body:** `{ "fullName": "Nguyen Van B", "phoneNumber": "0987654321", "identificationNumber": "123456789" }`

#### UC-23: Quản lý tài khoản User
- **Actor:** Admin
- **Description:** Xem toàn bộ người dùng, cập nhật trạng thái hoạt động (Khóa/Mở khóa tài khoản).
- **REST API Endpoints:**
  - `GET /api/v1/admin/users?page=0&size=20`
  - `PATCH /api/v1/admin/users/{userId}/status` (RequestBody: `{ "status": "LOCKED" | "ACTIVE" }`)

---

### 5.2. Phân hệ Khám phá & Tra cứu (Search & Discovery)

#### UC-06 & UC-07: Tìm kiếm & Lọc khách sạn
- **Actor:** Guest / Customer
- **Description:** Khách tìm kiếm theo địa điểm (location), tên (name), sắp xếp theo giá phòng (price), điểm đánh giá (rating).
- **REST API Endpoints:**
  - `GET /api/v1/hotels/search?location=Danang&name=Hilton&minRating=4.0&page=0&size=20`

#### UC-08 & UC-09: Xem chi tiết khách sạn & Xem phòng trống
- **Actor:** Guest / Customer
- **Happy Path:** Chọn một khách sạn cụ thể $\rightarrow$ Hệ thống hiển thị đầy đủ thông tin mô tả, danh sách hình ảnh từ bảng `hotel_images` và thực hiện kiểm tra trạng thái phòng trống trong khoảng ngày đặt.
- **REST API Endpoints:**
  - `GET /api/v1/hotels/{id}` (Lấy thông tin khách sạn và bộ sưu tập ảnh).
  - `GET /api/v1/rooms/search?hotelId=1&checkIn=2026-07-01T14:00:00&checkOut=2026-07-05T12:00:00`

---

### 5.3. Phân hệ Đặt phòng (Booking Management Core)

#### UC-10: Chọn ngày check-in/check-out
- **Actor:** Guest / Customer
- **Business Rules:**
  - Ngày nhận phòng (check-in) phải lớn hơn hoặc bằng ngày hiện tại.
  - Ngày trả phòng (check-out) phải sau ngày nhận phòng ít nhất 1 ngày.
- **REST API Endpoints:**
  - `POST /api/v1/bookings/validate-dates`
  - **Request Body (DateValidationRequest):**
    ```json
    {
      "checkInDate": "2026-07-01T14:00:00",
      "checkOutDate": "2026-07-05T12:00:00"
    }
    ```
  - **Response Body (DateValidationResponse):**
    ```json
    {
      "valid": true,
      "numberOfNights": 4,
      "message": "Stay period is valid"
    }
    ```

#### UC-11: Đặt phòng khách sạn (Create Booking)
- **Actor:** Customer
- **Happy Path:** Khách gửi yêu cầu đặt phòng (danh sách `roomId` và số lượng) $\rightarrow$ Hệ thống kiểm tra tính khả dụng của phòng trống (không trùng lịch check-in/check-out với bất kỳ booking `CONFIRMED` hoặc lock room còn hiệu lực nào) $\rightarrow$ Tạo booking trạng thái `PENDING`, payment_status `PENDING` $\rightarrow$ Khởi tạo việc tạm giữ phòng (UC-33) tạo bản ghi vào `room_locks` với thời gian hết hạn mặc định là 10 phút.
- **REST API Endpoints:**
  - `POST /api/v1/bookings`
  - **Request Body (BookingRequest):**
    ```json
    {
      "hotelId": 1,
      "checkInDate": "2026-07-01T14:00:00",
      "checkOutDate": "2026-07-05T12:00:00",
      "roomIds": [1, 2],
      "notes": "Non-smoking room"
    }
    ```
  - **Response Body (BookingResponse):**
    ```json
    {
      "bookingId": 25,
      "bookingCode": "BK-20260622-94817",
      "totalAmount": 400.00,
      "finalPrice": 400.00,
      "status": "PENDING",
      "expiresAt": "2026-06-22T09:33:06"
    }
    ```

#### UC-33: Tạm giữ phòng (Room Lock)
- **Actor:** System / Customer
- **Business Rules:**
  - Khi một đơn đặt phòng được tạo ở trạng thái `PENDING`, hệ thống sẽ tự động tạo một khóa giữ phòng trong `room_locks` trong thời gian 10 phút.
  - Trong thời gian 10 phút này, không ai có thể đặt các phòng này cho cùng khoảng thời gian đặt đó.
  - **Gia hạn Khóa (Lock Renewal):** Khách có thể gửi yêu cầu gia hạn khóa phòng nếu quá trình thanh toán đang được xử lý thông qua API.
  - **Giải phóng Khóa (Lock Release):** Một scheduler chạy tự động mỗi phút (`RoomLockCleanupScheduler`) sẽ quét bảng `room_locks`. Nếu `expires_at` nhỏ hơn thời gian hiện tại và đơn đặt phòng tương ứng chưa được xác nhận thanh toán (`status = 'PENDING'`), scheduler sẽ xóa khóa phòng trong `room_locks`, đồng thời chuyển trạng thái đơn hàng sang `CANCELLED`.
- **REST API Endpoints:**
  - `PUT /api/v1/bookings/{id}/lock/renew` (Gia hạn thêm 10 phút khóa phòng).

#### UC-12: Xác nhận booking
- **Actor:** System / Admin
- **Description:** Booking được xác nhận thành công sau khi thanh toán trực tuyến thành công hoặc Admin phê duyệt đơn đặt phòng ngoại tuyến (thanh toán bằng tiền mặt/chuyển khoản ngân hàng tại quầy).
- **Happy Path (Online Callback/Webhook):** Gateway gửi tín hiệu thành công $\rightarrow$ Chuyển booking sang `status = 'CONFIRMED'`, payment_status `PAID`, `confirmed_at = CURRENT_TIMESTAMP` $\rightarrow$ Giải phóng bản ghi trong `room_locks`.
- **REST API Endpoints:**
  - `POST /api/v1/bookings/confirm` (RequestBody: `PaymentConfirmRequest` chứa `bookingCode`, `transactionId`, `amount`).
  - `PATCH /api/v1/admin/bookings/{bookingId}/status` (Dành cho Admin thay đổi thủ công trạng thái).

#### UC-14: Hủy đặt phòng (Cancel Booking)
- **Actor:** Customer / Admin
- **Business Rules:**
  - Khách hàng chỉ được phép tự hủy đặt phòng trước ngày check-in ít nhất 24 giờ.
  - Nếu đã thanh toán trực tuyến (`payment_status = 'PAID'`), việc hủy đặt phòng sẽ tự động kích hoạt tiến trình hoàn tiền (UC-34).
- **REST API Endpoints:**
  - `POST /api/v1/bookings/{id}/cancel`

#### UC-15: Xem lịch sử booking
- **Actor:** Customer
- **REST API Endpoints:**
  - `GET /api/v1/bookings/my-history?page=0&size=20` (Trả về danh sách phân trang tối đa 20 bản ghi/trang).

---

### 5.4. Phân hệ Thanh toán & Khấu trừ (Payment & Billing)

#### UC-13: Thanh toán trực tuyến
- **Actor:** Customer
- **Happy Path:** Khách hàng chọn phương thức thanh toán trực tuyến $\rightarrow$ Hệ thống gửi yêu cầu khởi tạo giao dịch tới Gateway $\rightarrow$ Nhận được URL thanh toán từ cổng giao dịch $\rightarrow$ Trả về URL cho khách hàng chuyển hướng thanh toán. Sau khi khách thanh toán thành công, cổng thanh toán gọi lại Webhook của hệ thống $\rightarrow$ Hệ thống xác minh chữ ký HMAC của Webhook để đảm bảo bảo mật và tránh trùng lặp giao dịch $\rightarrow$ Xác nhận trạng thái booking.
- **REST API Endpoints:**
  - `POST /api/v1/payments/create` (RequestBody: `PaymentRequestDTO` chứa `bookingId`, `gateway`).
  - `POST /api/v1/payments/webhook` (Webhook nhận dữ liệu phản hồi từ Gateway).

#### UC-34: Hoàn tiền booking (Refund Process)
- **Actor:** System (Event-driven)
- **Happy Path:** Đơn hàng trạng thái `PAID` bị hủy $\rightarrow$ Hệ thống tự động tạo yêu cầu hoàn tiền gửi tới Gateway $\rightarrow$ Gateway xử lý và xác nhận hoàn tiền $\rightarrow$ Hệ thống cập nhật bảng `payments` (`status = 'REFUNDED'`, ghi nhận `refund_amount`, `refund_time`, `refund_transaction_id`).
- **Alternative Path (Retry Mechanism):** Nếu cuộc gọi API hoàn tiền sang Gateway gặp lỗi mạng hoặc bị từ chối tạm thời, hệ thống sẽ lưu trạng thái hoàn tiền là `FAILED` hoặc `PENDING`. Một scheduler chạy định kỳ sẽ tự động thực hiện gửi lại yêu cầu hoàn tiền (tối đa thử lại 3 lần, tăng biến `refund_retry_count`).
- **REST API Endpoints:**
  - `POST /api/v1/payments/{bookingId}/refund` (Trigger hoàn tiền thủ công hoặc từ Event-listener).

#### UC-35: Áp dụng mã giảm giá (Voucher)
- **Actor:** Customer
- **Business Rules:**
  - Voucher phải nằm trong thời gian hiệu lực (`start_date <= CURRENT_TIMESTAMP <= end_date`).
  - Tổng tiền đơn phòng (`total_amount`) phải lớn hơn hoặc bằng `min_booking_value` của voucher.
  - Số lượng sử dụng hiện tại phải nhỏ hơn giới hạn tối đa (`current_usage < max_usage`).
- **Tính toán giảm giá:**
  - Nếu `discount_type = 'PERCENTAGE'`: `discount_amount = total_amount * (discount_value / 100)`.
  - Nếu `discount_type = 'FIXED_AMOUNT'`: `discount_amount = discount_value`.
  - Đảm bảo `discount_amount <= final_price`. Số tiền cuối cùng: `final_price = total_amount - discount_amount`.
- **REST API Endpoints:**
  - `POST /api/v1/vouchers/apply`
  - **Request Body (ApplyVoucherRequestDTO):**
    ```json
    {
      "bookingId": 25,
      "voucherCode": "SUMMER2026"
    }
    ```

---

### 5.5. Phân hệ Quản lý Kho Dữ Liệu (Inventory & Catalog)

#### UC-18, UC-19, UC-20: Thêm, Sửa, Xóa khách sạn
- **Actor:** Admin
- **Business Rules (Soft Delete):** Khi xóa khách sạn, hệ thống **không xóa vĩnh viễn** dữ liệu khỏi cơ sở dữ liệu. Thay vào đó, đặt `is_active = 0` (Soft delete) và cập nhật trạng thái tất cả phòng liên quan thành `UNAVAILABLE`.
  - Không cho phép xóa khách sạn nếu đang tồn tại bất kỳ đơn đặt phòng nào có trạng thái `CONFIRMED` hoặc `PENDING` trong tương lai.
- **REST API Endpoints:**
  - `POST /api/v1/hotels` (Tạo mới).
  - `PUT /api/v1/hotels/{id}` (Sửa).
  - `DELETE /api/v1/hotels/{id}` (Xóa mềm).

#### UC-21: Quản lý phòng (Rooms Inventory)
- **Actor:** Admin / Staff
- **Description:** Thêm phòng mới vào khách sạn, chỉnh sửa giá phòng, thay đổi trạng thái hoạt động của phòng (`AVAILABLE`, `UNAVAILABLE`, `MAINTENANCE`).
- **REST API Endpoints:**
  - `POST /api/v1/rooms`
  - `PUT /api/v1/rooms/{id}`
  - `DELETE /api/v1/rooms/{id}`

#### UC-27: Upload hình ảnh khách sạn
- **Actor:** Admin
- **Business Rules:**
  - Chỉ chấp nhận các định dạng file ảnh: `jpg`, `jpeg`, `png`, `webp`.
  - File size giới hạn tối đa 5MB mỗi ảnh.
- **REST API Endpoints:**
  - `POST /api/v1/hotels/{id}/images` (Multi-part File Upload).

---

### 5.6. Phân hệ Báo cáo & Vận hành (Reporting & Operations)

#### UC-24: Xem thống kê đặt phòng (Booking Statistics)
- **Actor:** Admin
- **REST API Endpoints:**
  - `GET /api/v1/reports/bookings/statistics?startDate=2026-06-01&endDate=2026-06-30`

#### UC-25: Xem báo cáo doanh thu (Revenue Report)
- **Actor:** Director
- **Description:** Thống kê tổng doanh thu thực tế (chỉ tính các giao dịch thanh toán thành công, trừ đi tiền hoàn trả nếu có). Báo cáo theo khoảng thời gian tùy chọn (ngày/tháng/năm).
- **REST API Endpoints:**
  - `GET /api/v1/reports/revenue?startDate=2026-01-01&endDate=2026-06-30&period=MONTH`

#### UC-26 & UC-30: Xem hiệu suất phòng & Xuất Excel báo cáo
- **Actor:** Director / Admin
- **Description:** Thống kê tỷ lệ sử dụng phòng (occupancy rate) và xuất file Excel báo cáo.
- **REST API Endpoints:**
  - `GET /api/v1/reports/room-usage?startDate=2026-06-01&endDate=2026-06-30`
  - `GET /api/v1/reports/room-usage/export?startDate=2026-06-01&endDate=2026-06-30` (Trả về file stream Excel, thời gian xử lý tải xuống $\le 5$ giây).

#### UC-31: Kiểm duyệt đánh giá (Review Moderation)
- **Actor:** Admin
- **Happy Path:** Admin quét danh sách đánh giá của khách hàng $\rightarrow$ Phát hiện bình luận vi phạm chính sách $\rightarrow$ Thực hiện ẩn đánh giá $\rightarrow$ Cập nhật đánh giá (`status = 'HIDDEN'`, lưu ID Admin tại `moderated_by`, thời gian tại `moderated_at`, và nhập lý do tại `moderation_reason`) $\rightarrow$ Điểm đánh giá trung bình (`rating`) của khách sạn liên quan sẽ tự động được tính toán lại, bỏ qua đánh giá bị ẩn này.
- **REST API Endpoints:**
  - `GET /api/v1/reports/reviews?status=ALL&page=0&size=20` (Xem danh sách kiểm duyệt).
  - `PATCH /api/v1/reports/reviews/{id}/moderate`
  - **Request Body (ModerationRequest):**
    ```json
    {
      "action": "HIDE",
      "reason": "Spam / Contains inappropriate language"
    }
    ```

---

## 6. Cơ chế Bảo mật & Xác thực (Security & Authentication)

Hệ thống tuân thủ nghiêm ngặt các quy định bảo mật nêu tại tài liệu dự án:

1. **Quản lý mật khẩu (Password Security):**
   - Không lưu trữ mật khẩu dưới dạng văn bản thô. Chỉ lưu trữ dưới dạng mã băm BCrypt.
   - Sử dụng `BCryptPasswordEncoder` với cấu hình độ an toàn (strength) bằng `12`.
2. **Xác thực API (JWT API Security):**
   - Mọi API hoạt động phi trạng thái (stateless session). Xác thực qua Header `Authorization: Bearer <JWT>`.
   - Token JWT phải có thời gian hết hạn ngắn ($\le 24$ giờ).
   - Đăng xuất bằng cơ chế lưu danh sách Token đã hủy vào bảng `revoked_tokens` để ngăn chặn việc tái sử dụng token.
3. **Cấu hình CORS & CSRF:**
   - Chỉ cho phép các domain frontend được chỉ định truy cập API (ví dụ: `http://localhost:5173`). Không sử dụng `*` cho CORS trong môi trường production.
   - CSRF được tắt vì ứng dụng sử dụng cơ chế REST API không trạng thái qua JWT.
4. **Phòng chống tấn công SQL Injection:**
   - Tuyệt đối không cộng chuỗi SQL. Toàn bộ các câu truy vấn cơ sở dữ liệu phải sử dụng Spring Data JPA, Hibernate Criteria API hoặc Parametized Queries.
5. **Quản lý thông tin mật (Secrets Management):**
   - Không được phép hardcode mật khẩu DB, JWT secret, hoặc API Key cổng thanh toán vào mã nguồn. Tất cả phải được tham chiếu qua biến môi trường thông qua cú pháp `${VARIABLE_NAME}` trong tệp `application.properties`.

---

## 7. Cơ chế Xử lý Lỗi Chuẩn hóa (Global Error Handling)

Hệ thống sử dụng `@ControllerAdvice` (`GlobalExceptionHandler`) để bắt toàn bộ các exception phát sinh trong runtime.

### Cấu trúc thông điệp lỗi chuẩn (Standard JSON Error Format)
Tất cả các API khi gặp lỗi phải trả về một JSON đồng nhất có định dạng:
```json
{
  "timestamp": "2026-06-22T09:23:06Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Chi tiết nguyên nhân gây lỗi hiển thị ở đây",
  "path": "/api/v1/bookings"
}
```

### Bản đồ ánh xạ mã HTTP lỗi (HTTP Status Mapping)

| Exception | HTTP Status | Nguyên nhân |
| :--- | :--- | :--- |
| `EntityNotFoundException` | 404 Not Found | Không tìm thấy bản ghi tương ứng trong cơ sở dữ liệu. |
| `IllegalArgumentException` | 400 Bad Request | Sai định dạng đầu vào hoặc vi phạm logic nghiệp vụ cơ bản. |
| `MethodArgumentNotValidException` | 400 Bad Request | Vi phạm các ràng buộc validate đầu vào của Jakarta Bean Validation. Trả về chi tiết lỗi của từng trường dữ liệu. |
| `AccessDeniedException` | 403 Forbidden | Người dùng đã đăng nhập nhưng không có vai trò phù hợp để truy cập tài nguyên. |
| `AuthenticationException` | 401 Unauthorized | Token không hợp lệ, hết hạn hoặc không cung cấp thông tin xác thực. |
| `Exception` (Mọi lỗi chưa được định nghĩa) | 500 Internal Error | Lỗi hệ thống server-side không mong muốn. Ẩn thông tin kỹ thuật (stack trace) khỏi API response. |

---
*Tài liệu đặc tả hệ thống kết thúc tại đây. Mọi sự thay đổi phải thông qua quy trình phê duyệt PR của dự án.*
