# Tài liệu Đặc tả Use Case - Hệ thống Đặt phòng Khách sạn

Tài liệu này phân loại và tổng hợp 35 nghiệp vụ (Use Case) của hệ thống đặt phòng khách sạn, được chia thành 6 phân hệ (modules) chính để phục vụ quá trình phát triển và quản lý dự án.

---

## 1. Phân hệ Xác thực và Quản lý Tài khoản (Authentication & Identity)
Quản lý định danh, bảo mật, phân quyền và thông tin cá nhân của người dùng.

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Kết quả mong đợi |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Đăng ký tài khoản | Guest, System | Medium | Detailed | Tài khoản được tạo |
| 2 | Đăng nhập hệ thống | Guest, Customer, Admin, System | High | Formal | Đăng nhập thành công |
| 3 | Đăng xuất hệ thống | Customer, System | Medium | Detailed | Đăng xuất thành công |
| 4 | Đặt lại mật khẩu | Customer, System | High | Formal | Mật khẩu cập nhật |
| 5 | Cập nhật hồ sơ cá nhân | Customer | Medium | Detailed | Thông tin cập nhật |
| 23 | Quản lý tài khoản user | Admin | High | Formal | Quản lý user thành công |
| 32 | Quên mật khẩu | Guest, System | High | Formal | User được reset password |

---

## 2. Phân hệ Tìm kiếm và Khám phá (Search & Discovery)
Các tính năng hỗ trợ khách hàng tra cứu, lọc thông tin và ra quyết định.

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Kết quả mong đợi |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 6 | Tìm kiếm khách sạn | Guest, Customer, System | Low | Sketch | Danh sách khách sạn phù hợp |
| 7 | Lọc khách sạn | Guest, Customer, System | Medium | Detailed | Hiển thị danh sách phù hợp |
| 8 | Xem chi tiết khách sạn | Guest, Customer | Low | Sketch | Thông tin khách sạn hiển thị |
| 9 | Xem phòng trống | Guest, Customer, System | Medium | Detailed | Danh sách phòng khả dụng |

---

## 3. Phân hệ Đặt phòng và Thanh toán (Booking & Payment Core)
Luồng giao dịch cốt lõi, xử lý các tác vụ đặt chỗ, thanh toán và hoàn hủy.

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Kết quả mong đợi |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 10 | Chọn ngày check-in/out | Customer | Medium | Detailed | Thời gian hợp lệ |
| 11 | Đặt phòng khách sạn | Customer, System | High | Detailed | Booking được tạo |
| 12 | Xác nhận booking | System | High | Formal | Booking confirmed |
| 13 | Thanh toán trực tuyến | Customer, Payment Gateway, System | High | Formal | Thanh toán thành công |
| 14 | Hủy đặt phòng | Customer, System | High | Detailed | Booking bị hủy |
| 15 | Xem lịch sử booking | Customer | Low | Sketch | Hiển thị lịch sử |
| 33 | Tạm giữ phòng (Room Lock) | System, Payment Gateway | High | Formal | Phòng được giữ tạm thời |
| 34 | Hoàn tiền booking | System, Payment Gateway, Admin | High | Formal | Refund thành công |
| 35 | Áp dụng mã giảm giá | Customer, System | Medium | Detailed | Giá booking được giảm |

---

## 4. Phân hệ Quản trị Dữ liệu Khách sạn (Hotel & Room Inventory)
Các chức năng dành cho Admin để thiết lập và cập nhật kho dữ liệu phòng, khách sạn.

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Kết quả mong đợi |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 18 | Thêm khách sạn | Admin | High | Detailed | Khách sạn mới được tạo |
| 19 | Chỉnh sửa khách sạn | Admin | High | Detailed | Dữ liệu cập nhật |
| 20 | Xóa khách sạn | Admin | High | Formal | Hotel bị vô hiệu hóa |
| 21 | Quản lý phòng | Admin | High | Detailed | Danh sách phòng cập nhật |
| 27 | Upload hình ảnh khách sạn | Admin | Medium | Detailed | Hình ảnh cập nhật |

---

## 5. Phân hệ Vận hành và Báo cáo (Operations & Reporting)
Hỗ trợ nghiệp vụ backend, thống kê, kiểm duyệt và xuất báo cáo cho cấp quản lý.

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Kết quả mong đợi |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 22 | Quản lý booking | Admin | High | Detailed | Booking được xử lý |
| 24 | Xem thống kê booking | Admin | Medium | Detailed | Biểu đồ thống kê hiển thị |
| 25 | Xem báo cáo doanh thu | Director, System | High | Detailed | Báo cáo hiển thị |
| 26 | Xem báo cáo sử dụng phòng | Director | Medium | Detailed | Hiển thị tỷ lệ sử dụng phòng |
| 30 | Xuất báo cáo Excel | Admin, System | Medium | Detailed | File tải xuống thành công |
| 31 | Kiểm duyệt đánh giá | Admin | Medium | Detailed | Review được kiểm duyệt |

---

## 6. Phân hệ Tiện ích và Tương tác (Utilities & Interactions)
Các tính năng phụ trợ giúp tăng cường trải nghiệm và hỗ trợ người dùng.

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Kết quả mong đợi |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 16 | Gửi email thông báo | System | Medium | Detailed | Người dùng nhận email |
| 17 | Đánh giá khách sạn | Customer | Medium | Detailed | Đánh giá được lưu |
| 28 | Lưu khách sạn yêu thích | Customer | Low | Sketch | Khách sạn được lưu |
| 29 | Liên hệ hỗ trợ | Customer, Support Staff | Medium | Detailed | Khách hàng nhận hỗ trợ |
