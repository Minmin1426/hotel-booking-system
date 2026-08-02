# Kịch Bản Kiểm Thử: Báo Cáo Analytics, Xuất Excel/PDF, Live Chat & AI Chatbot
# Phân hệ: com.hotelbooking.report, com.hotelbooking.chat (Reports, Real-time Chat & AI Assistant)

Tài liệu này chi tiết hóa các kịch bản kiểm thử (Test Cases) và dữ liệu kiểm thử (Test Data) cho nghiệp vụ Báo cáo doanh thu Giám đốc, Xuất file Excel/PDF trong <2s, Kênh Chat Real-time Khách - Lễ tân, và Floating AI Chatbot Assistant.

---

## 1. Unit & Integration Test Cases (Kiểm thử Đơn vị & Tích hợp)

### [INT-REPORT-01]: Xuất Báo cáo Doanh thu & Tỷ lệ lấp đầy ra file Excel (.xlsx)
*   **Endpoint:** `GET /api/v1/reports/export-excel?fromDate=2026-06-01&toDate=2026-06-30`
*   **Mục đích:** Đảm bảo hệ thống xuất file báo cáo Excel hợp lệ chứa dữ liệu doanh thu phòng, suất ăn và tỷ lệ hoàn tiền.
*   **Thời gian phản hồi (Performance SLA):** `< 2.0 giây`.
*   **Phản hồi API kỳ vọng:**
    *   **HTTP Status:** `200 OK`
    *   **Content-Type:** `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
    *   Tải xuống file `Hotel_Booking_Revenue_Report_2026-06.xlsx`.

### [INT-AI-01]: AI Chatbot Assistant tư vấn gói Đặt phòng Đoàn & Tiệc Combo
*   **Endpoint:** `POST /api/v1/ai/chat`
*   **Payload:** `{ "message": "Công ty tôi có 25 người muốn đặt phòng ở Đà Nẵng và đặt tiệc tối hải sản thì có ưu đãi gì không?" }`
*   **Xử lý Backend:**
    1. AI Assistant truy vấn quy tắc giảm 25% cho đơn đoàn >= 5 phòng.
    2. AI Gợi ý gói Buffet Tối Hải Sản & tùy chọn Cọc 30% Deposit.
*   **Phản hồi API kỳ vọng:**
    *   **HTTP Status:** `200 OK`
    *   Trả về câu trả lời tư vấn chính xác kèm liên kết chuyển nhanh đến wizard đặt đoàn `SCR-304`.
