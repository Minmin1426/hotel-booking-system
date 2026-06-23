# Danh sách Đặc tả Use Case Chi tiết theo Vai trò 

Tài liệu này tổng hợp chi tiết các nghiệp vụ (Use Case) của hệ thống đặt phòng khách sạn. Nhằm đáp ứng nguyên tắc thiết kế hệ thống, tài liệu tuân thủ nghiêm ngặt điều kiện:
**Mỗi nghiệp vụ chỉ được thực hiện bởi duy nhất một vai trò chính (Primary Actor)** đóng vai trò khởi tạo luồng xử lý.

Các nghiệp vụ dưới đây được phân loại và ánh xạ trực tiếp đến các feature tương ứng trong cấu trúc package (`auth`, `user`, `hotel`, `room`, `booking`, `payment`, `voucher`, `report`, `setting`) để đảm bảo tính nhất quán.

---

## 1. Role: Guest (Khách vãng lai - Chưa đăng nhập)
*Khởi tạo các tác vụ tìm kiếm cơ bản và yêu cầu cấp quyền truy cập hệ thống.*

| STT | Nghiệp vụ | Feature | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 1 | Đăng ký tài khoản | `auth` | Medium | Detailed | Nhập thông tin → Validate → Lưu tài khoản | Tài khoản được tạo | Không trùng email, mật khẩu tối thiểu 8 ký tự |
| 2 | Đăng nhập hệ thống | `auth` | High | Formal | Nhập email/password → Xác thực → Tạo token/session | Đăng nhập thành công | Mật khẩu hash BCrypt, khóa tài khoản tạm thời sau 5 lần đăng nhập sai |
| 6 | Tìm kiếm khách sạn | `hotel` | Low | Sketch | Nhập từ khóa (vị trí/tên) → Truy vấn danh sách | Danh sách khách sạn phù hợp | Phân trang (Pagination) tối đa 20 bản ghi/trang |
| 7 | Lọc khách sạn | `hotel` | Medium | Detailed | Chọn các tiêu chí lọc (giá, tiện ích, xếp hạng) → Tìm kiếm | Kết quả lọc hiển thị | Hỗ trợ sắp xếp theo giá, đánh giá, khoảng cách |
| 8 | Xem chi tiết khách sạn | `hotel` | Low | Sketch | Chọn khách sạn → Tải dữ liệu chi tiết | Thông tin chi tiết khách sạn hiển thị | Hiển thị hình ảnh, mô tả, tiện ích, danh sách phòng |
| 9 | Xem phòng trống | `room` | Medium | Detailed | Chọn ngày nhận/trả phòng → Truy vấn phòng khả dụng | Danh sách phòng trống hiển thị | Dữ liệu trạng thái phòng cập nhật theo thời gian thực |
| 32 | Quên mật khẩu | `auth` | High | Formal | Nhập email → Gửi OTP/reset link → Xác thực OTP → Đổi mật khẩu mới | Đặt lại mật khẩu thành công | OTP hết hạn sau 5 phút |

---

## 2. Role: Customer (Khách hàng - Đã xác thực)
*Thực hiện các luồng giao dịch tài chính, quản lý lịch trình đặt phòng và hồ sơ cá nhân.*

| STT | Nghiệp vụ | Feature | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 3 | Đăng xuất hệ thống | `auth` | Medium | Detailed | Chọn logout → Hủy token/session | Đăng xuất thành công | Vô hiệu hóa JWT token / session hiện tại |
| 4 | Đặt lại mật khẩu | `auth` | High | Formal | Nhập mật khẩu cũ & mới → Validate → Đổi mật khẩu | Mật khẩu cập nhật | Yêu cầu nhập đúng mật khẩu hiện tại |
| 5 | Cập nhật hồ sơ cá nhân | `user` | Medium | Detailed | Chỉnh sửa thông tin cá nhân → Validate → Lưu thay đổi | Thông tin hồ sơ được cập nhật | Kiểm tra định dạng email, số điện thoại hợp lệ |
| 10 | Chọn ngày check-in/check-out | `booking` | Medium | Detailed | Lựa chọn ngày check-in/check-out → Validate tính hợp lệ | Ngày đặt phòng hợp lệ | Ngày đặt không được ở quá khứ, check-out phải sau check-in |
| 11 | Đặt phòng khách sạn | `booking` | High | Detailed | Chọn phòng và thời gian → Tạo thông tin đặt phòng | Booking mới được tạo (chờ thanh toán) | Ràng buộc tránh đặt trùng phòng trong cùng khoảng thời gian |
| 13 | Thanh toán trực tuyến | `payment` | High | Formal | Gửi yêu cầu thanh toán → Cổng thanh toán xử lý → Nhận IPN/Webhook → Cập nhật giao dịch | Đơn hàng được thanh toán | Xác thực mã bảo mật HMAC signature, ngăn chặn thanh toán trùng lặp |
| 14 | Hủy đặt phòng | `booking` | High | Detailed | Chọn đơn đặt phòng → Kiểm tra điều kiện hủy → Xác nhận hủy | Đơn đặt phòng bị hủy | Tính toán tiền hoàn lại (refund) theo chính sách hủy phòng |
| 15 | Xem lịch sử booking | `booking` | Low | Sketch | Gửi yêu cầu xem danh sách đặt phòng → Truy vấn | Lịch sử đặt phòng hiển thị | Phân trang (Pagination) tối đa 20 bản ghi/trang |
| 35 | Áp dụng mã giảm giá | `voucher` | Medium | Detailed | Nhập mã giảm giá → Validate điều kiện → Khấu trừ vào tổng tiền | Giá trị đơn đặt phòng được giảm trực tiếp | Mã giảm giá phải còn hạn sử dụng, đúng đối tượng và chưa vượt quá giới hạn lượt dùng |

