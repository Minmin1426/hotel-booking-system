package com.hotelbooking.common.utils;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.payment.Payment;
import com.hotelbooking.room.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void sendResetPasswordEmail(String email, String token) {
        log.info("Sending password reset email request received for: {}", email);
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;

        String htmlContent = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Khôi phục mật khẩu - Luxury Stay</title>\n" +
            "</head>\n" +
            "<body style=\"font-family: Arial, sans-serif; background-color: #f4f5f7; margin: 0; padding: 20px;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 40px; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); border: 1px solid #e3e3e8;\">\n" +
            "        <div style=\"text-align: center; margin-bottom: 30px;\">\n" +
            "            <h2 style=\"color: #0066cc; margin: 0; font-family: 'Georgia', serif;\">LUXURY STAY</h2>\n" +
            "            <p style=\"font-size: 10px; text-transform: uppercase; letter-spacing: 2px; color: #a1a1a6; margin: 5px 0 0 0;\">Exquisite Travel Experiences</p>\n" +
            "        </div>\n" +
            "        <div style=\"border-top: 1px solid #e3e3e8; padding-top: 30px; color: #1d1d1f;\">\n" +
            "            <p>Xin chào,</p>\n" +
            "            <p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn tại <strong>Luxury Stay</strong>.</p>\n" +
            "            <p>Vui lòng click vào nút bên dưới để tiến hành đặt lại mật khẩu mới (Đường liên kết này chỉ có hiệu lực trong vòng <strong>5 phút</strong>):</p>\n" +
            "            <div style=\"text-align: center; margin: 35px 0;\">\n" +
            "                <a href=\"%s\" style=\"background-color: #0066cc; color: #ffffff; text-decoration: none; padding: 14px 35px; border-radius: 12px; font-weight: bold; font-size: 14px; display: inline-block;\">Đặt lại mật khẩu</a>\n" +
            "            </div>\n" +
            "            <p style=\"font-size: 12px; color: #86868b; line-height: 1.6;\">Link: <a href=\"%s\" style=\"color: #0066cc;\">%s</a></p>\n" +
            "            <p style=\"margin-top: 30px;\">Trân trọng,<br><strong>Đội ngũ Luxury Stay Support</strong></p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            resetUrl, resetUrl, resetUrl
        );

        sendEmailAsync(email, "Yêu cầu đặt lại mật khẩu - Luxury Stay", htmlContent, "reset-password-" + email);
    }

    @Override
    public void sendBookingConfirmationEmail(String email, String bookingCode) {
        log.info("Sending booking confirmation email to: {}", email);
        String htmlContent = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body style=\"font-family: Arial, sans-serif; background-color: #f4f5f7; padding: 20px;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 40px; border-radius: 16px;\">\n" +
            "        <h2>LUXURY STAY - Xác nhận đặt phòng</h2>\n" +
            "        <p>Mã đặt phòng của bạn là: <strong style=\"color: #0066cc;\">%s</strong></p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            bookingCode
        );
        sendEmailAsync(email, "Xác nhận đặt phòng thành công - Luxury Stay", htmlContent, "booking-" + bookingCode);
    }

    @Override
    public void sendBookingTicketEmail(Booking booking, Payment payment) {
        if (booking == null) return;
        String recipientEmail = (booking.getUser() != null) ? booking.getUser().getEmail() : "customer@example.com";
        String customerName = (booking.getUser() != null) ? booking.getUser().getFullName() : "Quý khách";
        
        Hotel hotel = booking.getHotel();
        Room room = (booking.getBookingRooms() != null && !booking.getBookingRooms().isEmpty())
                ? booking.getBookingRooms().get(0).getRoom() : null;

        String hotelName = (hotel != null) ? hotel.getName() : "Luxury Hotel";
        String hotelAddress = (hotel != null) ? hotel.getLocation() : "Việt Nam";
        String roomType = (room != null) ? room.getRoomType() : "Standard Room";
        String roomNumber = (room != null && room.getRoomNumber() != null) ? room.getRoomNumber() : "TBD";
        
        String checkInStr = (booking.getCheckInDate() != null) ? booking.getCheckInDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " 12:00" : "N/A";
        String checkOutStr = (booking.getCheckOutDate() != null) ? booking.getCheckOutDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " 12:00" : "N/A";

        BigDecimal totalPrice = (booking.getFinalPrice() != null) ? booking.getFinalPrice() : BigDecimal.ZERO;
        BigDecimal paidAmount = (payment != null && payment.getAmount() != null) ? payment.getAmount() : totalPrice;
        BigDecimal remainingAmount = totalPrice.subtract(paidAmount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) remainingAmount = BigDecimal.ZERO;

        String paymentMethod = (payment != null && payment.getPaymentMethod() != null) ? payment.getPaymentMethod() : "ONLINE";
        String depositPercentStr = "30";
        if (payment != null && payment.getDepositRatio() != null) {
            depositPercentStr = String.valueOf(payment.getDepositRatio().multiply(new BigDecimal("100")).setScale(0, java.math.RoundingMode.HALF_UP));
        }
        String paymentStatusText = (payment != null && Boolean.TRUE.equals(payment.getIsDeposit())) 
                ? String.format("Đã cọc %s%% ($%s)", depositPercentStr, paidAmount.toString())
                : "Đã thanh toán 100% online";

        String checkinCode = (booking.getCheckinQrCode() != null) ? booking.getCheckinQrCode() : "CHK-" + booking.getBookingCode();

        String htmlContent = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Vé Điện Tử Check-in - Luxury Stay</title>\n" +
            "</head>\n" +
            "<body style=\"font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 30px; color: #334155;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 24px; padding: 35px; box-shadow: 0 10px 25px rgba(0,0,0,0.05);\">\n" +
            "        <div style=\"text-align: center; border-bottom: 2px solid #f1f5f9; padding-bottom: 20px;\">\n" +
            "            <h1 style=\"color: #b45309; margin: 0; font-family: 'Georgia', serif; font-size: 26px; letter-spacing: 2px; font-weight: bold;\">LUXURY STAY</h1>\n" +
            "            <p style=\"color: #64748b; font-size: 11px; text-transform: uppercase; letter-spacing: 3px; margin: 6px 0 0 0; font-weight: bold;\">Vé Điện Tử Check-in (E-Ticket Pass)</p>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"text-align: center; margin: 25px 0; background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 16px; padding: 25px;\">\n" +
            "            <p style=\"color: #64748b; font-size: 11px; text-transform: uppercase; letter-spacing: 2px; margin: 0 0 8px 0; font-weight: bold;\">Mã Nhận Phòng (Check-in Code)</p>\n" +
            "            <span style=\"font-family: monospace; font-size: 24px; font-weight: 900; color: #0f172a; background-color: #ffffff; border: 1px solid #cbd5e1; border-radius: 12px; padding: 10px 24px; display: inline-block; letter-spacing: 2px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);\">\n" +
            "                %s\n" +
            "            </span>\n" +
            "            <p style=\"color: #475569; font-size: 13px; margin: 15px 0 0 0; line-height: 1.5;\">Vui lòng cung cấp mã này cho nhân viên Lễ tân khi nhận phòng</p>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; margin-bottom: 20px;\">\n" +
            "            <h3 style=\"color: #0f172a; margin-top: 0; border-bottom: 2px solid #f1f5f9; padding-bottom: 8px; font-size: 15px; font-weight: bold;\">🏨 Thông Tin Đặt Phòng</h3>\n" +
            "            <table style=\"width: 100%%; font-size: 13px; color: #475569; border-collapse: collapse;\">\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Khách hàng:</td><td style=\"font-weight: bold; text-align: right; color: #0f172a;\">%s</td></tr>\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Mã đơn đặt:</td><td style=\"font-weight: bold; text-align: right; color: #b45309;\">%s</td></tr>\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Khách sạn:</td><td style=\"font-weight: bold; text-align: right; color: #0f172a;\">%s</td></tr>\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Địa chỉ:</td><td style=\"text-align: right; color: #475569;\">%s</td></tr>\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Loại phòng:</td><td style=\"font-weight: bold; text-align: right; color: #0f172a;\">%s (Số phòng: %s)</td></tr>\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Nhận phòng:</td><td style=\"text-align: right; color: #0f172a; font-weight: bold;\">%s</td></tr>\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Trả phòng:</td><td style=\"text-align: right; color: #0f172a; font-weight: bold;\">%s</td></tr>\n" +
            "            </table>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px;\">\n" +
            "            <h3 style=\"color: #0f172a; margin-top: 0; border-bottom: 2px solid #e2e8f0; padding-bottom: 8px; font-size: 15px; font-weight: bold;\">💳 Chi Tiết Thanh Toán</h3>\n" +
            "            <table style=\"width: 100%%; font-size: 13px; color: #475569;\">\n" +
            "                <tr><td style=\"padding: 6px 0; color: #64748b;\">Tổng giá trị đơn:</td><td style=\"font-weight: bold; text-align: right; color: #0f172a;\">$%s</td></tr>\n" +
            "                <tr><td style=\"padding: 6px 0; color: #64748b;\">Trạng thái:</td><td style=\"font-weight: bold; text-align: right; color: #16a34a;\">%s</td></tr>\n" +
            "                <tr><td style=\"padding: 6px 0; color: #64748b;\">Phương thức:</td><td style=\"text-align: right; color: #0f172a;\">%s</td></tr>\n" +
            "                <tr><td style=\"padding: 6px 0; color: #64748b;\">Đã thanh toán:</td><td style=\"font-weight: bold; text-align: right; color: #2563eb;\">$%s</td></tr>\n" +
            "                <tr style=\"border-top: 1px dashed #cbd5e1;\">\n" +
            "                    <td style=\"padding: 10px 0 0 0; color: #b45309; font-weight: bold;\">Thanh toán thêm tại quầy:</td>\n" +
            "                    <td style=\"padding: 10px 0 0 0; font-weight: bold; text-align: right; color: #b45309; font-size: 15px;\">$%s</td>\n" +
            "                </tr>\n" +
            "            </table>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"margin-top: 30px; text-align: center; font-size: 11px; color: #94a3b8;\">\n" +
            "            <p style=\"color: #64748b; font-weight: bold; margin-bottom: 5px;\">Trân trọng cảm ơn quý khách đã tin tưởng dịch vụ của Luxury Stay!</p>\n" +
            "            <p>&copy; 2026 Luxury Stay. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            checkinCode,
            customerName, booking.getBookingCode(), hotelName, hotelAddress, roomType, roomNumber,
            checkInStr, checkOutStr,
            totalPrice.toPlainString(), paymentStatusText, paymentMethod, paidAmount.toPlainString(), remainingAmount.toPlainString()
        );

        sendEmailAsync(recipientEmail, "🎟️ Vé Điện Tử Check-in Đặt Phòng - " + booking.getBookingCode(), htmlContent, "ticket-" + booking.getBookingCode());
    }

    @Override
    public void sendBookingCancellationEmail(Booking booking, Payment payment) {
        if (booking == null) return;
        String recipientEmail = (booking.getUser() != null) ? booking.getUser().getEmail() : "customer@example.com";
        String customerName = (booking.getUser() != null) ? booking.getUser().getFullName() : "Quý khách";
        
        BigDecimal refundAmount = (payment != null && payment.getAmount() != null) ? payment.getAmount() : BigDecimal.ZERO;
        String refundNotice = "";
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            refundNotice = String.format(
                "        <div style=\"background-color: #fef3c7; border: 1px solid #fde68a; border-radius: 12px; padding: 15px; margin: 20px 0; color: #b45309; font-size: 13px; font-weight: bold;\">\n" +
                "            💰 Khoản tiền đặt cọc $%s đang ở trạng thái \"CHỜ HOÀN TIỀN\" và sẽ được chuyển lại tài khoản của quý khách sau khi ban quản trị phê duyệt.\n" +
                "        </div>\n",
                refundAmount.setScale(2).toString()
            );
        }

        String htmlContent = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Xác nhận hủy đặt phòng - Luxury Stay</title>\n" +
            "</head>\n" +
            "<body style=\"font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 30px; color: #334155;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 24px; padding: 35px; box-shadow: 0 10px 25px rgba(0,0,0,0.05);\">\n" +
            "        <div style=\"text-align: center; border-bottom: 2px solid #f1f5f9; padding-bottom: 20px;\">\n" +
            "            <h1 style=\"color: #b45309; margin: 0; font-family: 'Georgia', serif; font-size: 26px; letter-spacing: 2px; font-weight: bold;\">LUXURY STAY</h1>\n" +
            "            <p style=\"color: #64748b; font-size: 11px; text-transform: uppercase; letter-spacing: 3px; margin: 6px 0 0 0; font-weight: bold;\">Xác nhận hủy đặt phòng thành công</p>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"margin: 25px 0; font-size: 14px; line-height: 1.6; color: #475569;\">\n" +
            "            <p>Xin chào <strong>%s</strong>,</p>\n" +
            "            <p>Luxury Stay xin xác nhận yêu cầu hủy đặt phòng của quý khách đã được thực hiện thành công.</p>\n" +
            "            <p>Mã đặt phòng bị hủy: <strong style=\"color: #b45309; font-family: monospace; font-size: 15px;\">%s</strong></p>\n" +
            "        </div>\n" +
            "\n" +
            "%s" +
            "\n" +
            "        <div style=\"background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; font-size: 13px; color: #64748b;\">\n" +
            "            <p style=\"margin-top: 0; font-weight: bold; color: #0f172a;\">ℹ️ Chính sách hoàn tiền của Luxury Stay:</p>\n" +
            "            <ul style=\"padding-left: 20px; margin-bottom: 0;\">\n" +
            "                <li>Hủy trước 3 ngày nhận phòng: Hoàn lại 100% số tiền cọc.</li>\n" +
            "                <li>Hủy từ 1 đến 3 ngày trước nhận phòng: Hoàn lại 50% số tiền cọc.</li>\n" +
            "                <li>Hủy trong vòng 24 giờ trước nhận phòng: Không áp dụng hoàn tiền cọc.</li>\n" +
            "            </ul>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"margin-top: 30px; text-align: center; font-size: 11px; color: #94a3b8;\">\n" +
            "            <p style=\"color: #64748b; font-weight: bold; margin-bottom: 5px;\">Hy vọng sẽ được phục vụ quý khách trong những chuyến đi tới!</p>\n" +
            "            <p>&copy; 2026 Luxury Stay. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            customerName, booking.getBookingCode(), refundNotice
        );

        sendEmailAsync(recipientEmail, "❌ Xác Nhận Hủy Đặt Phòng Thành Công - " + booking.getBookingCode(), htmlContent, "cancel-" + booking.getBookingCode());
    }

    @Override
    public void sendRefundConfirmationEmail(String email, String bookingCode, BigDecimal refundAmount) {
        log.info("Sending refund confirmation email to: {}", email);
        
        String htmlContent = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Xác nhận hoàn tiền - Luxury Stay</title>\n" +
            "</head>\n" +
            "<body style=\"font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 30px; color: #334155;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 24px; padding: 35px; box-shadow: 0 10px 25px rgba(0,0,0,0.05);\">\n" +
            "        <div style=\"text-align: center; border-bottom: 2px solid #f1f5f9; padding-bottom: 20px;\">\n" +
            "            <h1 style=\"color: #b45309; margin: 0; font-family: 'Georgia', serif; font-size: 26px; letter-spacing: 2px; font-weight: bold;\">LUXURY STAY</h1>\n" +
            "            <p style=\"color: #64748b; font-size: 11px; text-transform: uppercase; letter-spacing: 3px; margin: 6px 0 0 0; font-weight: bold;\">Xác nhận hoàn tiền giao dịch thành công</p>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"text-align: center; margin: 25px 0; background-color: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 16px; padding: 25px;\">\n" +
            "            <p style=\"color: #15803d; font-size: 11px; text-transform: uppercase; letter-spacing: 2px; margin: 0 0 8px 0; font-weight: bold;\">Số Tiền Đã Hoàn Trả (Refunded Amount)</p>\n" +
            "            <span style=\"font-size: 26px; font-weight: 900; color: #16a34a;\">\n" +
            "                $%s\n" +
            "            </span>\n" +
            "            <p style=\"color: #166534; font-size: 13px; margin: 12px 0 0 0; font-weight: 500;\">Giao dịch hoàn tiền đã được xử lý thành công!</p>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; margin-bottom: 20px;\">\n" +
            "            <h3 style=\"color: #0f172a; margin-top: 0; border-bottom: 2px solid #f1f5f9; padding-bottom: 8px; font-size: 15px; font-weight: bold;\">📋 Thông Tin Giao Dịch</h3>\n" +
            "            <table style=\"width: 100%%; font-size: 13px; color: #475569; border-collapse: collapse;\">\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Mã đặt phòng:</td><td style=\"font-weight: bold; text-align: right; color: #0f172a; font-family: monospace;\">%s</td></tr>\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Trạng thái:</td><td style=\"font-weight: bold; text-align: right; color: #16a34a;\">ĐÃ HOÀN TIỀN (REFUNDED)</td></tr>\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Phương thức hoàn:</td><td style=\"text-align: right; color: #0f172a; font-weight: bold;\">Tự động qua cổng thanh toán</td></tr>\n" +
            "                <tr><td style=\"padding: 8px 0; color: #64748b;\">Thời gian thực hiện:</td><td style=\"text-align: right; color: #0f172a;\">Hôm nay</td></tr>\n" +
            "            </table>\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 16px; padding: 20px; font-size: 13px; color: #64748b; line-height: 1.5;\">\n" +
            "            💡 <strong>Lưu ý:</strong> Tiền hoàn trả sẽ được chuyển vào tài khoản hoặc thẻ ngân hàng quý khách đã sử dụng để giao dịch trong vòng 3-5 ngày làm việc tùy thuộc vào quy định ngân hàng.\n" +
            "        </div>\n" +
            "\n" +
            "        <div style=\"margin-top: 30px; text-align: center; font-size: 11px; color: #94a3b8;\">\n" +
            "            <p style=\"color: #64748b; font-weight: bold; margin-bottom: 5px;\">Hy vọng sẽ tiếp tục được đồng hành cùng quý khách trong chuyến đi sau!</p>\n" +
            "            <p>&copy; 2026 Luxury Stay. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            refundAmount.setScale(2).toString(), bookingCode
        );
        sendEmailAsync(email, "✅ Xác nhận hoàn tiền thành công - Luxury Stay", htmlContent, "refund-" + bookingCode);
    }

    private void sendEmailAsync(String to, String subject, String htmlContent, String fileKey) {
        // Always save a local preview HTML file for offline/dev inspection
        saveLocalEmailPreview(to, subject, htmlContent, fileKey);

        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                log.info("✅ Email sent successfully via SMTP to: {}", to);
            } catch (Exception e) {
                log.warn("⚠️ SMTP Mail Send Exception to {}: {}. (Saved local HTML preview in uploads/emails/)", to, e.getMessage());
            }
        });
    }

    private void saveLocalEmailPreview(String to, String subject, String htmlContent, String fileKey) {
        try {
            File dir = new File("uploads/emails");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String safeFileName = (fileKey != null ? fileKey : "mail") + ".html";
            File mailFile = new File(dir, safeFileName);
            try (FileWriter writer = new FileWriter(mailFile)) {
                writer.write(htmlContent);
            }
            log.info("📧 LOCAL DEV MAIL DUMPER: Email to [{}] saved to local file -> file:///{}/{}", to, dir.getAbsolutePath().replace("\\", "/"), safeFileName);
        } catch (Exception ex) {
            log.error("Failed to save local email preview file: {}", ex.getMessage());
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        sendEmailAsync(to, subject, body, "email-" + System.currentTimeMillis());
    }

    @Override
    public void sendOtpEmail(String email, String fullName, String otpCode) {
        log.info("Sending OTP email to: {}", email);
        String htmlContent = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Mã xác minh OTP - Luxury Stay</title>\n" +
            "</head>\n" +
            "<body style=\"font-family: Arial, sans-serif; background-color: #f4f5f7; margin: 0; padding: 20px;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 40px; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); border: 1px solid #e3e3e8;\">\n" +
            "        <div style=\"text-align: center; margin-bottom: 30px;\">\n" +
            "            <h2 style=\"color: #0066cc; margin: 0; font-family: 'Georgia', serif;\">LUXURY STAY</h2>\n" +
            "            <p style=\"font-size: 10px; text-transform: uppercase; letter-spacing: 2px; color: #a1a1a6; margin: 5px 0 0 0;\">Exquisite Travel Experiences</p>\n" +
            "        </div>\n" +
            "        <div style=\"border-top: 1px solid #e3e3e8; padding-top: 30px; color: #1d1d1f;\">\n" +
            "            <p>Xin chào <strong>%s</strong>,</p>\n" +
            "            <p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn tại <strong>Luxury Stay</strong>.</p>\n" +
            "            <p>Đây là mã xác minh (OTP) của bạn:</p>\n" +
            "            <div style=\"text-align: center; margin: 35px 0;\">\n" +
            "                <div style=\"display: inline-block; background-color: #f5f5f7; border: 2px dashed #0066cc; padding: 20px 40px; border-radius: 12px;\">\n" +
            "                    <p style=\"margin: 0; font-size: 32px; font-weight: bold; color: #0066cc; letter-spacing: 8px; font-family: 'Courier New', monospace;\">%s</p>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "            <p style=\"font-size: 12px; color: #86868b;\">Mã này có hiệu lực trong <strong>15 phút</strong>. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>\n" +
            "            <p style=\"margin-top: 30px;\">Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua email này.</p>\n" +
            "            <p>Trân trọng,<br><strong>Đội ngũ Luxury Stay Support</strong></p>\n" +
            "        </div>\n" +
            "        <div style=\"margin-top: 40px; border-top: 1px solid #e3e3e8; padding-top: 20px; text-align: center; font-size: 11px; color: #86868b;\">\n" +
            "            <p>Đây là email tự động từ hệ thống. Vui lòng không trả lời email này.</p>\n" +
            "            <p>&copy; 2026 Luxury Stay. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            fullName != null ? fullName : "Quý khách",
            otpCode
        );
        sendEmailAsync(email, "Mã xác minh khôi phục mật khẩu - Luxury Stay", htmlContent, "otp-reset-" + email);
    }
}
