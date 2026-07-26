# TÀI LIỆU BÁO CÁO & THUYẾT TRÌNH CHI TIẾT DỰ ÁN
## HỆ THỐNG ĐẶT PHÒNG KHÁCH SẠN (HOTEL BOOKING SYSTEM)
> **Tài liệu phục vụ Báo cáo Tiến độ, Bảo vệ Dự án & Review Chuyên môn với Giảng viên Hướng dẫn / Hội đồng**  
> *Được biên soạn chi tiết đầy đủ 17 nội dung cốt lõi theo quy trình Kỹ nghệ Phần mềm hiện đại (SDD, ADD & AI-Assisted Engineering)*

---

## 📋 NỘI DUNG TỔNG QUAN HỆ THỐNG

- **Tên dự án:** Hệ thống Đặt phòng Khách sạn Trực tuyến (Hotel Booking System)
- **Công nghệ áp dụng:** Java 17, Spring Boot 3+, SQL Server / PostgreSQL, Flyway Migration, Maven, RESTful API, JWT Security, React/Vite Frontend, Docker.
- **Phương pháp phát triển:** Specification-Driven Development (SDD - Phát triển dựa trên đặc tả) & Architecture-Driven Development (ADD - Thiết kế dựa trên kiến trúc).
- **Cấu trúc kiến trúc:** Package-by-Feature (Phân rã theo gói tính năng chuyên biệt).

---

# PHẦN I: TỔNG QUAN, ĐỘNG LỰC PHÁT TRIỂN & PHÂN TÍCH THỊ TRƯỜNG

## 1. Trang 1: Giới Thiệu Tên Dự Án & Thông Tin Tổng Quan (Project Identification & Overview)

### 1.1 Tên Dự Án
**Hotel Booking System — Hệ Thống Quản Lý Và Đặt Phòng Khách Sạn Trực Tuyến Chuẩn Doanh Nghiệp.**

### 1.2 Thành Phần Tham Gia & Phân Công Nhiệm Vụ
- **Nhóm thực hiện:** Nhóm phát triển phần mềm dự án Hotel Booking System.
- **Giảng viên hướng dẫn:** Giảng viên phụ trách môn học / Đồ án tốt nghiệp Kỹ nghệ phần mềm.
- **Vai trò thành viên:** Phân chia theo mô hình Feature Ownership (Mỗi thành viên chịu trách nhiệm trọn gói từ DB, Backend Service, REST API DTO đến Frontend UI của từng phân hệ chức năng).

### 1.3 Mục Tiêu Trình Bày Với Giảng Viên
Báo cáo toàn bộ chuỗi quy trình từ khâu ý tưởng, phân tích thị trường, chốt yêu cầu bằng AI, thiết lập kiến trúc SDD/ADD, thiết kế CSDL, quản trị rủi ro schema, phát triển theo gói tính năng, kiểm thử tự động, quét lỗ hổng bảo mật cho tới khi đóng gói sản phẩm hoàn chỉnh.

---

## 2. Lý Do Chọn Đề Tài (Problem Statement & Project Rationale)

### 2.1 Thực Trạng Vấn Đề (Problem Background)
Ngành du lịch và lưu trú trực tuyến bùng nổ đòi hỏi các hệ thống đặt phòng phải xử lý khối lượng giao dịch cực kỳ lớn với độ chính xác tuyệt đối. Qua phân tích thực tế, các hệ thống quản lý khách sạn vừa và nhỏ hiện nay thường gặp phải các vấn đề kỹ thuật nghiêm trọng:
1. **Sự cố Overbooking (Đặt trùng phòng):** Khi nhiều người dùng cùng tìm kiếm và thao tác đặt cùng một phòng tại cùng một thời điểm, nếu hệ thống không có cơ chế khóa tạm thời (Room Locking) và xử lý bất đồng bộ/đồng thời (Concurrency Control) tốt, việc bán vượt số lượng phòng rất dễ xảy ra.
2. **Tính toán sai lệch Giá phòng & Voucher:** Việc áp dụng mã giảm giá, tính phụ phí lễ tết, phụ phí người lớn/trẻ em thường bị rối loạn logic nếu không được quy định bằng bộ Quy tắc Nghiệp vụ (Business Rules) tập trung.
3. **Trải nghiệm người dùng kém & Không minh bạch:** Thiếu cơ chế giữ phòng có thời hạn (ví dụ: giữ phòng 10-15 phút để khách hàng hoàn tất thanh toán VNPay/Stripe), khiến người dùng hủy ngang hoặc mất phòng vô lý.
4. **Bảo mật và Phân quyền lỏng lẻo:** Thiếu phân quyền chặt chẽ giữa Khách hàng (Customer), Quản trị viên (Admin) và Giám đốc điều hành (Director), dễ gây rò rỉ dữ liệu hoặc thao tác trái thẩm quyền.

