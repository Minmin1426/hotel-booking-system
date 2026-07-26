# Danh sách Features System (Features List & Project Tracking)

Tài liệu này cấu trúc các nghiệp vụ (Use Cases) của hệ thống đặt phòng khách sạn theo **Cấu trúc Package-by-Feature** hiện tại của mã nguồn Spring Boot, kết hợp hệ thống **50 Màn Hình (SCR-101 đến SCR-510)**.

> 📌 **Chi tiết Tiến độ Dự án & Ma trận 50 Màn hình**: Xem tại tài liệu **[PROJECT_TRACKING.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/PROJECT_TRACKING.md)**.

---

## 1. Feature: `auth` (Xác thực & Bảo mật)
*Quản lý đăng ký, đăng nhập, bảo mật phiên làm việc, cấp phát token và phục hồi mật khẩu.*

| STT | Nghiệp vụ | Mã Screen | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :---: | :--- | :--- | :--- |
| 1 | Đăng ký tài khoản | `SCR-101` | Guest | Medium | Detailed | Nhập thông tin → Validate → Lưu tài khoản | Tài khoản được tạo | Không trùng email, mật khẩu tối thiểu 8 ký tự |
| 2 | Đăng nhập hệ thống | `SCR-101` | Guest | High | Formal | Nhập email/password → Xác thực → Tạo token/session | Đăng nhập thành công | Mật khẩu hash BCrypt, khóa tài khoản tạm thời sau 5 lần đăng nhập sai |
| 3 | Đăng xuất hệ thống | Header | Customer | Medium | Detailed | Chọn logout → Hủy token/session | Đăng xuất thành công | Vô hiệu hóa JWT token / session hiện tại |
| 4 | Đặt lại mật khẩu | `SCR-101` | Customer | High | Formal | Nhập mật khẩu cũ & mới → Validate → Đổi mật khẩu | Mật khẩu cập nhật | Yêu cầu nhập đúng mật khẩu hiện tại |
| 32 | Quên mật khẩu | `SCR-101` | Guest | High | Formal | Nhập email → Gửi OTP/reset link → Xác thực OTP → Đổi mật khẩu mới | Đặt lại mật khẩu thành công | OTP hết hạn sau 5 phút |

---

## 2. Feature: `user` (Quản lý Người dùng & Doanh nghiệp)
*Quản lý thông tin tài khoản cá nhân, hồ sơ thuế doanh nghiệp CTP và danh sách tài khoản bởi Quản trị viên.*

| STT | Nghiệp vụ | Mã Screen | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :---: | :--- | :--- | :--- |
| 5 | Cập nhật hồ sơ cá nhân | `SCR-102` | Customer | Medium | Detailed | Chỉnh sửa thông tin cá nhân → Validate → Lưu thay đổi | Thông tin hồ sơ được cập nhật | Kiểm tra định dạng email, số điện thoại hợp lệ |
| 23 | Quản lý tài khoản user | `SCR-110` | Admin | High | Formal | Xem danh sách tài khoản → Kích hoạt / Khóa tài khoản | Trạng thái tài khoản được cập nhật | Kiểm soát quyền truy cập dựa trên vai trò (RBAC) |
| 36 | Hồ sơ Thuế Doanh nghiệp (CTP) | `SCR-102`, `SCR-408` | Customer / Admin | High | Formal | Nhập MST & Tên Công Ty → Xác minh CTP → Xuất Hóa đơn Red VAT | Kích hoạt xuất Hóa đơn VAT | Phê duyệt MST doanh nghiệp hợp lệ cho các đoàn khách |

---

## 3. Feature: `hotel` (Khách sạn, Bộ Lọc & Đánh Giá)
*Quản lý thông tin khách sạn, hình ảnh, tìm kiếm, bộ lọc combo và các đánh giá (Reviews) từ người dùng.*

