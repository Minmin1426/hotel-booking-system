package com.hotelbooking.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendResetPasswordEmail(String email, String token) {
        log.info("Sending password reset email request received for: {}", email);
        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        log.info("Password Reset Token: {}", token);
        log.info("Reset Password URL: {}", resetUrl);

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
            "                <a href=\"%s\" style=\"background-color: #0066cc; color: #ffffff; text-decoration: none; padding: 14px 35px; border-radius: 12px; font-weight: bold; font-size: 14px; display: inline-block; box-shadow: 0 4px 8px rgba(0,102,204,0.25);\">Đặt lại mật khẩu</a>\n" +
            "            </div>\n" +
            "            <p style=\"font-size: 12px; color: #86868b; line-height: 1.6;\">Nếu nút bấm trên không hoạt động, bạn có thể copy và dán đường link bên dưới vào thanh địa chỉ của trình duyệt:<br>\n" +
            "            <a href=\"%s\" style=\"color: #0066cc; word-break: break-all;\">%s</a></p>\n" +
            "            <p style=\"margin-top: 30px;\">Trân trọng,<br><strong>Đội ngũ Luxury Stay Support</strong></p>\n" +
            "        </div>\n" +
            "        <div style=\"margin-top: 40px; border-top: 1px solid #e3e3e8; padding-top: 20px; text-align: center; font-size: 11px; color: #86868b;\">\n" +
            "            <p>Đây là email tự động từ hệ thống. Vui lòng không trả lời email này.</p>\n" +
            "            <p>&copy; 2026 Luxury Stay. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            resetUrl, resetUrl, resetUrl
        );

        sendEmailAsync(email, "Yêu cầu đặt lại mật khẩu - Luxury Stay", htmlContent);
    }

    @Override
    public void sendBookingConfirmationEmail(String email, String bookingCode) {
        log.info("Sending booking confirmation email to: {}", email);

        String htmlContent = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Xác nhận đặt phòng - Luxury Stay</title>\n" +
            "</head>\n" +
            "<body style=\"font-family: Arial, sans-serif; background-color: #f4f5f7; margin: 0; padding: 20px;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 40px; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); border: 1px solid #e3e3e8;\">\n" +
            "        <div style=\"text-align: center; margin-bottom: 30px;\">\n" +
            "            <h2 style=\"color: #0066cc; margin: 0; font-family: 'Georgia', serif;\">LUXURY STAY</h2>\n" +
            "            <p style=\"font-size: 10px; text-transform: uppercase; letter-spacing: 2px; color: #a1a1a6; margin: 5px 0 0 0;\">Exquisite Travel Experiences</p>\n" +
            "        </div>\n" +
            "        <div style=\"border-top: 1px solid #e3e3e8; padding-top: 30px; color: #1d1d1f;\">\n" +
            "            <h3 style=\"color: #24b47e; margin-top: 0;\">🎉 Đặt phòng thành công!</h3>\n" +
            "            <p>Xin chào,</p>\n" +
            "            <p>Cảm ơn bạn đã lựa chọn dịch vụ đặt phòng của <strong>Luxury Stay</strong>. Đơn đặt phòng của bạn đã được xác nhận thành công trên hệ thống.</p>\n" +
            "            <div style=\"background-color: #f5f5f7; padding: 20px; border-radius: 12px; margin: 25px 0; border: 1px solid #e3e3e8;\">\n" +
            "                <p style=\"margin: 0 0 8px 0; font-size: 13px; color: #86868b;\">MÃ ĐẶT PHÒNG (BOOKING CODE)</p>\n" +
            "                <p style=\"margin: 0; font-size: 24px; font-weight: bold; color: #0066cc; letter-spacing: 1px;\">%s</p>\n" +
            "            </div>\n" +
            "            <p>Thông tin chi tiết về phòng đặt và hướng dẫn nhận phòng (Check-in) đã được cập nhật đầy đủ trong tài khoản cá nhân của bạn trên website.</p>\n" +
            "            <p style=\"margin-top: 30px;\">Chúc bạn có một kỳ nghỉ tuyệt vời!<br><strong>Đội ngũ Luxury Stay</strong></p>\n" +
            "        </div>\n" +
            "        <div style=\"margin-top: 40px; border-top: 1px solid #e3e3e8; padding-top: 20px; text-align: center; font-size: 11px; color: #86868b;\">\n" +
            "            <p>Đây là email tự động từ hệ thống. Vui lòng không trả lời email này.</p>\n" +
            "            <p>&copy; 2026 Luxury Stay. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            bookingCode
        );

        sendEmailAsync(email, "Xác nhận đặt phòng thành công - Luxury Stay", htmlContent);
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
            "<body style=\"font-family: Arial, sans-serif; background-color: #f4f5f7; margin: 0; padding: 20px;\">\n" +
            "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 40px; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); border: 1px solid #e3e3e8;\">\n" +
            "        <div style=\"text-align: center; margin-bottom: 30px;\">\n" +
            "            <h2 style=\"color: #0066cc; margin: 0; font-family: 'Georgia', serif;\">LUXURY STAY</h2>\n" +
            "            <p style=\"font-size: 10px; text-transform: uppercase; letter-spacing: 2px; color: #a1a1a6; margin: 5px 0 0 0;\">Exquisite Travel Experiences</p>\n" +
            "        </div>\n" +
            "        <div style=\"border-top: 1px solid #e3e3e8; padding-top: 30px; color: #1d1d1f;\">\n" +
            "            <h3 style=\"color: #0066cc; margin-top: 0;\">💸 Xác nhận hoàn tiền thành công</h3>\n" +
            "            <p>Xin chào,</p>\n" +
            "            <p>Chúng tôi xác nhận đã thực hiện hoàn trả tiền cho giao dịch hủy đặt phòng của bạn tại <strong>Luxury Stay</strong>.</p>\n" +
            "            <div style=\"background-color: #f5f5f7; padding: 20px; border-radius: 12px; margin: 25px 0; border: 1px solid #e3e3e8;\">\n" +
            "                <table style=\"width: 100%; font-size: 14px;\">\n" +
            "                    <tr>\n" +
            "                        <td style=\"color: #86868b; padding-bottom: 8px;\">Mã đặt phòng:</td>\n" +
            "                        <td style=\"font-weight: bold; text-align: right; padding-bottom: 8px;\">%s</td>\n" +
            "                    </tr>\n" +
            "                    <tr style=\"border-top: 1px solid #e3e3e8;\">\n" +
            "                        <td style=\"color: #86868b; padding-top: 8px;\">Số tiền hoàn trả:</td>\n" +
            "                        <td style=\"font-weight: bold; color: #d93838; text-align: right; padding-top: 8px; font-size: 16px;\">$%s</td>\n" +
            "                    </tr>\n" +
            "                </table>\n" +
            "            </div>\n" +
            "            <p>Số tiền trên sẽ được chuyển về tài khoản của bạn tùy theo phương thức thanh toán ban đầu (thời gian nhận tiền thực tế phụ thuộc vào ngân hàng xử lý hoặc ví điện tử).</p>\n" +
            "            <p style=\"margin-top: 30px;\">Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với bộ phận hỗ trợ khách hàng của chúng tôi.<br><strong>Đội ngũ Luxury Stay Support</strong></p>\n" +
            "        </div>\n" +
            "        <div style=\"margin-top: 40px; border-top: 1px solid #e3e3e8; padding-top: 20px; text-align: center; font-size: 11px; color: #86868b;\">\n" +
            "            <p>Đây là email tự động từ hệ thống. Vui lòng không trả lời email này.</p>\n" +
            "            <p>&copy; 2026 Luxury Stay. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            bookingCode, refundAmount.setScale(2).toString()
        );

        sendEmailAsync(email, "Xác nhận hoàn tiền giao dịch - Luxury Stay", htmlContent);
    }

    private void sendEmailAsync(String to, String subject, String htmlContent) {
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                log.info("Email sent successfully to: {}", to);
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            }
        });
    }
}