### 2.2 Mục Tiêu Kỹ Thuật Của Dự Án
Hệ thống được xây dựng nhằm giải quyết triệt để các thách thức trên thông qua:
- **Kiến trúc bền vững (Robust Architecture):** Xây dựng trên nền tảng **Java 17** và **Spring Boot**, đảm bảo hiệu năng cao, độ tin cậy và khả năng mở rộng.
- **Giải pháp khóa phòng tạm thời (Temporary Room Lock Engine):** Sử dụng cơ chế khóa có thời hạn kết hợp tác vụ ngầm (Scheduler) để tự động giải phóng phòng nếu không thanh toán đúng hạn.
- **Quản lý dữ liệu chính xác tuyệt đối:** Áp dụng Flyway Migration quản lý phiên bản CSDL và cơ chế Cố định Schema (Database Freezing) chống rủi ro sai lệch dữ liệu.
- **Tuân thủ quy trình phần mềm chuẩn mực:** Áp dụng phương pháp **SDD** (Phát triển dựa trên đặc tả) và **ADD** (Thiết kế dựa trên kiến trúc) cùng sự hỗ trợ của các công cụ AI Agent tiên tiến.

---

## 3. Phân Tích Sản Phẩm Tương Tự & Quyết Định Tính Năng (Benchmarking, Market Research & Feature Scope Decision)

### 3.1 Phân Tích Các Giải Pháp Hiện Hữu Trên Thị Trường
Nhóm đã tiến hành nghiên cứu các nền tảng OTA (Online Travel Agency) lớn như Booking.com, Agoda, Traveloka và các phần mềm PMS (Property Management System) phổ biến:

| Tiêu Chí So Sánh | Booking.com / Agoda | Phần Mềm PMS Nhỏ Đơn Lẻ | Hệ Thống Hotel Booking Nhóm Đề Xuất |
| :--- | :--- | :--- | :--- |
| **Quy Mô Hệ Thống** | Cực lớn, kết nối đa khách sạn toàn cầu | Nhỏ, cài đặt cục bộ tại 1 khách sạn | Chuẩn Doanh nghiệp (Enterprise-ready), hỗ trợ chuỗi/phòng |
| **Cơ Chế Giữ Phòng** | Xử lý phức tạp, giữ phòng qua thẻ | Thiếu tính năng giữ phòng thời gian thực | Khóa phòng tạm thời (Room Lock) có đếm ngược tự động |
| **Phân Quyền Chi Tiết (RBAC)** | Phức tạp, nhiều tầng nấc | Đơn giản (Admin / Staff) | Rõ ràng 4 nhóm: Guest, Customer, Admin, Director |
| **Voucher & Khuyến Mãi** | Đa dạng nhưng logic đóng kín | Hạn chế, cấu hình cứng | Cấu hình linh hoạt: Giảm %, Giảm cố định, Điều kiện áp dụng |
| **Kiểm Soát Mã Nguồn & CSDL** | Đóng kín (Proprietary) | Thường không có quản lý phiên bản DB | Flyway Migration + SDD + Quản lý Schema nghiêm ngặt |

### 3.2 Quyết Định Phạm Vi Tính Năng Cốt Lõi (Scope & Feature Matrix)
Dựa trên kết quả so sánh, nhóm đã quyết định xây dựng hệ thống tập trung vào **6 Phân Hệ Chức Năng Cốt Lõi (35 Use Cases)**:
1. **Phân hệ Xác thực & Bảo mật (Auth Module):** Đăng ký, Đăng nhập, JWT Refresh Token Rotation, Đăng xuất, Đổi mật khẩu, Quên mật khẩu.
2. **Phân hệ Quản lý Người dùng (User Module):** Quản lý hồ sơ cá nhân, Quản lý tài khoản người dùng, Phân quyền RBAC.
3. **Phân hệ Quản lý Khách sạn & Phòng (Hotel & Room Module):** Tìm kiếm phòng khả dụng theo ngày/giá/tiện nghi, Quản lý loại phòng, Quản lý thông tin phòng, Duyệt đánh giá (Reviews).
4. **Phân hệ Đặt phòng & Khóa phòng (Booking & Room Lock Module):** Đặt phòng, Tạo lệnh khóa phòng tạm thời, Tự động hủy lệnh quá hạn qua Scheduler, Xử lý hủy phòng & Hoàn tiền theo chính sách.
5. **Phân hệ Thanh toán & Voucher (Payment & Voucher Module):** Khởi tạo giao dịch thanh toán, Quản lý trạng thái thanh toán, Quản lý và áp dụng Mã giảm giá (Voucher redemption validation).
6. **Phân hệ Báo cáo & Cấu hình (Report & Setting Module):** Báo cáo doanh thu cho Director, Thống kê tỷ lệ lấp đầy phòng (Occupancy rate), Cấu hình tham số hệ thống.

