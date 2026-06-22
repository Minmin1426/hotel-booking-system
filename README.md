# Hotel Booking System

Chào mừng bạn đến với dự án **Hotel Booking System** (Hệ thống Đặt phòng Khách sạn). Dự án được xây dựng trên nền tảng **Java 17**, **Spring Boot**, và **SQL Server**.

Dự án áp dụng quy trình phát triển dựa trên đặc tả (**Specification-Driven Development - SDD**), trong đó tài liệu đặc tả là nguồn sự thật duy nhất (Single Source of Truth) định hướng toàn bộ mã nguồn và kiểm thử.

## Tài liệu Dự án

Tất cả các tài liệu đặc tả chi tiết của hệ thống nằm trong thư mục `docs/`:

1. **[Đặc tả Toàn diện Hệ thống (System Specification)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/docs/specs/system_specification.md)**: Chứa toàn bộ đặc tả cơ sở dữ liệu, phân quyền RBAC, chi tiết API Endpoints, luồng xử lý và quy tắc nghiệp vụ (Business Rules) cho 35 Use Cases thuộc 6 phân hệ chức năng.
2. **[Thiết kế & Đặc tả Cơ sở dữ liệu (Database Design Document)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/docs/database/database_design.md)**: Thiết kế chi tiết cấu trúc 16 bảng dữ liệu, chỉ mục (Indexes) tối ưu hóa truy vấn, ràng buộc toàn vẹn dữ liệu, sơ đồ ERD Mermaid, và lịch sử Flyway migrations (V1 - V12).
3. **[Phân cấp Chức năng (Functional Decomposition)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/FUNCTIONALDECOMPOSITION.md)**: Danh sách tổng hợp và phân loại các nghiệp vụ hệ thống theo từng module.
4. **[Đặc tả Use Cases (Use Case Specification)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/USECASE.md)**: Phân rã nghiệp vụ chi tiết theo vai trò người dùng (Guest, Customer, Admin, Director, System).
5. **[Quy tắc phát triển cho Agent (AGENTS.md)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/AGENTS.md)**: Quy tắc về kiến trúc, bảo mật, xử lý lỗi và quản lý thông tin mật dành cho các AI Code Agent.


## Hướng dẫn cài đặt & Chạy ứng dụng

### Yêu cầu hệ thống
- JDK 17
- Maven 3.8+
- SQL Server (Mặc định cấu hình kết nối local tại port `1433`)

### Chạy các lệnh kiểm thử và kiểm tra
```bash
# Chạy toàn bộ các Integration & Unit tests
mvn test

# Kiểm tra các lỗ hổng bảo mật của dependencies (OWASP check)
mvn dependency:check

# Khởi chạy ứng dụng Spring Boot local
mvn spring-boot:run
```

---
*Mọi thay đổi đối với tài liệu đặc tả hoặc mã nguồn cần tuân thủ nghiêm ngặt quy trình đề xuất thông qua Pull Request và được đánh giá bảo mật (Pre-commit Security Hook).*
