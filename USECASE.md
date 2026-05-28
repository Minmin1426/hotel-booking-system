# Danh sách Đặc tả Use Case Chi tiết theo Vai trò (Role)

Tài liệu này tổng hợp chi tiết các nghiệp vụ (Use Case) của hệ thống đặt phòng khách sạn. Nhằm đáp ứng nguyên tắc thiết kế hệ thống, tài liệu tuân thủ nghiêm ngặt hai điều kiện:
1. **Mỗi nghiệp vụ chỉ được thực hiện bởi duy nhất một vai trò chính (Primary Actor)** đóng vai trò khởi tạo luồng xử lý.
2. **Loại bỏ hoàn toàn các nghiệp vụ thuộc phân hệ tiện ích và tương tác** (Gửi email thông báo, Đánh giá khách sạn, Lưu khách sạn yêu thích, Liên hệ hỗ trợ).

---

## 1. Role: Guest (Khách vãng lai - Chưa đăng nhập)
*Khởi tạo các tác vụ tìm kiếm cơ bản và yêu cầu cấp quyền truy cập hệ thống.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 1 | Đăng ký tài khoản | Guest | Medium | Detailed | Nhập thông tin → Validate → Lưu tài khoản | Tài khoản được tạo | Không trùng email, mật khẩu tối thiểu 8 ký tự |
| 2 | Đăng nhập hệ thống | Guest | High | Formal | Nhập email/password → Xác thực → Tạo session | Đăng nhập thành công | Password hash bcrypt, khóa tài khoản sau 5 lần sai |
| 6 | Tìm kiếm khách sạn | Guest | Low | Sketch | Nhập địa điểm → Truy vấn dữ liệu | Danh sách khách sạn phù hợp | Pagination tối đa 20 records/trang |
| 7 | Lọc khách sạn | Guest | Medium | Detailed | Chọn bộ lọc → Xử lý dữ liệu | Hiển thị danh sách phù hợp | Hỗ trợ sort theo giá, đánh giá, khoảng cách |
| 8 | Xem chi tiết khách sạn | Guest | Low | Sketch | Chọn khách sạn → Hiển thị thông tin | Thông tin khách sạn hiển thị | Bao gồm hình ảnh, tiện ích, đánh giá |
| 9 | Xem phòng trống | Guest | Medium | Detailed | Chọn ngày → Kiểm tra phòng | Danh sách phòng khả dụng | Availability cập nhật real-time |
| 32 | Quên mật khẩu | Guest | High | Formal | Nhập email → Gửi OTP/reset link → Verify | User được reset password | OTP hết hạn sau 5 phút |

---

## 2. Role: Customer (Khách hàng - Đã xác thực)
*Thực hiện các luồng giao dịch tài chính, quản lý lịch trình đặt phòng và hồ sơ cá nhân.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 3 | Đăng xuất hệ thống | Customer | Medium | Detailed | Chọn logout → Xóa session/token | Đăng xuất thành công | Hủy toàn bộ session đang hoạt động |
| 4 | Đặt lại mật khẩu | Customer | High | Formal | Nhập email → Gửi OTP/link → Xác nhận → Đổi mật khẩu | Mật khẩu cập nhật | OTP hết hạn sau 5 phút |
| 5 | Cập nhật hồ sơ cá nhân | Customer | Medium | Detailed | Chỉnh sửa thông tin → Validate → Lưu | Thông tin cập nhật | Kiểm tra định dạng email/số điện thoại |
| 10 | Chọn ngày check-in/check-out | Customer | Medium | Detailed | Chọn ngày → Validate thời gian | Thời gian hợp lệ | Không cho chọn ngày quá khứ |
| 11 | Đặt phòng khách sạn | Customer | High | Detailed | Chọn phòng → Nhập thông tin → Tạo booking | Booking được tạo | Không cho booking trùng thời gian |
| 13 | Thanh toán trực tuyến | Customer | High | Formal | Tạo payment request → Gateway xử lý → Verify webhook → Cập nhật trạng thái | Thanh toán thành công | Verify HMAC signature, chống duplicate payment |
| 14 | Hủy đặt phòng | Customer | High | Detailed | Chọn booking → Kiểm tra điều kiện → Hủy booking | Booking bị hủy | Refund theo chính sách |
| 15 | Xem lịch sử booking | Customer | Low | Sketch | Truy vấn booking | Hiển thị lịch sử | Pagination tối đa 20 records/trang |
| 35 | Áp dụng mã giảm giá | Customer | Medium | Detailed | Nhập voucher → Validate → Tính discount | Giá booking được giảm | Voucher phải còn hiệu lực |