---

# PHẦN II: ỨNG DỤNG AI & QUY TRÌNH THIẾT KẾ ĐẶC TẢ (SDD & ADD)

## 4. Dùng AI Hỗ Trợ Chốt Tính Năng Như Thế Nào? (AI-Assisted Requirements Engineering)

### 4.1 Phương Pháp Tiếp Cận Đột Phá Với AI Agent
Nhóm không phụ thuộc hoàn toàn vào AI để viết code ngẫu nhiên, mà áp dụng AI theo **Quy trình Kỹ nghệ Yêu cầu có Kiểm soát (Controlled Requirements Engineering)**:

```
[Khai Phá Yêu Cầu] ---> [AI Brainstorm & Draft Use Cases] ---> [Con Người Review & Audit] ---> [Chốt Special Specification]
```

### 4.2 Các Bước Trực Tiếp Áp Dụng
1. **Bước 1: Prompting Định Tường & Thu Thập Ý Tưởng:**  
   Sử dụng AI Agent để liệt kê toàn bộ các kịch bản ngoại lệ (Edge Cases) trong nghiệp vụ đặt phòng (Ví dụ: Khách hủy phòng trước 24h thì hoàn bao nhiêu %? Khách đặt trùng lịch thì báo lỗi gì? Quá thời gian giữ phòng mà chưa thanh toán thì giải phóng ra sao?).
2. **Bước 2: AI Sinh Chuẩn Sơ Đồ & Danh Sách Use Cases:**  
   AI hỗ trợ nhóm phân rã 35 Use Cases theo đúng chuẩn Uml/Actor (Guest, Customer, Admin, Director, System).
3. **Bước 3: Thẩm Định Lại (Critical Human Audit):**  
   Thành viên nhóm rà soát lại kết quả sinh ra từ AI để loại bỏ các tính năng dư thừa không khả thi, điều chỉnh lại logic cho phù hợp với thời gian triển khai đồ án.
4. **Bước 4: Đóng Gói Thành Tài Liệu Đặc Tả (`USECASE.md` & `FEATURES.md`):**  
   Chuyển hóa toàn bộ kết quả đã chốt thành các file markdown đóng vai trò là "Khuôn mẫu" cho cả dự án.

---

## 5. Dựng Tài Liệu Cơ Bản Trước Khi Code (Single Source of Truth - SSoT)

### 5.1 Triết Lý Single Source of Truth (SSoT)
Nhóm cam kết triệt để nguyên tắc: **Tài liệu đặc tả là nguồn sự thật duy nhất.** Không một dòng code nào được viết ra nếu chưa có trong đặc tả, và không một bảng CSDL nào được tạo ra nếu chưa được mô tả chi tiết trong tài liệu thiết kế.

### 5.2 Bộ Tài Liệu Chuẩn Hóa Trong Thư Mục `docs/`
Nhóm đã dựng sẵn 8 tài liệu kỹ thuật cốt lõi trước khi tiến hành viết dòng code đầu tiên:
1. `docs/specs/system_specification.md`: Tài liệu toàn diện chứa 35 Use Cases, luồng xử lý và Business Rules.
2. `docs/database/database_design.md`: Sơ đồ ERD, thiết kế 16 bảng CSDL, Indexing và lịch sử Flyway migrations (V1 - V12).
3. `docs/architecture/architecture_design.md`: Thiết kế kiến trúc N-Tier, phân rã Package-by-Feature, JWT Authentication flow.
4. `docs/business-rules/business_rules.md`: Chi tiết các ràng buộc logic ở tầng Service và Database.
5. `docs/api/api_specification.md`: Chuẩn hóa REST API Endpoints, Request/Response DTOs, HTTP Status Codes.
6. `FEATURES.md`: Tổng hợp danh sách tính năng theo từng package.
7. `USECASE.md`: Chi tiết từng Use Case theo Actor.
8. `AGENTS.md`: Bộ quy tắc đóng vai trò "Chính sách Báo mật và Tiêu chuẩn Code" bắt buộc AI và người lập trình phải tuân theo.

---

## 6. Kiến Thức Học Được Về SDD và ADD (Specification-Driven & Architecture-Driven Development)

### 6.1 SDD — Specification-Driven Development (Phát Triển Dựa Trên Đặc Tả)
- **Bản chất:** SDD coi tài liệu đặc tả (Specification) là "trái tim" của dự án. Mọi nâng cấp, sửa đổi hay kiểm thử đều phải lấy đặc tả làm gốc.
- **Quy trình làm việc SDD:**
  1. Viết Đặc tả (Write Spec) -> 2. Review Đặc tả (Review Spec) -> 3. Viết Mã Nguồn (Implement Code) -> 4. Kiểm Thử Đối Soát (Verify against Spec).
