# Hướng dẫn Kiểm thử Dự án (Project Testing Guide)
**Dự án:** Hotel Booking System
**Thư mục chứa mã kiểm thử:** `src/test/java/com/hotelbooking/`
**Công cụ sử dụng:** JUnit 5 (Jupyter), Mockito, Spring Security Test, MockMvc (Spring Boot Test)

Tài liệu này tổng hợp cấu trúc kiểm thử, các kịch bản kiểm thử (Test Scenarios) cốt lõi của hệ thống và cách chạy kiểm thử phục vụ cho việc chuẩn bị nội dung review dự án.

---

## 1. Cấu trúc Thư mục Kiểm thử (Test Package Structure)

Toàn bộ các lớp kiểm thử được tổ chức đồng bộ với gói mã nguồn chính và chia làm 3 phân vùng kỹ thuật chính dưới gói gốc [src/test/java/com/hotelbooking](file:///C:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking):

```
src/test/java/com/hotelbooking/
│
├── controller/                  # Unit Test cho tầng REST Controller (sử dụng MockMvc)
│   ├── BookingControllerTest.java
│   ├── HotelControllerTest.java
│   ├── RoomControllerTest.java
│   ├── AdminBookingControllerTest.java
│   └── AdminUserControllerTest.java
│
├── service/                     # Unit Test nghiệp vụ cho tầng Service (sử dụng Mockito)
│   ├── AuthServiceImplTest.java
│   ├── BookingServiceImplTest.java
│   ├── RoomLockServiceImplTest.java
│   ├── HotelServiceImplTest.java
│   ├── RoomServiceImplTest.java
│   ├── VoucherServiceTest.java
│   ├── PaymentServiceTest.java
│   ├── ReportServiceImplTest.java
│   └── AdminUserServiceTest.java
│
└── security/                    # Kiểm thử lập lịch dọn dẹp khóa phòng và bảo mật
    └── RoomLockCleanupSchedulerTest.java
```

---

## 2. Công nghệ & Thư viện Sử dụng

*   **JUnit 5 (Jupiter):** Framework nền tảng để viết và chạy test suite (`@Test`, `@BeforeEach`, `@DisplayName`, `Assertions`).
*   **Mockito:** Sử dụng mock các Repositories/Services phụ thuộc để thực hiện kiểm thử độc lập cho tầng Service:
    *   `@ExtendWith(MockitoExtension.class)`
    *   `@Mock` để giả lập các Bean Repository.
    *   `@InjectMocks` để đưa các mock này vào lớp Service cần test.
    *   Sử dụng `when(...).thenReturn(...)` để mô phỏng hành vi của Database.
*   **Spring Boot Test & MockMvc:** Dùng cho tầng Controller để kiểm tra API Endpoints mà không cần khởi động thực tế Tomcat Server:
    *   `@WebMvcTest(TargetController.class)`
    *   `MockMvc` thực hiện giả lập các HTTP Requests (`post()`, `get()`, `put()`, `delete()`).
    *   Kiểm tra định dạng JSON trả về, HTTP Status trả về, và phân quyền bảo mật (`@WithMockUser`).

---

## 3. Các kịch bản kiểm thử (Test Scenarios) cốt lõi đã cài đặt

### 3.1. Đăng ký & Đăng nhập (AuthServiceImplTest)
*   **Đăng ký tài khoản mới:** Kiểm tra đăng ký thành công và kiểm tra ném ra lỗi trùng lặp Email.
*   **Đăng nhập hệ thống:**
    *   Đăng nhập thành công trả về JWT Token chính xác.
    *   Đăng nhập sai mật khẩu $\rightarrow$ ghi nhận số lần sai, tăng dần lượt đếm.
    *   Khóa tài khoản (Lock account) $\rightarrow$ nếu người dùng đăng nhập sai quá số lần quy định, tài khoản chuyển sang trạng thái `LOCKED`.
*   **Đăng xuất (Logout):** Kiểm tra cơ chế thêm Token hiện tại vào blacklist (`revoked_tokens`) trong Database.

### 3.2. Đặt phòng & Tạm giữ phòng (BookingServiceImplTest & RoomLockServiceImplTest)
*   **Validate Stay Period:**
    *   Ngày nhận phòng ở quá khứ $\rightarrow$ Không hợp lệ.
    *   Ngày trả phòng trước ngày nhận phòng $\rightarrow$ Không hợp lệ.
    *   Số đêm tối thiểu ít hơn 1 đêm $\rightarrow$ Không hợp lệ.
*   **Tạo Booking & Khóa phòng tạm thời:**
    *   Mô phỏng tình huống phòng trống $\rightarrow$ Tạo đơn `PENDING` thành công và ghi nhận khóa phòng tạm thời (`room_locks`) trong 10 phút.
    *   Mô phỏng tình huống phòng đã có người đặt (Overlapping bookings) hoặc phòng đã bị khóa bởi giao dịch khác (Overlapping locks) $\rightarrow$ Ném lỗi `BusinessException` và Rollback Transaction để bảo vệ tài nguyên.
*   **Gia hạn & Giải phóng khóa:** Kiểm tra việc gia hạn khóa phòng khi người dùng thực hiện renew lock và giải phóng khóa khi giao dịch thành công/hủy đơn.

### 3.3. Áp dụng Voucher giảm giá (VoucherServiceTest)
*   **Kiểm tra điều kiện:**
    *   Voucher chưa đến ngày kích hoạt hoặc đã hết hạn $\rightarrow$ Báo lỗi.
    *   Voucher đạt giới hạn sử dụng tối đa (`maxUsage`) $\rightarrow$ Báo lỗi.
    *   Giá trị đơn hàng nhỏ hơn giá trị tối thiểu của voucher (`minBookingValue`) $\rightarrow$ Báo lỗi.
*   **Tính toán giảm giá:**
    *   Giảm giá theo phần trăm (`PERCENTAGE`): ví dụ giảm 10% đơn hàng.
    *   Giảm giá cố định (`FIXED`): trừ trực tiếp số tiền.
    *   Giới hạn mức giảm tối đa bằng tổng tiền đơn phòng nếu mức giảm vượt quá tổng tiền.

### 3.4. Cổng thanh toán & Webhook (PaymentServiceTest)
*   **Khởi tạo thanh toán:** Sinh link thanh toán giả lập với mã định danh giao dịch `transactionId` duy nhất.
*   **Xử lý Webhook callback:**
    *   Nhận webhook có chữ ký HMAC hợp lệ $\rightarrow$ Cập nhật Payment thành `SUCCESS`, Booking thành `CONFIRMED`, gửi email xác nhận.
    *   Nhận webhook sai chữ ký $\rightarrow$ Ném lỗi bảo mật `SecurityException` và ghi vết `PaymentAuditLog`.
    *   Nhận webhook trùng lặp giao dịch (Duplicate callback) $\rightarrow$ Bỏ qua không xử lý lại.

---

## 4. Hướng dẫn Chạy Kiểm thử (Test Execution Command)

Để chạy kiểm thử dự án từ môi trường dòng lệnh (Command Line/Terminal), sử dụng các lệnh Maven sau:

1.  **Chạy toàn bộ Test Suite của dự án:**
    ```bash
    mvn test
    ```
2.  **Chạy riêng một lớp kiểm thử cụ thể (ví dụ lớp test Đặt phòng):**
    ```bash
    mvn test -Dtest=BookingServiceImplTest
    ```
3.  **Chạy kiểm thử một phương thức kiểm thử cụ thể trong lớp:**
    ```bash
    mvn test -Dtest=BookingServiceImplTest#createBooking_Success
    ```
4.  **Kiểm tra tính an toàn của thư viện phụ thuộc (OWASP dependency vulnerability check):**
    ```bash
    mvn dependency:check
    ```
    *(Chạy lệnh này trước mỗi lần release để phát hiện các lỗ hổng bảo mật CVE trong các thư viện bên thứ ba).*
