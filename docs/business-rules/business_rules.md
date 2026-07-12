# Quy tắc Nghiệp vụ Hệ thống (Business Rules Document)
**Dự án:** Hotel Booking System
**Trạng thái:** Hoàn chỉnh
**Quy trình quản lý:** Specification-Driven Development (SDD)

Tài liệu này tập hợp tất cả các quy tắc nghiệp vụ (Business Rules) bắt buộc của dự án Hotel Booking System. Đây là những ràng buộc logic được thực thi nghiêm ngặt tại tầng mã nguồn Java (Service Layer) và tầng Cơ sở dữ liệu (Database Layer) nhằm đảm bảo hệ thống vận hành đúng đắn, an toàn và nhất quán.

---

## BR-01: Quy tắc Đăng ký & Bảo mật Tài khoản (Identity & Security Rules)

1. **Tính Duy Nhất của Email:**
   - Mỗi địa chỉ email chỉ được phép đăng ký tối đa 1 tài khoản trên hệ thống.
   - Kiểm tra tính trùng lặp email được thực hiện ở tầng Service trước khi băm mật khẩu và được cưỡng chế bởi ràng buộc `UNIQUE` trên cột `email` của bảng `users`.
2. **Độ Phức Tạp của Mật Khẩu:**
   - Mật khẩu đăng ký hoặc mật khẩu thay đổi mới phải có độ dài tối thiểu là **8 ký tự**.
   - Mật khẩu phải chứa ít nhất một chữ viết hoa, một chữ viết thường, một chữ số, và không chứa khoảng trắng.
3. **Cơ chế Khóa Tài Khoản Tự Động:**
   - Khi đăng nhập thất bại (sai mật khẩu), hệ thống sẽ tăng giá trị trường `failed_login_attempts` trong bảng `users` lên 1.
   - Nếu số lần đăng nhập sai liên tiếp **đạt ngưỡng 5 lần**, trạng thái tài khoản (`status`) sẽ tự động chuyển sang `LOCKED`.
   - Tài khoản bị khóa sẽ không thể thực hiện bất kỳ giao dịch nào và chỉ có thể mở lại khi có sự can thiệp mở khóa thủ công từ Admin (`AdminUserController`).
   - Khi đăng nhập thành công, hệ thống tự động reset trường `failed_login_attempts` về lại `0`.
4. **Quy tắc Đăng xuất & Vô hiệu hóa Token:**
   - Khi người dùng gửi yêu cầu đăng xuất, Access Token đang sử dụng sẽ bị đưa vào danh sách đen (`revoked_tokens`) và lập tức mất hiệu lực sử dụng cho các yêu cầu API tiếp theo, ngay cả khi thời gian sống (TTL) của token đó vẫn chưa hết hạn.

---

## BR-02: Quy tắc Thời gian Lưu trú (Stay Period Rules)

1. **Kiểm tra ngày nhận phòng (Check-in Date):**
   - Ngày nhận phòng được chọn phải lớn hơn hoặc bằng ngày hiện tại (không cho phép chọn thời điểm trong quá khứ).
2. **Kiểm tra ngày trả phòng (Check-out Date):**
   - Ngày trả phòng phải sau ngày nhận phòng ít nhất là **1 ngày** (Khoảng thời gian lưu trú tối thiểu của 1 booking là 1 đêm).
   - Hệ thống tự động tính số lượng đêm lưu trú dựa trên chênh lệch thời gian giữa ngày Check-in và Check-out để tính tổng tiền phòng.

---

## BR-03: Quy tắc Tạm giữ Phòng (Room Locking Rules)

1. **Cơ chế tạm giữ phòng (Room Lock) khi đặt phòng:**
   - Khi khách hàng tiến hành đặt phòng (UC-11), hệ thống sẽ chuyển phòng đó vào trạng thái tạm khóa giữ chỗ bằng cách chèn bản ghi vào bảng `room_locks` với thời gian hết hạn (`expires_at`) mặc định là **10 phút** kể từ thời điểm đặt.
   - Trạng thái booking lúc này được thiết lập là `PENDING`.
2. **Chống đặt trùng phòng (Anti-Overbooking):**
   - Một phòng được coi là **không khả dụng** cho một khoảng thời gian cụ thể nếu tồn tại:
     - Bất kỳ đơn đặt phòng nào có trạng thái `CONFIRMED` bao phủ khoảng thời gian đó.
     - Hoặc có bất kỳ bản ghi giữ phòng (`room_locks`) nào của phòng đó chưa hết hạn (`expires_at > CURRENT_TIMESTAMP`).