- **Lợi ích thực tế thu được:**
  - Loại bỏ hoàn toàn tình trạng "Code một đằng, tài liệu một nẻo".
  - Giúp các thành viên trong nhóm làm việc độc lập mà không bị gián đoạn hay tranh cãi về mặt logic.
  - Giúp AI Agent hiểu chính xác ngữ cảnh dự án mà không sinh ra code rác.

### 6.2 ADD — Architecture-Driven Development (Thiết Kế Dựa Trên Kiến Trúc)
- **Bản chất:** ADD lấy các Thuộc tính Chất lượng (Quality Attributes) như: Bảo mật (Security), Hiệu năng (Performance), Khả năng bảo trì (Maintainability) và Khả năng mở rộng (Scalability) để định hình kiến trúc ứng dụng.
- **Ứng dụng Mô Hình Package-by-Feature trong Dự Án:**  
  Thay vì chia thư mục theo tầng cổ điển (Package-by-Layer: `controller`, `service`, `repository`), nhóm áp dụng **Package-by-Feature** (Chia theo tính năng):
  ```
  src/main/java/com/hotelbooking/
  ├── common/          # Cross-cutting: security, config, exception, utils
  ├── auth/            # Auth controller, service, tokens, DTOs
  ├── user/            # User controller, service, entity, DTOs
  ├── hotel/           # Hotel controller, service, entity, DTOs
  ├── room/            # Room & Locking controller, service, entity, DTOs
  ├── booking/         # Booking controller, service, entity, DTOs
  ├── payment/         # Payment controller, service, entity, DTOs
  ├── voucher/         # Voucher controller, service, entity, DTOs
  ├── report/          # Report controller, service, DTOs
  └── setting/         # Setting controller, service
  ```
- **Ưu điểm lớn của Package-by-Feature:**
  - **Tính đóng gói cao (High Encapsulation):** Các repository và implementation class có thể để phạm vi `package-private`, tránh bị các phân hệ khác gọi trực tiếp gây phụ thuộc vòng (Circular Dependency).
  - **Dễ dàng làm việc nhóm:** Mỗi thành viên toàn quyền phụ trách một folder tính năng mà không lo đụng độ code (Merge Conflict) với thành viên khác.

---

# PHẦN III: BỘ KỸ THUẬT, THẨM ĐỊNH BẢO MẬT & QUẢN LÝ CSDL

## 7. Chuẩn Bị Bộ Kỹ Thuật (Tech Stack & Development Tooling)

Nhóm đã lựa chọn và chuẩn bị môi trường kỹ thuật hiện đại, chuẩn doanh nghiệp:

### 7.1 Backend Tech Stack
- **Ngôn ngữ:** Java 17 (Phiên bản LTS ổn định, tối ưu về hiệu năng và hỗ trợ các tính năng hiện đại như Records, Sealed Classes, Pattern Matching).
- **Framework:** Spring Boot 3.x / 4.x (Tối ưu hóa dependency management, hỗ trợ Spring Security 6, Jakarta EE).
- **Security:** Spring Security + JWT (JSON Web Token) với cơ chế Refresh Token Rotation và mã hóa mật khẩu bằng BCrypt (strength ≥ 12).
- **ORM & Data Access:** Spring Data JPA / Hibernate kết hợp với QueryDSL/Specification cho tìm kiếm động.
- **Database Migration:** Flyway (Tự động hóa việc quản lý và thực thi các bản SQL Migration).

### 7.2 Database & Storage
- **Hệ quản trị CSDL:** SQL Server (Hỗ trợ tốt transaction phức tạp, ACID compliant) / PostgreSQL.

### 7.3 Frontend & Design System
- **Frontend Stack:** React / Vite / HTML5 / Vanilla CSS với các kỹ thuật giao diện hiện đại (Glassmorphism, Vibrant Colors, CSS Variables).

### 7.4 Quality & Automation Tools
- **Build Tool:** Apache Maven 3.9+.
- **Security Audit Tool:** OWASP Dependency Check (`mvn dependency:check`).
- **Testing:** JUnit 5, Mockito.
- **Version Control:** Git & GitHub với Pre-commit Security Hooks.

---

## 8. Quy Trình Thẩm Định Nhiều Lớp "Double - Triple Check" (Verification & Validation)

Để đảm bảo mã nguồn và hệ thống đạt chất lượng cao nhất, nhóm thiết lập quy trình **Kiểm tra 3 Lớp (Triple Check)** trước khi một tính năng được coi là hoàn tất:

```
[Lớp 1: Rà Soát Đặc Tả & DTO] ---> [Lớp 2: Kiểm Thử Mã Nguồn & Build] ---> [Lớp 3: Quét Lỗ Hổng Bảo Mật & Static Analysis]
```