| STT | Nghiệp vụ | Mã Screen | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :---: | :--- | :--- | :--- |
| 6 | Tìm kiếm khách sạn | `SCR-301` | Guest | Low | Sketch | Nhập từ khóa (vị trí/tên) → Truy vấn danh sách | Danh sách khách sạn phù hợp | Phân trang (Pagination) tối đa 20 bản ghi/trang |
| 7 | Lọc khách sạn theo Combo | `SCR-302` | Guest | Medium | Detailed | Chọn các tiêu chí lọc (giá, tiện ích, xếp hạng, dịch vụ đoàn) → Tìm kiếm | Kết quả lọc hiển thị | Hỗ trợ sắp xếp theo giá, đánh giá, khoảng cách |
| 8 | Xem chi tiết khách sạn | `SCR-303` | Guest | Low | Sketch | Chọn khách sạn → Tải dữ liệu chi tiết | Thông tin chi tiết khách sạn hiển thị | Hiển thị hình ảnh, mô tả, tiện ích, danh sách phòng |
| 18 | Thêm khách sạn | `SCR-202` | Admin | High | Detailed | Nhập thông tin khách sạn → Validate → Lưu | Khách sạn mới được tạo | Xác thực đầy đủ các trường bắt buộc |
| 19 | Chỉnh sửa khách sạn | `SCR-201` | Admin | High | Detailed | Cập nhật thông tin khách sạn → Kiểm tra → Lưu | Dữ liệu khách sạn được cập nhật | Ghi log lịch sử thay đổi thông tin |
| 20 | Xóa khách sạn | `SCR-201` | Admin | High | Formal | Yêu cầu xóa khách sạn → Soft delete hệ thống | Trạng thái khách sạn chuyển sang Disabled | Chỉ cho phép xóa khi không còn booking nào đang hoạt động |
| 27 | Upload hình ảnh khách sạn | `SCR-202` | Admin | Medium | Detailed | Chọn file ảnh → Validate định dạng/kích thước → Lưu trữ | Hình ảnh được liên kết với khách sạn | Chỉ chấp nhận định dạng jpg, png, webp |
| 31 | Kiểm duyệt đánh giá | `SCR-209` | Admin | Medium | Detailed | Duyệt danh sách review → Ẩn/Xóa đánh giá vi phạm tiêu chuẩn | Trạng thái review thay đổi | Lưu vết hoạt động kiểm duyệt (Audit Log) |

---

## 4. Feature: `room` (Quản lý Phòng & Ma Trận Room Matrix)
*Quản lý thông tin phòng trống, cấu hình chi tiết phòng, sơ đồ phòng real-time và cơ chế tạm giữ phòng (Room Lock).*

| STT | Nghiệp vụ | Mã Screen | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :---: | :--- | :--- | :--- |
| 9 | Xem phòng trống | `SCR-203` | Guest | Medium | Detailed | Chọn ngày nhận/trả phòng → Truy vấn phòng khả dụng | Danh sách phòng trống hiển thị | Dữ liệu trạng thái phòng cập nhật theo thời gian thực |
| 21 | Quản lý ma trận phòng | `SCR-204` | Housekeeper / Admin | High | Detailed | Xem sơ đồ phòng real-time → Đổi trạng thái Sạch/Bẩn/Occupied | Sơ đồ phòng cập nhật | Phục vụ điều phối lễ tân & dọn phòng |
| 33 | Tạm giữ phòng (Room Lock) | `SCR-307` | System | High | Formal | Khách hàng tiến hành thanh toán → Giữ phòng tạm thời | Phòng được khóa trong thời gian thanh toán | Tự động giải phóng (release) từ 10 đến 30 phút qua Scheduler |

---

## 5. Feature: `booking` (Đặt Phòng Lẻ & Đặt Đoàn >5 Phòng)
*Xử lý quy trình đặt phòng lẻ & đặt đoàn, tự động giảm 25%, đặt cọc 30%, xác nhận và hủy đặt phòng.*

| STT | Nghiệp vụ | Mã Screen | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :---: | :--- | :--- | :--- |
| 10 | Chọn ngày check-in/check-out | `SCR-304` | Customer | Medium | Detailed | Lựa chọn ngày check-in/check-out → Validate tính hợp lệ | Ngày đặt phòng hợp lệ | Ngày đặt không được ở quá khứ, check-out phải sau check-in |
| 11 | Đặt phòng & Đặt đoàn (>5 Phòng) | `SCR-304` | Customer | High | Detailed | Chọn >5 phòng → Tự động chiết khấu 25% → Chọn cọc 30% | Booking đoàn mới được tạo | Hỗ trợ cọc 30% Deposit & đính kèm danh sách đoàn |
| 12 | Xác nhận booking & Mã QR Tổng | `SCR-307` | System / Receptionist | High | Formal | Hệ thống nhận kết quả thanh toán/cọc → Tạo mã QR Đơn | Trạng thái đặt phòng chuyển sang Confirmed | Tự động sinh mã QR Check-in & QR Vé ăn |
| 14 | Hủy đặt phòng & Hoàn tiền Ví | `SCR-108` | Customer | High | Detailed | Chọn đơn đặt phòng → Tính % hoàn tiền → Hoàn về Ví | Đơn đặt phòng bị hủy, tiền cộng vào Ví | Hoàn tiền 100%, 80%, 50%, 0% theo lead time |
| 15 | Xem lịch sử booking | `SCR-108` | Customer | Low | Sketch | Gửi yêu cầu xem danh sách đặt phòng → Truy vấn | Lịch sử đặt phòng hiển thị | Phân trang (Pagination) tối đa 20 bản ghi/trang |
| 22 | Quản lý booking & Lễ tân Check-in | `SCR-308`, `SCR-309` | Receptionist / Admin | High | Detailed | Check-in đoàn cấp tốc → Phát thẻ phòng & QR suất ăn | Trạng thái đơn đặt phòng cập nhật | Phục vụ lễ tân tiếp nhận & dọn phòng |