3. **Tự động giải phóng phòng (Auto-Release Room Locks):**
   - Tác vụ ngầm `RoomLockCleanupScheduler` chạy định kỳ mỗi **60 giây** sẽ quét và dọn dẹp các khóa phòng đã hết hạn.
   - Nếu đơn đặt phòng tương ứng của khóa phòng đó vẫn ở trạng thái `PENDING`, hệ thống sẽ:
     1. Chuyển trạng thái đơn đặt phòng (`bookings.status`) thành `FAILED`.
     2. Xóa các bản ghi giữ phòng trong bảng `room_locks` để đưa phòng trở lại trạng thái trống khả dụng.
4. **Gia hạn thời gian khóa phòng (Lock Renewal):**
   - Khách hàng có thể gọi API gia hạn khóa phòng (`PUT /api/v1/bookings/{id}/lock/renew`) để kéo dài thêm tối đa 10 phút khóa phòng nếu tiến trình thanh toán trực tuyến của họ đang bị chậm trễ hoặc đang xử lý.

---

## BR-04: Quy tắc Áp dụng Mã Giảm Giá (Voucher Rules)

1. **Điều kiện Áp dụng:**
   - Mã giảm giá phải đang trong thời gian hiệu lực (`start_date <= CURRENT_TIMESTAMP <= end_date`).
   - Tổng giá trị gốc của đơn đặt phòng (`total_amount`) phải lớn hơn hoặc bằng giá trị đặt phòng tối thiểu (`min_booking_value`) yêu cầu của voucher.
   - Lượt sử dụng hiện tại phải nhỏ hơn giới hạn sử dụng tối đa (`current_usage < max_usage`). Nếu `max_usage = 0`, voucher không bị giới hạn lượt dùng.
2. **Quy tắc Tính toán Giảm giá:**
   - **Loại giảm giá theo phần trăm (PERCENTAGE):** Số tiền giảm giá được tính bằng `total_amount * (discount_value / 100)`.
   - **Loại giảm giá cố định (FIXED_AMOUNT):** Số tiền giảm giá đúng bằng giá trị `discount_value`.
   - **Ràng buộc giới hạn số tiền giảm:** Số tiền giảm giá (`discount_amount`) tuyệt đối không được vượt quá số tiền của đơn đặt phòng. Số tiền cuối cùng khách cần trả (`final_price`) bằng `total_amount - discount_amount` và không được phép âm (luôn $\ge 0$).
3. **Cập nhật số lượt dùng của Voucher:**
   - Lượt dùng của voucher (`current_usage`) chỉ được tăng lên thêm 1 khi đơn đặt phòng liên kết được chuyển sang trạng thái thành công (`CONFIRMED`). Nếu đặt phòng bị hủy ở trạng thái `PENDING` hoặc quá hạn thanh toán dẫn tới `FAILED`, lượt dùng của voucher không bị tính.

---

## BR-05: Quy tắc Hủy Đặt phòng & Hoàn tiền (Cancellation & Refund Rules)

1. **Thời hạn cho phép khách hàng tự hủy đặt phòng:**
   - Khách hàng chỉ có thể tự hủy đơn đặt phòng của mình thông qua giao diện ứng dụng trước giờ Check-in tối thiểu là **24 giờ**.
   - Nếu hủy trong vòng 24 giờ trước giờ Check-in, việc hủy trực tuyến sẽ bị khóa. Khách hàng phải liên hệ trực tiếp với quầy hỗ trợ của khách sạn để nhân viên có thẩm quyền xử lý thủ công.
2. **Cơ chế Hoàn tiền Tự động (Auto-Refund):**
   - Khi một đơn hàng đã thanh toán (`payment_status = 'PAID'`) được hủy thành công, hệ thống tự động kích hoạt tiến trình hoàn trả tiền cho khách hàng.
   - Số tiền hoàn lại (`refund_amount`) được xác định dựa trên chính sách hoàn tiền tương ứng với thời điểm hủy (ví dụ: hoàn tiền 100% nếu hủy trước 48 giờ, hoàn tiền 50% nếu hủy trước 24 giờ).
3. **Cơ chế Thử lại Tự động khi Hoàn tiền Thất bại (Refund Retry Mechanism):**
   - Nếu cuộc gọi API hoàn tiền sang cổng thanh toán Gateway gặp sự cố mạng hoặc bị lỗi từ chối tạm thời, hệ thống sẽ lưu trạng thái hoàn trả là `FAILED`.
   - Tác vụ ngầm `retryFailedRefunds` chạy định kỳ mỗi **60 giây** sẽ tự động truy vấn các giao dịch hoàn tiền bị lỗi và thực hiện gửi lại yêu cầu lên Gateway tối đa **3 lần** (`refund_retry_count < 3`).
   - Nếu sau 3 lần vẫn thất bại, hệ thống sẽ dừng thử lại và đánh dấu giao dịch cần kiểm tra thủ công (Admin/Director can can thiệp đối soát).