1. **Lớp 1 (Check 1 - Spec & API Contract Audit):**  
   Đối soát code Controller/DTO với file `docs/api/api_specification.md`. Đảm bảo tên trường (field names), kiểu dữ liệu (data types) và validation annotation (`@NotNull`, `@Size`, `@Email`) chính xác 100%.
2. **Lớp 2 (Check 2 - Automated Test Execution):**  
   Bắt buộc thực thi lệnh `mvn test`. Mọi Unit Test và Integration Test liên quan đến Service logic, Booking Flow, Locking Mechanism phải vượt qua (Pass 100%).
3. **Lớp 3 (Check 3 - Security & Dependency Audit):**  
   Thực thi lệnh `mvn dependency:check` để đảm bảo không sử dụng thư viện chứa lỗ hổng bảo mật nghiêm trọng (CVE). Kiểm tra qua Pre-commit Security Hook để đảm bảo không lộ mật khẩu hay secret key.

---

## 9. Quy Trình Xử Lý Sau Khi Cập Nhật Xong (Change Management & Versioning)

Trong quá trình phát triển, nếu có thay đổi hoặc phát sinh yêu cầu nghiệp vụ mới, nhóm áp dụng **Quy trình Quản lý Thay đổi 4 Bước**:

1. **Bước 1 — Đề xuất Thay đổi (Change Request):** Thành viên phát hiện điểm cần thay đổi sẽ mở thảo luận hoặc issue trên Git.
2. **Bước 2 — Cập nhật Tài liệu Đặc tả Trước (Update Spec First):** Chỉnh sửa file markdown tương ứng trong thư mục `docs/` (ví dụ: bổ sung trường dữ liệu mới vào `database_design.md`).
3. **Bước 3 — Tạo Pull Request & Code Review Tài liệu:** Thành viên khác rà soát và phê duyệt (Approve) thay đổi trong đặc tả.
4. **Bước 4 — Triển khai Mã Nguồn & Tạo Migration:** Sau khi đặc tả được cập nhật, mới tiến hành viết code Java và tạo bản Flyway Migration mới (ví dụ: `V13__add_new_field.sql`).

---

## 10. Chuẩn Bị CSDL và Triển Khai CSDL (Database Design & Flyway Deploy)

### 10.1 Thiết Kế CSDL Chuẩn Hóa (3NF)
CSDL của hệ thống gồm **16 bảng dữ liệu** được thiết kế chuẩn hóa dạng 3NF, phân rã hợp lý để đảm bảo tính toàn vẹn dữ liệu:
- **Tài khoản & Phân quyền:** `users`, `roles`, `user_roles`, `refresh_tokens`.
- **Khách sạn & Phòng:** `hotels`, `room_types`, `rooms`, `room_images`, `amenities`, `room_amenities`.
- **Đặt phòng & Khóa phòng:** `bookings`, `booking_details`, `room_locks`.
- **Thanh toán & Voucher:** `payments`, `vouchers`, `voucher_usages`.
- **Đánh giá:** `reviews`.

### 10.2 Quản Lý Phiên Bản CSDL Với Flyway Migration
Nhóm không thực hiện tạo bảng thủ công trên CSDL, mà toàn bộ DDL được quản lý qua các file SQL Migration đặt tại `src/main/resources/db/migration/sqlserver/`:
- `V1__init_schema.sql`: Khởi tạo cấu trúc các bảng cơ bản.
- `V2__seed_initial_data.sql`: Chèn dữ liệu danh mục ban đầu (Roles, Amenities).
- ...
- `V12__add_indexes_and_constraints.sql`: Thêm chỉ mục (Indexes) tối ưu hóa truy vấn tìm kiếm phòng.

**Lợi ích:** Bất kỳ thành viên nào khi pull code về chỉ cần chạy ứng dụng, Flyway sẽ tự động đồng bộ CSDL local lên phiên bản mới nhất một cách an toàn và chính xác.

---

## 11. Chiến Lược Cố Định CSDL (DB Schema Freezing) & Quản Trị Rủi Ro AI

### 11.1 Lý Do Phải Cố Định CSDL (Why Freeze DB Schema?)
Trong các dự án áp dụng AI Agent, một rủi ro cực kỳ lớn là **AI tự ý thay đổi cấu trúc bảng (DDL)**, tự thêm/xóa cột hoặc xóa bảng khi gặp lỗi mã nguồn. Điều này dẫn đến:
- Mất mát dữ liệu (Data Loss).
- Sai lệch so với tài liệu đặc tả ERD đã thống nhất với Giảng viên.
- Gây lỗi dây chuyền cho code của các thành viên khác trong nhóm.

### 11.2 Các Biện Pháp Bảo Vệ & Cố Định CSDL Nghiêm Ngặt
Nhóm đã triển khai chiến lược bảo mật CSDL 3 tầng (Được quy định cụ thể tại **Section 4 & Section 7 của `AGENTS.md`**):