---

## 3. Role: Admin (Quản trị viên)
*Quản lý toàn bộ kho dữ liệu hệ thống (Inventory), phân quyền cấu hình và kiểm duyệt vận hành.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 18 | Thêm khách sạn | Admin | High | Detailed | Nhập thông tin → Validate → Lưu | Khách sạn mới được tạo | Kiểm tra dữ liệu bắt buộc |
| 19 | Chỉnh sửa khách sạn | Admin | High | Detailed | Chỉnh sửa dữ liệu → Lưu thay đổi | Dữ liệu cập nhật | Lưu lịch sử chỉnh sửa |
| 20 | Xóa khách sạn | Admin | High | Formal | Soft delete hotel → Disable room liên quan | Hotel bị vô hiệu hóa | Không xóa nếu còn booking active |
| 21 | Quản lý phòng | Admin | High | Detailed | Thêm/Sửa/Xóa phòng | Danh sách phòng cập nhật | Quản lý giá và trạng thái phòng |
| 22 | Quản lý booking | Admin | High | Detailed | Xác nhận/Từ chối booking | Booking được xử lý | Chỉ xử lý manual/offline booking |
| 23 | Quản lý tài khoản user | Admin | High | Formal | Xem danh sách → Khóa/Mở tài khoản | Quản lý user thành công | Phân quyền RBAC |
| 24 | Xem thống kê booking | Admin | Medium | Detailed | Chọn thời gian → Tổng hợp dữ liệu | Biểu đồ thống kê hiển thị | Hỗ trợ dashboard |
| 27 | Upload hình ảnh khách sạn | Admin | Medium | Detailed | Upload ảnh → Validate → Lưu | Hình ảnh cập nhật | Chỉ cho phép jpg/png/webp |
| 30 | Xuất báo cáo Excel | Admin | Medium | Detailed | Chọn báo cáo → Export file | File tải xuống thành công | Thời gian xử lý ≤ 5 giây |
| 31 | Kiểm duyệt đánh giá | Admin | Medium | Detailed | Xem review → Ẩn/Xóa review vi phạm | Review được kiểm duyệt | Lưu audit log |

---

## 4. Role: Director (Giám đốc / Ban quản lý cấp cao)
*Theo dõi hiệu suất kinh doanh, chiến lược doanh thu và hiệu quả khai thác vận hành.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 25 | Xem báo cáo doanh thu | Director | High | Detailed | Chọn thời gian → Sinh báo cáo | Báo cáo hiển thị | Theo ngày/tháng/quý/năm |
| 26 | Xem báo cáo sử dụng phòng | Director | Medium | Detailed | Thống kê dữ liệu | Hiển thị tỷ lệ sử dụng phòng | Hỗ trợ export Excel |

---

## 5. Role: System (Hệ thống tự động)
*Xử lý các tác vụ ngầm dựa trên sự kiện kích hoạt (Event-driven) và ràng buộc thời gian (Cron-job).*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 12 | Xác nhận booking | System | High | Formal | Kiểm tra payment → Auto confirm booking | Booking confirmed | Chỉ auto-confirm với online payment success |
| 33 | Tạm giữ phòng (Room Lock) | System | High | Formal | Lock room khi payment processing | Phòng được giữ tạm thời | Auto release sau 10 phút |
| 34 | Hoàn tiền booking | System | High | Formal | Trigger refund request → Verify refund | Refund thành công | Retry nếu refund thất bại |
