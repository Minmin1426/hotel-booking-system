# Danh sách Features Hệ thống (Features List)

Tài liệu này cấu trúc các nghiệp vụ (Use Case) của hệ thống đặt phòng khách sạn theo **Cấu trúc Package-by-Feature** hiện tại của mã nguồn. Điều này giúp đồng bộ hóa trực tiếp giữa tài liệu nghiệp vụ và các package code trong dự án Spring Boot.

---

## 1. Feature: `auth` (Xác thực & Bảo mật)
*Quản lý đăng ký, đăng nhập, bảo mật phiên làm việc, cấp phát token và phục hồi mật khẩu.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 1 | Đăng ký tài khoản | Guest | Medium | Detailed | Nhập thông tin → Validate → Lưu tài khoản | Tài khoản được tạo | Không trùng email, mật khẩu tối thiểu 8 ký tự |
| 2 | Đăng nhập hệ thống | Guest | High | Formal | Nhập email/password → Xác thực → Tạo token/session | Đăng nhập thành công | Mật khẩu hash BCrypt, khóa tài khoản tạm thời sau 5 lần đăng nhập sai |
| 3 | Đăng xuất hệ thống | Customer | Medium | Detailed | Chọn logout → Hủy token/session | Đăng xuất thành công | Vô hiệu hóa JWT token / session hiện tại |
| 4 | Đặt lại mật khẩu | Customer | High | Formal | Nhập mật khẩu cũ & mới → Validate → Đổi mật khẩu | Mật khẩu cập nhật | Yêu cầu nhập đúng mật khẩu hiện tại |
| 32 | Quên mật khẩu | Guest | High | Formal | Nhập email → Gửi OTP/reset link → Xác thực OTP → Đổi mật khẩu mới | Đặt lại mật khẩu thành công | OTP hết hạn sau 5 phút |

---

## 2. Feature: `user` (Quản lý Người dùng)
*Quản lý thông tin tài khoản cá nhân của khách hàng và danh sách tài khoản bởi Quản trị viên.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 5 | Cập nhật hồ sơ cá nhân | Customer | Medium | Detailed | Chỉnh sửa thông tin cá nhân → Validate → Lưu thay đổi | Thông tin hồ sơ được cập nhật | Kiểm tra định dạng email, số điện thoại hợp lệ |
| 23 | Quản lý tài khoản user | Admin | High | Formal | Xem danh sách tài khoản → Kích hoạt / Khóa tài khoản | Trạng thái tài khoản được cập nhật | Kiểm soát quyền truy cập dựa trên vai trò (RBAC) |

---

## 3. Feature: `hotel` (Khách sạn & Đánh giá)
*Quản lý thông tin khách sạn, hình ảnh, tìm kiếm, bộ lọc và các đánh giá (Reviews) từ người dùng.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 6 | Tìm kiếm khách sạn | Guest | Low | Sketch | Nhập từ khóa (vị trí/tên) → Truy vấn danh sách | Danh sách khách sạn phù hợp | Phân trang (Pagination) tối đa 20 bản ghi/trang |
| 7 | Lọc khách sạn | Guest | Medium | Detailed | Chọn các tiêu chí lọc (giá, tiện ích, xếp hạng) → Tìm kiếm | Kết quả lọc hiển thị | Hỗ trợ sắp xếp theo giá, đánh giá, khoảng cách |
| 8 | Xem chi tiết khách sạn | Guest | Low | Sketch | Chọn khách sạn → Tải dữ liệu chi tiết | Thông tin chi tiết khách sạn hiển thị | Hiển thị hình ảnh, mô tả, tiện ích, danh sách phòng |
| 18 | Thêm khách sạn | Admin | High | Detailed | Nhập thông tin khách sạn → Validate → Lưu | Khách sạn mới được tạo | Xác thực đầy đủ các trường bắt buộc |
| 19 | Chỉnh sửa khách sạn | Admin | High | Detailed | Cập nhật thông tin khách sạn → Kiểm tra → Lưu | Dữ liệu khách sạn được cập nhật | Ghi log lịch sử thay đổi thông tin |
| 20 | Xóa khách sạn | Admin | High | Formal | Yêu cầu xóa khách sạn → Soft delete hệ thống | Trạng thái khách sạn chuyển sang Disabled | Chỉ cho phép xóa khi không còn booking nào đang hoạt động |
| 27 | Upload hình ảnh khách sạn | Admin | Medium | Detailed | Chọn file ảnh → Validate định dạng/kích thước → Lưu trữ | Hình ảnh được liên kết với khách sạn | Chỉ chấp nhận định dạng jpg, png, webp |
| 31 | Kiểm duyệt đánh giá | Admin | Medium | Detailed | Duyệt danh sách review → Ẩn/Xóa đánh giá vi phạm tiêu chuẩn | Trạng thái review thay đổi | Lưu vết hoạt động kiểm duyệt (Audit Log) |

---

## 4. Feature: `room` (Quản lý Phòng & Khóa phòng)
*Quản lý thông tin phòng trống, cấu hình chi tiết phòng, giá cả và cơ chế tạm giữ phòng (Room Lock) trong quá trình giao dịch.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 9 | Xem phòng trống | Guest | Medium | Detailed | Chọn ngày nhận/trả phòng → Truy vấn phòng khả dụng | Danh sách phòng trống hiển thị | Dữ liệu trạng thái phòng cập nhật theo thời gian thực |
| 21 | Quản lý phòng | Admin | High | Detailed | Thêm/Sửa/Xóa cấu hình phòng của khách sạn | Thông tin phòng được cập nhật | Quản lý loại phòng, số lượng, giá cơ bản |
| 33 | Tạm giữ phòng (Room Lock) | System | High | Formal | Khách hàng tiến hành thanh toán → Giữ phòng tạm thời | Phòng được khóa trong thời gian thanh toán | Tự động giải phóng (release) sau 10 phút nếu thanh toán thất bại |