1. **Cấm Tuyệt Đối Lệnh DDL Trực Tiếp từ Tài Khoản Ứng Dụng:**  
   User CSDL kết nối từ Spring Boot chỉ có quyền `SELECT`, `INSERT`, `UPDATE`, `DELETE`. Không cấp quyền `CREATE`, `ALTER`, `DROP`.
2. **Ngăn Chặn AI Sửa CSDL (AI Restriction Rule):**  
   Quy định rõ trong `AGENTS.md`: AI Agent **KHÔNG BẢO GIỜ** được phép sinh lệnh DDL trực tiếp trên DB hoặc tự ý chỉnh sửa các file Migration cũ đã ship.
3. **Quy Trình Sửa DB Duy Nhất — Cả Team Đồng Thuận (Team Approval Workflow):**  
   Mọi thay đổi CSDL bắt buộc phải:
   - Được thảo luận và 100% thành viên team đồng ý.
   - Tạo một file Flyway Migration mới (ví dụ: `V13__description.sql`).
   - Được kiểm duyệt qua Pull Request trước khi merge.

---

# PHẦN IV: THIẾT KẾ FRONTEND, PHÂN RÃ MÀN HÌNH & VẬN HÀNH DỰ ÁN

## 12. Sử Dụng "Skills" Để AI Thiết Kế Frontend Chuẩn Mực (AI Skill-Based Frontend Engineering)

### 12.1 Khái Niệm AI Skill Là Gì?
Trong hệ thống AI Coding hiện đại (như Antigravity AI Agent), **Skill** là một tập hợp các hướng dẫn, tiêu chuẩn, mã mẫu và quy tắc được đóng gói sẵn để định hướng cho AI thực hiện một nhiệm vụ chuyên biệt.

### 12.2 Cách Nhóm Xây Dựng Skill Thiết Kế Frontend
Để giao diện ứng dụng không bị rời rạc hay mang dáng dấp của các ứng dụng mẫu đơn điệu, nhóm đã viết **Frontend Design System Skill** cung cấp cho AI các quy tắc thiết kế cao cấp:
1. **Bảng màu & Typography Chuẩn:** Định nghĩa CSS Variables cho màu sắc (Hsl Tailored Palette, Dark Mode, Accent Colors) và Font chữ Google Fonts (`Inter`, `Outfit`).
2. **Phong cách Thẩm mỹ (Rich Aesthetics):** Yêu cầu áp dụng hiệu ứng Glassmorphism (làm mờ hậu cảnh), Gradient mềm mại, và Micro-animations (hiệu ứng hover, chuyển trang mượt mà).
3. **Component Reusability:** Yêu cầu AI viết các thành phần (Button, Card, Modal, Input) thành các module tái sử dụng, không dùng inline CSS hay các utility ngẫu nhiên.
4. **Không Dùng Ảnh Placeholder Giả:** Bắt buộc AI sử dụng công cụ tạo ảnh hoặc asset thực tế để giao diện sinh ra trông như một sản phẩm thật 100%.

---

## 13. Phân Rã Màn Hình & Mô Hình Hóa Tính Năng (Screen Mapping & Feature Breakdown)

### 13.1 Phân Rã Giao Diện Theo Actor (Actor-Based Screen Breakdown)
Hệ thống được chia thành 3 nhóm màn hình chính tương ứng với các vai trò người dùng:

```
                          [HỆ THỐNG PHÂN RÃ MÀN HÌNH]
                                       |
       +-------------------------------+-------------------------------+
       |                               |                               |
[KHÁCH HÀNG (Customer)]       [QUẢN TRỊ VIÊN (Admin)]       [GIÁM ĐỐC (Director)]
 - Màn hình Trang chủ          - Dashboard Quản lý           - Báo cáo Doanh thu tổng hợp
 - Tìm kiếm & Lọc phòng        - Quản lý Khách sạn & Phòng   - Thống kê Tỷ lệ lấp đầy phòng
 - Chi tiết Phòng & Tiện nghi  - Duyệt Đánh giá (Reviews)    - Cấu hình Tham số Hệ thống
 - Khóa phòng & Đặt phòng      - Quản lý Danh mục Voucher
 - Thanh toán & Nhận vé        - Quản lý Đặt phòng (Bookings)
 - Lịch sử Đặt phòng & Đánh giá
```

### 13.2 Luồng Trải Nghiệm Khách Hàng (Customer Booking Flow)
1. Khách hàng truy cập Trang chủ -> Tìm kiếm phòng theo Ngày nhận/trả phòng và Số lượng khách.
2. Hệ thống hiển thị Danh sách phòng khả dụng (Đã lọc bỏ các phòng đang bị khóa hoặc đã có người đặt).
3. Khách chọn phòng -> Hệ thống khởi tạo **Lệnh khóa phòng tạm thời (Room Lock)** 10 phút.
4. Khách nhập thông tin, áp dụng Mã giảm giá (Voucher) -> Chuyển sang Màn hình Thanh toán.
5. Sau khi thanh toán thành công -> Chuyển trạng thái Booking sang `CONFIRMED`, gửi Email xác nhận và tạo Mã vé Booking.