---

## 6. Feature: `payment` (Giao dịch, Ví Điện Tử & VAT Invoice)
*Thanh toán trực tuyến Stripe/VNPay, Ví Điện Tử cá nhân, đặt cọc đoàn 30% và xuất Hóa đơn Red VAT Doanh nghiệp.*

| STT | Nghiệp vụ | Mã Screen | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :---: | :--- | :--- | :--- |
| 13 | Thanh toán trực tuyến & Cọc 30% | `SCR-401`, `SCR-402` | Customer | High | Formal | Chọn Stripe / VNPay / Ví → Xử lý thanh toán cọc 30% | Biên lai điện tử sinh ra | Mã hóa HMAC, ngăn chặn cọc trùng lặp |
| 34 | Hoàn tiền về Ví Điện Tử | `SCR-105`, `SCR-404` | System | High | Formal | Hủy phòng hợp lệ → Tiền cộng trực tiếp vào Ví | Số dư ví cập nhật tức thì | Khách có thể dùng số dư ví cho booking sau |
| 37 | Nạp tiền Ví & Hạn mức Chi tiêu | `SCR-106`, `SCR-407` | Customer | Medium | Detailed | Nạp tiền vào Ví → Cấu hình Hạn mức chi tiêu ngày | Số dư ví tăng, bảo mật hạn mức | Tránh chi tiêu vượt định mức cho thành viên đoàn |

---

## 7. Feature: `voucher` & `customer-portal` (Khuyến Mãi, Vé Ăn & Loyalty)
*Quản lý mã giảm giá, thẻ hội viên thân thiết (Platinum VIP) và kho vé ăn QR Code.*

| STT | Nghiệp vụ | Mã Screen | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :---: | :--- | :--- | :--- |
| 35 | Áp dụng mã giảm giá Combo | `SCR-109`, `SCR-406` | Customer / Admin | Medium | Detailed | Nhập mã giảm giá → Validate điều kiện → Trừ trực tiếp | Tổng tiền giảm theo voucher | Mã giảm giá phải còn lượt sử dụng |
| 38 | Thẻ Hội viên & Điểm Loyalty | `SCR-104` | Customer | Low | Sketch | Tích điểm qua booking → Nâng hạng Bronze/Gold/Platinum | Hạng thẻ cập nhật, nhận ưu đãi | Platinum giảm thêm 10% & free Buffet sáng |
| 39 | Kho Vé Ăn & Mã QR Suất Ăn | `SCR-107`, `SCR-207` | Customer / Staff | High | Formal | Mua vé ăn lẻ/theo đoàn → Sinh mã QR → Nhà hàng quét QR | Vé ăn chuyển trạng thái USED | Kiểm toán nhật ký quét QR real-time |

---

## 8. Feature: `report` (Thống kê, Real-time Chat & AI Assistant)
*Khai thác dữ liệu kinh doanh, xuất báo cáo Excel/PDF, Kênh Chat Lễ tân & Floating AI Assistant.*

| STT | Nghiệp vụ | Mã Screen | Vai trò | Risk | Spec Level | Luồng xử lý chính | Kết quả mong đợi | Business Rules / Ghi chú |
| :---: | :--- | :---: | :---: | :---: | :---: | :--- | :--- | :--- |
| 24 | Xem thống kê booking | `SCR-506` | Admin | Medium | Detailed | Lựa chọn mốc thời gian → Truy vấn tổng số booking | Biểu đồ thống kê hiển thị | Thống kê đơn lẻ vs đơn đoàn |
| 25 | Xem báo cáo doanh thu | `SCR-507`, `SCR-509` | Director | High | Detailed | Chọn chu kỳ → Phân tích doanh thu Phòng vs Vé ăn | Báo cáo chi tiết | Phân tích theo Ngày, Tuần, Tháng, Năm |
| 30 | Xuất báo cáo Excel / PDF | `SCR-510` | Admin / Director | Medium | Detailed | Click `Export Report` → Sinh file Excel | File Excel tải xuống thành công | Xuất dữ liệu báo cáo trong < 2 giây |
| 40 | AI Chatbot Tư vấn Combo | `SCR-503` | Guest / Customer | Low | Sketch | Click Floating Widget `🤖 AI Tư Vấn Đặt Đoàn` → Hỏi đáp | AI gợi ý combo & tiệc đoàn | Tăng trải nghiệm & tỷ lệ chuyển đổi |
