# Kịch Bản Kiểm Thử & Dữ Liệu Test: Xác Thực & Người Dùng
# Phân hệ: com.hotelbooking.auth, com.hotelbooking.user

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho phân hệ Xác thực, Bảo mật và Quản lý Người dùng của hệ thống Hotel Booking.

---

## 1. Unit Test Cases (Kiểm thử Đơn vị)

### [UNIT-AUTH-01]: Đăng nhập thành công (AuthServiceImpl)
*   **Mục đích:** Xác thực người dùng đăng nhập đúng tài khoản thì nhận được JWT Token.
*   **Trạng thái DB giả lập (Mockito Stubbing):**
    *   `userRepository.findByEmail("customer@hotel.com")` $\rightarrow$ Trả về đối tượng `User` có:
        *   `userId = 1`, `email = "customer@hotel.com"`
        *   `passwordHash = "$2a$12$..."` (Băm của "Password123")
        *   `role = CUSTOMER`, `status = ACTIVE`
        *   `failedLoginAttempts = 0`
    *   `passwordEncoder.matches("Password123", "$2a$12$...")` $\rightarrow$ Trả về `true`.
*   **Dữ liệu Input (LoginRequest):**
    ```json
    {
      "email": "customer@hotel.com",
      "password": "Password123"
    }
    ```
*   **Kết quả kỳ vọng (Expected Output):**
    *   Trả về đối tượng `LoginResponse` chứa:
        *   `accessToken` (chuỗi JWT hợp lệ, hạn dùng 24h).
        *   `role = "CUSTOMER"`, `email = "customer@hotel.com"`.
    *   `failedLoginAttempts` không bị thay đổi.

### [UNIT-AUTH-02]: Tăng số lần đăng nhập sai và khóa tài khoản
*   **Mục đích:** Khóa tài khoản (`status` chuyển sang `LOCKED`) khi đăng nhập sai mật khẩu liên tục quá 5 lần.
*   **Trạng thái DB giả lập (Mockito Stubbing):**
    *   `userRepository.findByEmail("customer@hotel.com")` $\rightarrow$ Trả về đối tượng `User` có:
        *   `userId = 1`, `email = "customer@hotel.com"`, `status = ACTIVE`
        *   `failedLoginAttempts = 4` (Đã sai 4 lần trước đó).
    *   `passwordEncoder.matches("WrongPassword", ...)` $\rightarrow$ Trả về `false`.
*   **Dữ liệu Input (LoginRequest):**
    ```json
    {
      "email": "customer@hotel.com",
      "password": "WrongPassword"
    }
    ```
*   **Kết quả kỳ vọng (Expected Output):**
    *   Ném ra ngoại lệ `AuthenticationException` ("Invalid credentials").
    *   Cột `failedLoginAttempts` tăng lên thành `5`.
    *   Trạng thái `status` của User tự động cập nhật thành `LOCKED` trong DB.

---

## 2. Integration Test Cases (Kiểm thử Tích hợp)

### [INT-AUTH-01]: Đăng ký tài khoản mới - Trùng lặp Email (MockMvc)
*   **Endpoint:** `POST /api/v1/auth/register`
*   **Dữ liệu DB hiện tại (Pre-conditions):**
    *   Bảng `users` đã tồn tại bản ghi có email `exist@hotel.com`.
*   **Dữ liệu Yêu cầu gửi đi (Request Payload):**
    ```json
    {
      "email": "exist@hotel.com",
      "password": "NewPassword123",
      "fullName": "Nguyen Van A"
    }
    ```
*   **Phản hồi API kỳ vọng (Expected Response):**
    *   **HTTP Status:** `400 Bad Request`
    *   **Body:**
        ```json
        {
          "status": 400,
          "error": "Bad Request",
          "message": "Email already registered",
          "path": "/api/v1/auth/register"
        }
        ```

### [INT-AUTH-02]: Kiểm tra quyền hạn Endpoint bảo mật (RBAC JWT Filter)
*   **Endpoint:** `GET /api/v1/admin/bookings`
*   **Trường hợp 1: Không gửi Token**
    *   **Headers:** Không có `Authorization`.
    *   **HTTP Response:** `401 Unauthorized`.
*   **Trường hợp 2: Gửi Token của CUSTOMER**
    *   **Headers:** `Authorization: Bearer <TOKEN_CUSTOMER>`
    *   **HTTP Response:** `403 Forbidden` (Chỉ cho phép `ADMIN` hoặc `RECEPTIONIST`).

---

## 3. System Test Cases (Kiểm thử Hệ thống E2E)

### [SYS-AUTH-01]: Đăng xuất và thu hồi Token (Token Blacklisting)
1.  **Bước 1:** Khách hàng gửi request đăng nhập thành công, nhận được `accessToken`.
2.  **Bước 2:** Gửi request đăng xuất lên `POST /api/v1/auth/logout` với header `Authorization: Bearer <TOKEN>`.
    *   *Kỳ vọng:* Hệ thống phản hồi `200 OK` ("Logged out successfully").
    *   *DB kiểm tra:* Bảng `revoked_tokens` xuất hiện dòng ghi lại mã băm/chữ ký của token vừa sử dụng.
3.  **Bước 3:** Tiếp tục dùng lại `TOKEN` đó để gọi API lấy thông tin cá nhân `GET /api/v1/users/me`.
    *   *Kỳ vọng:* Hệ thống trả về `401 Unauthorized` do Token đã bị cho vào danh sách đen (Blacklist).

---

## 4. Acceptance Test Cases (Kiểm thử Chấp nhận - UAT)

### [ACC-AUTH-01]: Phân quyền giao diện theo vai trò người dùng (Staff Portal Access)
*   **User Story:** "Là nhân viên dọn phòng (Housekeeper), tôi muốn chỉ được phép truy cập vào các tính năng dọn dẹp phòng và bị chặn khỏi tất cả các trang quản lý doanh thu hoặc đặt phòng để đảm bảo an toàn thông tin."
*   **Dữ liệu Test:**
    *   Tài khoản Housekeeper: `cleaner@hotel.com` / `Password123`
    *   Tài khoản Lễ tân: `receptionist@hotel.com` / `Password123`
*   **Kiểm tra tính năng:**
    1.  Đăng nhập bằng tài khoản `cleaner@hotel.com`.
    2.  Truy cập trang Dọn dẹp phòng (`/staff/rooms`) $\rightarrow$ Thành công, hiển thị danh sách phòng cần dọn.
    3.  Truy cập trang Quản trị đơn hàng (`/admin/bookings`) $\rightarrow$ Bị chặn, giao diện báo lỗi hoặc tự động chuyển hướng về trang chủ.
    4.  Đăng nhập bằng tài khoản `receptionist@hotel.com`.
    5.  Truy cập trang Quản trị đơn hàng (`/admin/bookings`) $\rightarrow$ Thành công, hiển thị toàn bộ lịch đặt phòng hệ thống.
