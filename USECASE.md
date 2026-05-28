# Danh sách Use Case phân chia theo Vai trò (Role)

Tài liệu này định nghĩa các Use Case dựa trên nguyên tắc **1 nghiệp vụ chỉ được thực hiện bởi 1 role (Primary Actor)**, tức là người hoặc hệ thống chủ động khởi tạo tác vụ đó. Các nghiệp vụ thuộc phân hệ tiện ích và tương tác đã được loại bỏ.

---

## 1. Role: Guest (Khách vãng lai - Chưa đăng nhập)
*Khởi tạo các tác vụ tìm kiếm cơ bản và yêu cầu cấp quyền truy cập hệ thống.*

* **1.** Đăng ký tài khoản
* **2.** Đăng nhập hệ thống
* **6.** Tìm kiếm khách sạn
* **7.** Lọc khách sạn
* **8.** Xem chi tiết khách sạn
* **9.** Xem phòng trống
* **32.** Quên mật khẩu

---

## 2. Role: Customer (Khách hàng - Đã xác thực)
*Thực hiện các luồng giao dịch, quản lý tài khoản cá nhân và thanh toán.*

* **3.** Đăng xuất hệ thống
* **4.** Đặt lại mật khẩu
* **5.** Cập nhật hồ sơ cá nhân
* **10.** Chọn ngày check-in/check-out
* **11.** Đặt phòng khách sạn
* **13.** Thanh toán trực tuyến
* **14.** Hủy đặt phòng
* **15.** Xem lịch sử booking
* **35.** Áp dụng mã giảm giá

---

## 3. Role: Admin (Quản trị viên)
*Thực hiện các thao tác quản trị dữ liệu (CRUD), kiểm duyệt và xuất báo cáo vận hành.*

* **18.** Thêm khách sạn
* **19.** Chỉnh sửa khách sạn
* **20.** Xóa khách sạn
* **21.** Quản lý phòng
* **22.** Quản lý booking
* **23.** Quản lý tài khoản user
* **24.** Xem thống kê booking
* **27.** Upload hình ảnh khách sạn
* **30.** Xuất báo cáo Excel
* **31.** Kiểm duyệt đánh giá

---

## 4. Role: Director (Giám đốc/Cấp quản lý)
*Xem các báo cáo chiến lược và hiệu quả kinh doanh.*

* **25.** Xem báo cáo doanh thu
* **26.** Xem báo cáo sử dụng phòng

---

## 5. Role: System (Hệ thống tự động)
*Xử lý ngầm các nghiệp vụ kích hoạt tự động theo logic lập trình mà không cần sự can thiệp trực tiếp của con người.*

* **12.** Xác nhận booking
* **33.** Tạm giữ phòng (Room Lock)
* **34.** Hoàn tiền booking
