# Hướng dẫn và Chuẩn hóa Prompts (AI Prompts Guide)
**Dự án:** Hotel Booking System
**Thư mục:** `docs/prompts/`
**Mô hình phát triển:** Specification-Driven Development (SDD) kết hợp AI Coding Assistant

Thư mục này được sử dụng để lưu trữ lịch sử các câu lệnh (prompts) mẫu và tiêu chuẩn tương tác với các công cụ AI Assistant (như Gemini, Cursor, ChatGPT) để sinh mã nguồn, sinh bộ kiểm thử và tài liệu hóa dự án một cách nhất quán.

---

## 1. Mục đích của Thư mục Prompts

Trong mô hình phát triển phần mềm hiện đại có sự hỗ trợ của AI (AI-Assisted Development), việc lưu trữ prompts mang lại các lợi ích sau:
*   **Tính tái tạo (Reproducibility):** Giúp các thành viên khác trong dự án có thể sử dụng lại các prompt mẫu để sinh các thành phần tương tự (ví dụ: tạo một Entity mới kèm đầy đủ DTO, Controller, Service, Repository và Unit Test).
*   **Kiểm soát chất lượng (Quality Control):** Đảm bảo mã nguồn do AI sinh ra tuân thủ đúng các ràng buộc kiến trúc, quy tắc bảo mật và phong cách viết code của dự án.
*   **Lịch sử phát triển:** Lưu vết cách thức hệ thống được hình thành và các yêu cầu đặc thù đã cung cấp cho AI trong từng giai đoạn.

---

## 2. Tiêu chuẩn viết Prompt của Dự án (Prompt Standard)

Theo đặc tả của dự án tại [PROJECTAGENT.md](file:///C:/Users/Minmin/Documents/GitHub/hotel-booking-system/PROJECTAGENT.md#L833-L843), mọi câu lệnh gửi cho AI phát triển chức năng đều phải tuân thủ cấu trúc 5 phần rõ ràng:

```text
1. Context (Bối cảnh hệ thống, cấu trúc hiện tại)
2. Task (Nhiệm vụ cụ thể cần thực hiện)
3. Constraints (Các ràng buộc bảo mật, kiến trúc và hiệu năng)
4. Acceptance Criteria (Tiêu chí nghiệm thu chức năng)
5. Output Format (Định dạng kết quả đầu ra mong muốn)
```

### Ví dụ Prompt Mẫu sinh Unit Test cho Service:
```text
[CONTEXT]
Tôi đang phát triển dự án Hotel Booking System sử dụng Java 17, Spring Boot 3.x, JUnit 5 và Mockito. Lớp Service hiện tại là BookingServiceImpl xử lý nghiệp vụ đặt phòng.

[TASK]
Hãy viết Unit Test cho phương thức `createBooking` của BookingServiceImpl.

[CONSTRAINTS]
- Sử dụng JUnit 5 và Mockito (@Mock, @InjectMocks).
- Không khởi động thực tế Spring Context (chỉ unit test thuần).
- Độ bao phủ mã nguồn (Coverage) tối thiểu 90% các nhánh logic của hàm.
- Mock đầy đủ các repository phụ thuộc: BookingRepository, UserRepository, RoomRepository.

[ACCEPTANCE CRITERIA]
- Kịch bản 1: Đặt phòng thành công khi phòng trống và ngày hợp lệ.
- Kịch bản 2: Ném lỗi BusinessException khi ngày check-out trước ngày check-in.
- Kịch bản 3: Ném lỗi BusinessException khi phòng đã bị khóa bởi người khác.

[OUTPUT FORMAT]
Trả về mã nguồn Java hoàn chỉnh của lớp BookingServiceImplTest.
```