---

## 3. Role: Admin (Quản trị viên)
*Quản lý toàn bộ kho dữ liệu hệ thống (Inventory), phân quyền cấu hình và kiểm duyệt vận hành.*

| STT | Nghiệp vụ | Feature | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 18 | Thêm khách sạn | `hotel` | High | Detailed | Nhập thông tin khách sạn → Validate → Lưu | Khách sạn mới được tạo | Xác thực đầy đủ các trường bắt buộc |
| 19 | Chỉnh sửa khách sạn | `hotel` | High | Detailed | Cập nhật thông tin khách sạn → Kiểm tra → Lưu | Dữ liệu khách sạn được cập nhật | Ghi log lịch sử thay đổi thông tin |
| 20 | Xóa khách sạn | `hotel` | High | Formal | Yêu cầu xóa khách sạn → Soft delete hệ thống | Trạng thái khách sạn chuyển sang Disabled | Chỉ cho phép xóa khi không còn booking nào đang hoạt động |
| 21 | Quản lý phòng | `room` | High | Detailed | Thêm/Sửa/Xóa cấu hình phòng của khách sạn | Thông tin phòng được cập nhật | Quản lý loại phòng, số lượng, giá cơ bản |
| 22 | Quản lý booking | `booking` | High | Detailed | Duyệt danh sách đặt phòng → Xác nhận/Từ chối thủ công | Trạng thái đơn đặt phòng được cập nhật | Sử dụng cho trường hợp thanh toán trực tiếp tại khách sạn (offline booking) |
| 23 | Quản lý tài khoản user | `user` | High | Formal | Xem danh sách tài khoản → Kích hoạt / Khóa tài khoản | Trạng thái tài khoản được cập nhật | Kiểm soát quyền truy cập dựa trên vai trò (RBAC) |
| 24 | Xem thống kê booking | `report` | Medium | Detailed | Lựa chọn mốc thời gian → Truy vấn tổng số booking | Biểu đồ thống kê hiển thị trên Dashboard | Thống kê số lượng đơn thành công, bị hủy, chờ duyệt |
| 27 | Upload hình ảnh khách sạn | `hotel` | Medium | Detailed | Chọn file ảnh → Validate định dạng/kích thước → Lưu trữ | Hình ảnh được liên kết với khách sạn | Chỉ chấp nhận định dạng jpg, png, webp |
| 30 | Xuất báo cáo Excel | `report` | Medium | Detailed | Chọn loại báo cáo cần xuất → Hệ thống sinh file Excel | File Excel tải xuống thành công | Đảm bảo tốc độ xuất file không quá 5 giây |
| 31 | Kiểm duyệt đánh giá | `hotel` | Medium | Detailed | Duyệt danh sách review → Ẩn/Xóa đánh giá vi phạm tiêu chuẩn | Trạng thái review thay đổi | Lưu vết hoạt động kiểm duyệt (Audit Log) |

---

## 4. Role: Director (Giám đốc / Ban quản lý cấp cao)
*Theo dõi hiệu suất kinh doanh, chiến lược doanh thu và hiệu quả khai thác vận hành.*

| STT | Nghiệp vụ | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 25 | Xem báo cáo doanh thu | `report` | High | Detailed | Lựa chọn chu kỳ báo cáo → Tính toán doanh thu thực tế | Báo cáo doanh thu chi tiết | Hỗ trợ phân tích theo ngày, tuần, tháng, quý, năm |
| 26 | Xem báo cáo sử dụng phòng | `report` | Medium | Detailed | Tổng hợp dữ liệu công suất đặt phòng của các phòng | Biểu đồ tỉ lệ lấp đầy phòng hiển thị | Cung cấp thông tin loại phòng được đặt nhiều nhất |

---

## 5. Role: System (Hệ thống tự động)
*Xử lý các tác vụ ngầm dựa trên sự kiện kích hoạt (Event-driven) và ràng buộc thời gian (Cron-job).*

| STT | Nghiệp vụ | Feature | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :--- | :--- | :--- |
| 12 | Xác nhận booking | `booking` | High | Formal | Hệ thống nhận kết quả thanh toán → Tự động xác nhận đặt phòng | Trạng thái đặt phòng chuyển sang Confirmed | Chỉ tự động xác nhận khi thanh toán trực tuyến thành công |
| 33 | Tạm giữ phòng (Room Lock) | `room` | High | Formal | Khách hàng tiến hành thanh toán → Giữ phòng tạm thời | Phòng được khóa trong thời gian thanh toán | Tự động giải phóng (release) sau 10 phút nếu thanh toán thất bại |
| 34 | Hoàn tiền booking | `payment` | High | Formal | Hủy phòng hợp lệ kích hoạt → Gửi yêu cầu hoàn tiền → Xử lý hoàn tiền | Số tiền được hoàn về tài khoản khách hàng | Tự động thử lại (retry) nếu giao dịch hoàn tiền bị lỗi |
