# 🏨 Hotel Booking System (Hệ Thống Đặt Phòng Khách Sạn Mở Rộng)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0--M1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-8.0-purple.svg)](https://vitejs.dev/)
[![Docker](https://img.shields.io/badge/Docker-Full--Stack-blue.svg)](https://www.docker.com/)
[![Tests](https://img.shields.io/badge/Tests-207%20Passed-emerald.svg)]()

Hệ thống Đặt phòng Khách sạn (Standard & Group Booking Engine) quy mô **50 Màn Hình Enterprise**, hỗ trợ Đặt phòng Lẻ, **Đặt phòng Đoàn (>5 phòng giảm 25%)**, **Đặt cọc 30% Deposit**, **Ví Điện Tử (E-Wallet)**, **Kho Vé Ăn & Mã QR Suất Ăn**, và **Hóa Đơn Thuế Doanh Nghiệp Red VAT (CTP)**.

---

## 📌 Tài liệu Dự án & Project Tracking

Tất cả các tài liệu đặc tả & theo dõi dự án đầy đủ nhất nằm trong kho tài liệu:

1. 🚀 **[Project Tracking Master (PROJECT_TRACKING.md)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/PROJECT_TRACKING.md)**: Ma trận chi tiết 50 Màn hình (SCR-101 đến SCR-510), Lịch sử 30 Migration Flyway, Kết quả Kiểm thử Backend (207 tests), Trạng thái Build Frontend & Cấu hình Docker Containerization.
2. 📋 **[Danh sách Features Hệ thống (FEATURES.md)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/FEATURES.md)**: Tổng hợp 35 Business Use Cases phân theo mô hình Package-by-Feature (`auth`, `user`, `hotel`, `room`, `booking`, `payment`, `voucher`, `report`, `setting`, `customer-portal`).
3. 📝 **[Đặc tả Phân hệ Đặt phòng (specs/003-booking-management/spec.md)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/003-booking-management/spec.md)**: Đặc tả chi tiết thuật toán đặt đoàn (-25%), đặt cọc 30%, giải phóng room lock tự động (10-30 phút) và Engine hoàn tiền hủy phòng.
4. 🗄️ **[Thiết kế Cơ sở Dữ liệu (Database Design Document)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/docs/database/database_design.md)**: Cấu trúc các bảng dữ liệu, Flyway Migrations (V1 - V30), Hỗ trợ song song PostgreSQL Neon Cloud DB & Local SQL Server.
5. 🛡️ **[Quy tắc Bảo mật Agent (AGENTS.md)](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/AGENTS.md)**: Quy chuẩn mã nguồn, bảo mật JWT, Pre-commit Security Hook & Phân quyền RBAC.

---

## 🛠️ Công Nghệ Sử Dụng

- **Backend**: Java 17, Spring Boot 4.0.0-M1, Spring Data JPA, Spring Security (JWT), Spring Scheduling.
- **Database**: PostgreSQL (Neon Cloud Database) / SQL Server, Flyway Migrations (V1-V30).
- **Frontend**: React 18, Vite 8, TailwindCSS, HTML5 Canvas QR Generator.
- **DevOps & Container**: Docker, Docker Compose, Multi-stage Dockerfile, Nginx Reverse Proxy.

---

## 🚀 Hướng Dẫn Khởi Chạy Ứng Dụng

### 1. Khởi chạy nhanh bằng Docker (Recommeded)
```bash
# Khởi chạy đồng thời cả Backend (Port 8080) và Frontend Nginx (Port 80 / 5173)
docker-compose up --build -d
```

### 2. Khởi chạy thủ công (Development Mode)
```bash
# 1. Khởi chạy Backend Spring Boot (với Java 17/18 wrapper)
cmd /c "set JAVA_HOME=C:\Program Files\Java\jdk-18.0.2.1&& set PATH=C:\Program Files\Java\jdk-18.0.2.1\bin;%PATH%&& mvn spring-boot:run -Dspring-boot.run.profiles=dev"

# 2. Khởi chạy Frontend Vite Dev Server
cd frontend
npm run dev
# Mở trình duyệt truy cập: http://localhost:5173
```

### 3. Chạy Kiểm Thử (Unit & Integration Tests)
```bash
# Chạy toàn bộ 207 unit & integration tests
cmd /c "set JAVA_HOME=C:\Program Files\Java\jdk-18.0.2.1&& set PATH=C:\Program Files\Java\jdk-18.0.2.1\bin;%PATH%&& mvn test"
```

---
*Dự án tuân thủ nghiêm ngặt quy trình phát triển dựa trên đặc tả Specification-Driven Development (SDD).*