---

## BR-06: Quy tắc Xóa Mềm & Quản lý Inventory (Soft Delete Rules)

1. **Soft Delete trên Khách sạn (Hotel Soft Delete):**
   - Khi Admin thực hiện xóa khách sạn (`DELETE /api/v1/hotels/{id}`), hệ thống tuyệt đối không thực hiện câu lệnh SQL `DELETE` vật lý để xóa bản ghi.
   - Thay vào đó, hệ thống thực hiện cập nhật `is_active = 0` trên bảng `hotels` và tự động chuyển trạng thái của toàn bộ các phòng (`rooms`) thuộc khách sạn đó thành `UNAVAILABLE`.
2. **Ràng buộc đơn đặt phòng đang hoạt động (Active Booking Check):**
   - Hệ thống nghiêm cấm xóa mềm khách sạn hoặc thay đổi trạng thái phòng thành `UNAVAILABLE` nếu tại khách sạn hoặc phòng đó đang tồn tại bất kỳ đơn đặt phòng nào có trạng thái `PENDING` hoặc `CONFIRMED` trong tương lai. Admin bắt buộc phải xử lý dời lịch hoặc hủy/hoàn tiền các đơn hàng này trước khi vô hiệu hóa tài nguyên.

---

## BR-07: Quy tắc Gửi & Kiểm duyệt Đánh giá (Review & Moderation Rules)

1. **Tính Hợp Lệ khi Gửi Đánh Giá:**
   - Khách hàng chỉ có thể gửi đánh giá khách sạn khi đơn đặt phòng (`booking_id`) của họ có trạng thái `COMPLETED` (đã hoàn tất check-out lưu trú).
   - Chỉ người dùng sở hữu đơn đặt phòng (`booking.user_id = current_user_id`) mới được phép gửi đánh giá cho đơn đặt phòng đó.
   - Mỗi đơn đặt phòng (`booking_id`) chỉ được phép tạo tối đa **1 đánh giá** duy nhất (quy định ràng buộc UNIQUE trên database).
2. **Kiểm duyệt Đánh giá của Admin (Moderation):**
   - Khi Admin thực hiện ẩn một đánh giá vi phạm quy định (`status` đổi thành `HIDDEN`), đánh giá đó sẽ lập tức bị loại bỏ khỏi các truy vấn hiển thị công khai.
   - Điểm số đánh giá trung bình (`rating`) của khách sạn liên quan sẽ tự động được hệ thống tính toán lại dựa trên tất cả các đánh giá hợp lệ (`status = 'VISIBLE'`), bỏ qua hoàn toàn các đánh giá đã bị ẩn.

---

## BR-08: Quy tắc Cập nhật Trạng thái Buồng phòng (Staff Room Status Rules)

1. **Phân Quyền Vận Hành (Staff Role Permissions):**
   - Chỉ các vai trò nhân viên buồng phòng (`HOUSEKEEPER`), lễ tân (`RECEPTIONIST`) và quản trị viên (`ADMIN`) mới được phép thay đổi trạng thái dọn dẹp/bảo trì của phòng (`status`).
2. **Quy Tắc Chuyển Trạng Thái Phòng:**
   - **Chuyển sang `UNAVAILABLE` (Bẩn/Cần dọn dẹp/Bảo trì):** Thực hiện khi khách check-out, hoặc phòng phát sinh sự cố cần khóa tạm thời. Khi phòng ở trạng thái `UNAVAILABLE`, hệ thống tìm kiếm phòng trống (`UC-09`) sẽ tự động loại bỏ phòng này khỏi danh sách khả dụng cho các lượt đặt phòng mới.
   - **Chuyển sang `AVAILABLE` (Đã dọn dẹp sạch sẽ/Sẵn sàng):** Thực hiện sau khi nhân viên buồng phòng hoàn thành vệ sinh phòng và xác nhận phòng sẵn sàng đón lượt khách tiếp theo.
3. **Ràng Buộc Đặt Phòng Trước Khi Khóa Phòng:**
   - Nghiêm cấm chuyển trạng thái phòng sang `UNAVAILABLE` hoặc `MAINTENANCE` nếu phòng đó đang nằm trong lịch check-in của bất kỳ đơn đặt phòng nào có trạng thái `CONFIRMED` hoặc `PENDING` đang hoạt động trong tương lai, trừ các trường hợp khẩn cấp có sự duyệt duyệt thủ công của quản trị viên cấp cao.