---

## 14. Phân Công Nhân Sự Theo Gói Tính Năng (Package-by-Feature Allocation)

Nhóm phân chia công việc theo mô hình **Feature Ownership**, loại bỏ tình trạng "người làm frontend chờ người làm backend":

| Phân Hệ Tính Năng (Feature Module) | Thành Viên Phụ Trách | Phạm Vi Đóng Gói (Encapsulation Scope) |
| :--- | :--- | :--- |
| **Auth & Security Module** | Thành viên A | `com.hotelbooking.auth` (Controller, Service, JWT, DTOs) |
| **User & RBAC Module** | Thành viên B | `com.hotelbooking.user` (User Profile, Roles, Permissions) |
| **Hotel & Room Module** | Thành viên C | `com.hotelbooking.hotel`, `com.hotelbooking.room` |
| **Booking & Locking Module**| Thành viên D | `com.hotelbooking.booking` (Lock Engine, Scheduler) |
| **Payment & Voucher Module** | Thành viên E | `com.hotelbooking.payment`, `com.hotelbooking.voucher` |
| **Report & Setting Module**  | Thành viên F | `com.hotelbooking.report`, `com.hotelbooking.setting` |

---

## 15. Quy Trình Code Và Review Hàng Tuần (Weekly Sprint & Code Review Workflow)

### 15.1 Chu Kỳ Sprint Hàng Tuần (Weekly Iterations)
Nhóm áp dụng mô hình Agile/Scrum rút gọn với chu kỳ 1 tuần:
- **Thứ Hai:** Lập kế hoạch Sprint, phân chia công việc trên GitHub Projects.
- **Thứ Hai - Thứ Sáu:** Tiến hành lập trình, tuân thủ `AGENTS.md` và Google Java Style Guide.
- **Thứ Bảy:** Tổ chức buổi **Code Review hàng tuần** (Weekly Review).

### 15.2 Quy Trình Pull Request & Review Bắt Buộc
- **Chiến lược Git Branching:** Cấm commit trực tiếp vào nhánh `main`. Mọi tính năng phải phát triển trên nhánh riêng (ví dụ: `feature/booking-lock-engine`).
- **Yêu cầu khi tạo PR:**
  - Mô tả rõ mục đích thay đổi.
  - Đính kèm kết quả chạy `mvn test` và `mvn dependency:check`.
  - Phải có ít nhất 1 thành viên khác (hoặc Team Lead) review và Approve mới được merge.

---

## 16. Đưa Kiểm Thử Tự Động & Kiểm Tra Bảo Mật Vào Quy Trình (Automated Testing & Security Pipeline)

### 16.1 Kiểm Thử Tự Động (Automated Testing Pipeline)
Nhóm triển khai hệ thống kiểm thử đa tầng:
- **Unit Tests:** Kiểm thử các hàm xử lý logic nghiệp vụ tính giá, kiểm tra điều kiện áp voucher, mã hóa mật khẩu.
- **Integration Tests:** Kiểm thử luồng giao dịch Đặt phòng - Khóa phòng - Thanh toán tích hợp với Spring Boot Test Context.
- **Lệnh thực thi:** `mvn test` được chạy tự động trên CI/CD hoặc trước khi merge code.

### 16.2 Kiểm Tra Lỗ Hổng Bảo Mật Thư Viện (OWASP Dependency Check)
Chạy lệnh `mvn dependency:check` định kỳ để quét toàn bộ các file JAR phụ thuộc trong `pom.xml`. Nếu phát hiện thư viện có lỗ hổng bảo mật mức Critical (CVE), nhóm lập tức nâng cấp phiên bản thư viện.

### 16.3 Pre-commit Security Hook Chống Lộ Mật Khẩu (Security Guard)
Để ngăn chặn tuyệt đối việc thành viên vô tình commit thông tin nhạy cảm lên GitHub, nhóm cài đặt script **Pre-commit Hook** (quy định tại **Section 4 trong `AGENTS.md`**).  
Hook sẽ tự động quét và **Block Commit** nếu phát hiện các mẫu chuỗi nguy hiểm:
- Password / Secret hardcoded (ví dụ: `password=123456`).
- Private Key, JWT Signing Key chưa rút ra biến môi trường.
- Tokens GitHub / Slack / AWS Keys.

---

## 17. Hoàn Thiện Dự Án, Đánh Giá Kết Quả & Hướng Phát Triển (Project Completion & Future Roadmap)