---

## 5. Feature: `booking` (Quản lý Đặt phòng)
*Xử lý quy trình đặt phòng, cấu hình thời gian đặt phòng, xác nhận và hủy đặt phòng.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 10 | Chọn ngày check-in/check-out | Customer | Medium | Detailed | Lựa chọn ngày check-in/check-out → Validate tính hợp lệ | Ngày đặt phòng hợp lệ | Ngày đặt không được ở quá khứ, check-out phải sau check-in |
| 11 | Đặt phòng khách sạn | Customer | High | Detailed | Chọn phòng và thời gian → Tạo thông tin đặt phòng | Booking mới được tạo (chờ thanh toán) | Ràng buộc tránh đặt trùng phòng trong cùng khoảng thời gian |
| 12 | Xác nhận booking | System | High | Formal | Hệ thống nhận kết quả thanh toán → Tự động xác nhận đặt phòng | Trạng thái đặt phòng chuyển sang Confirmed | Chỉ tự động xác nhận khi thanh toán trực tuyến thành công |
| 14 | Hủy đặt phòng | Customer | High | Detailed | Chọn đơn đặt phòng → Kiểm tra điều kiện hủy → Xác nhận hủy | Đơn đặt phòng bị hủy | Tính toán tiền hoàn lại (refund) theo chính sách hủy phòng |
| 15 | Xem lịch sử booking | Customer | Low | Sketch | Gửi yêu cầu xem danh sách đặt phòng → Truy vấn | Lịch sử đặt phòng hiển thị | Phân trang (Pagination) tối đa 20 bản ghi/trang |
| 22 | Quản lý booking | Admin | High | Detailed | Duyệt danh sách đặt phòng → Xác nhận/Từ chối thủ công | Trạng thái đơn đặt phòng được cập nhật | Sử dụng cho trường hợp thanh toán trực tiếp tại khách sạn (offline booking) |

---

## 6. Feature: `payment` (Giao dịch & Ghi hóa đơn)
*Tích hợp cổng thanh toán trực tuyến, đối soát giao dịch và thực hiện hoàn tiền.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 13 | Thanh toán trực tuyến | Customer | High | Formal | Gửi yêu cầu thanh toán → Cổng thanh toán xử lý → Nhận IPN/Webhook → Cập nhật giao dịch | Đơn hàng được thanh toán | Xác thực mã bảo mật HMAC signature, ngăn chặn thanh toán trùng lặp |
| 34 | Hoàn tiền booking | System | High | Formal | Hủy phòng hợp lệ kích hoạt → Gửi yêu cầu hoàn tiền → Xử lý hoàn tiền | Số tiền được hoàn về tài khoản khách hàng | Tự động thử lại (retry) nếu giao dịch hoàn tiền bị lỗi |

---

## 7. Feature: `voucher` (Chương trình Khuyến mãi)
*Quản lý mã giảm giá, áp dụng ưu đãi vào hóa đơn đặt phòng của khách hàng.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 35 | Áp dụng mã giảm giá | Customer | Medium | Detailed | Nhập mã giảm giá → Validate điều kiện → Khấu trừ vào tổng tiền | Giá trị đơn đặt phòng được giảm trực tiếp | Mã giảm giá phải còn hạn sử dụng, đúng đối tượng và chưa vượt quá giới hạn lượt dùng |

---

## 8. Feature: `report` (Thống kê & Báo cáo)
*Khai thác dữ liệu kinh doanh, lập báo cáo doanh thu, công suất sử dụng phòng và kết xuất tài liệu báo cáo.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 24 | Xem thống kê booking | Admin | Medium | Detailed | Lựa chọn mốc thời gian → Truy vấn tổng số booking | Biểu đồ thống kê hiển thị trên Dashboard | Thống kê số lượng đơn thành công, bị hủy, chờ duyệt |
| 25 | Xem báo cáo doanh thu | Director | High | Detailed | Lựa chọn chu kỳ báo cáo → Tính toán doanh thu thực tế | Báo cáo doanh thu chi tiết | Hỗ trợ phân tích theo ngày, tuần, tháng, quý, năm |
| 26 | Xem báo cáo sử dụng phòng | Director | Medium | Detailed | Tổng hợp dữ liệu công suất đặt phòng của các phòng | Biểu đồ tỉ lệ lấp đầy phòng hiển thị | Cung cấp thông tin loại phòng được đặt nhiều nhất |
| 30 | Xuất báo cáo Excel | Admin | Medium | Detailed | Chọn loại báo cáo cần xuất → Hệ thống sinh file Excel | File Excel tải xuống thành công | Đảm bảo tốc độ xuất file không quá 5 giây |

---

## 9. Feature: `setting` (Cài đặt Hệ thống)
*Quản lý các cấu hình tham số hoạt động toàn hệ thống.*
*(Phân hệ này hiện tại dùng để quản lý cấu hình hệ thống chung, sẽ được bổ sung các nghiệp vụ quản trị chi tiết ở các phiên bản tiếp theo).*