### 17.1 Đánh Giá Kết Quả Đạt Được (Project Accomplishments)
1. **Hoàn thành 100% Phạm vi:** Triển khai trọn vẹn 35 Use Cases thuộc 6 phân hệ chức năng.
2. **Kiến trúc Chuẩn mực:** Áp dụng thành công SDD và ADD với cấu trúc Package-by-Feature sạch sẽ, dễ bảo trì.
3. **Quản trị Dữ liệu An toàn:** Flyway Migration V1-V12 hoạt động hoàn hảo, CSDL được cố định an toàn chống rủi ro.
4. **Bảo mật Đa lớp:** Xử lý xác thực JWT, phân quyền RBAC, mã hóa BCrypt, Pre-commit hook và quét OWASP đầy đủ.
5. **Giao diện Hiện đại:** Frontend responsive, chuẩn UI/UX và mang lại trải nghiệm người dùng vượt trội.

### 17.2 Bài Học Kinh Nghiệm (Lessons Learned)
- **Tầm quan trọng của Đặc tả:** Dành thời gian làm tài liệu đặc tả kỹ lưỡng ban đầu giúp tiết kiệm 50% thời gian sửa lỗi (debug/rework) về sau.
- **Kiểm soát AI hiệu quả:** AI là công cụ hỗ trợ đắc lực nếu có quy tắc ràng buộc (`AGENTS.md`, Skills), nhưng cần con người giữ vai trò phê duyệt cuối cùng.

### 17.3 Định Hướng Phát Triển Trong Tương Lai (Future Roadmap)
- **Chuyển đổi sang Kiến trúc Microservices:** Tách riêng Booking Service và Payment Service để nâng cao khả năng chịu tải.
- **Tích hợp AI Recommendation Engine:** Gợi ý phòng và khách sạn thông minh dựa trên lịch sử đặt phòng của khách hàng.
- **Tích hợp Đa cổng Thanh toán Quốc tế:** Kết nối cổng thanh toán Stripe, PayPal, VNPay thực tế.

---

# BẢNG TỔNG HỢP CÁC CÂU HỎI REVIEW THƯỜNG GẶP VỚI GIẢNG VIÊN (Q&A PREPARATION)

Để chuẩn bị tốt nhất cho buổi Review và Thuyết trình, dưới đây là bộ câu hỏi trọng tâm mà Giảng viên thường đặt ra và gợi ý câu trả lời chuẩn xác:

### ❓ Câu 1: Tại sao nhóm chọn Java 17 & Spring Boot mà không chọn các Framework khác?
- **Trả lời:** Java 17 là phiên bản LTS có tính ổn định cao, tối ưu về bộ nhớ và hiệu năng với Garbage Collector mới. Spring Boot cung cấp hệ sinh thái mạnh mẽ cho ứng dụng doanh nghiệp (Spring Security cho JWT/RBAC, Spring Data JPA cho ORM, Spring Scheduler cho tác vụ ngầm).

### ❓ Câu 2: Nhóm giải quyết bài toán Overbooking (Đặt trùng phòng) như thế nào?
- **Trả lời:** Nhóm sử dụng giải pháp 2 tầng:
  1. Tầng CSDL: Sử dụng cơ chế Transaction Isolation và pessimistic/optimistic locking khi cập nhật trạng thái phòng.
  2. Tầng Nghiệp vụ: Thiết lập **Room Lock Engine** — khi khách chọn phòng, hệ thống tạo bản ghi khóa phòng tạm thời có hiệu lực 10-15 phút trong bảng `room_locks`. Tác vụ ngầm `Spring Scheduler` sẽ tự động quét và giải phóng phòng nếu khách không hoàn tất thanh toán.

### ❓ Câu 3: SDD và ADD đã giúp ích gì cụ thể cho nhóm?
- **Trả lời:** SDD giúp nhóm có tài liệu đặc tả chuẩn (`docs/`) làm "nguồn sự thật duy nhất", tránh tranh cãi nghiệp vụ và giúp AI Agent sinh code chuẩn xác. ADD giúp nhóm định hình kiến trúc Package-by-Feature, đóng gói dữ liệu tốt hơn và dễ phân công công việc mà không bị đụng độ code.

### ❓ Câu 4: Làm thế nào nhóm đảm bảo AI không sinh ra mã nguồn lỗi hoặc phá hỏng CSDL?
- **Trả lời:** Nhóm xây dựng file quy tắc `AGENTS.md` với các chính sách bảo mật và kiến trúc nghiêm ngặt. Nhóm áp dụng cơ chế Cố định Schema CSDL (DB Freezing), không cấp quyền DDL cho tài khoản app và bắt buộc mọi thay đổi DB phải thông qua Flyway Migration được cả team kiểm duyệt.

---
*Tài liệu này được biên soạn đầy đủ, chi tiết nhằm phục vụ công tác báo cáo và bảo vệ đồ án trước Giảng viên Hướng dẫn và Hội đồng Chuyên môn.*
