package com.hotelbooking.common.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Generates HMAC-SHA256 signed QR codes for meal tickets.
 * The QR payload contains: ticketId:userId:timestamp:nonce:signature
 * AC-062: HMAC-SHA256 signature prevents QR forgery.
 */
@Component
@Slf4j
public class QrCodeGenerator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String PAYLOAD_SEPARATOR = ":";
    private static final int QR_SIZE = 300;

    @Value("${meal-ticket.qr-secret:default-secret-key-for-qr-codes-minimum-32-chars-long}")
    private String qrSecret;

    /**
     * Generates a signed QR payload string and returns it along with
     * a base64-encoded PNG image of the QR code.
     *
     * @param ticketId the meal ticket ID
     * @param userId the ticket holder's user ID
     * @return QrResult containing the raw payload string and base64 PNG image
     */
    public QrResult generateQr(Long ticketId, Long userId) {
        String nonce = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        String rawPayload = String.join(PAYLOAD_SEPARATOR,
                ticketId.toString(), userId.toString(), String.valueOf(timestamp), nonce);
        String signature = sign(rawPayload);
        String fullPayload = rawPayload + PAYLOAD_SEPARATOR + signature;

        String base64Png = generateQrImageBase64(fullPayload);

        return new QrResult(fullPayload, base64Png, signature);
    }

    /**
     * Verifies a scanned QR payload and extracts the ticket ID and user ID.
     * Throws SecurityException if the signature is invalid.
     */
    public QrPayload verifyQr(String qrPayload) {
        if (qrPayload == null || qrPayload.isBlank()) {
            throw new SecurityException("INVALID_QR_CODE: QR payload is empty");
        }

        String[] parts = qrPayload.split(PAYLOAD_SEPARATOR);
        if (parts.length < 5) {
            throw new SecurityException("INVALID_QR_CODE: Malformed QR payload");
        }

        String ticketIdStr = parts[0];
        String userIdStr = parts[1];
        String timestampStr = parts[2];
        String providedSignature = parts[parts.length - 1];

        // Reconstruct the payload for signature verification
        String rawPayload = String.join(PAYLOAD_SEPARATOR, ticketIdStr, userIdStr, timestampStr,
                String.join(PAYLOAD_SEPARATOR,
                        java.util.Arrays.copyOfRange(parts, 3, parts.length - 1)));

        String expectedSignature = sign(rawPayload);
        if (!expectedSignature.equals(providedSignature)) {
            throw new SecurityException("INVALID_QR_CODE: Signature mismatch — possible tampering");
        }

        // Check timestamp is within a reasonable window (e.g., 30 days)
        long timestamp = Long.parseLong(timestampStr);
        long thirtyDaysMs = 30L * 24 * 60 * 60 * 1000;
        if (Math.abs(System.currentTimeMillis() - timestamp) > thirtyDaysMs) {
            throw new SecurityException("INVALID_QR_CODE: QR code is too old");
        }

        return new QrPayload(Long.parseLong(ticketIdStr), Long.parseLong(userIdStr));
    }

    /**
     * Regenerates the PNG image for an existing QR payload.
     */
    public String regenerateQrImage(String qrPayload) {
        return generateQrImageBase64(qrPayload);
    }

    private String generateQrImageBase64(String content) {
        try {
            QRCodeWriter qrWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            BitMatrix bitMatrix = qrWriter.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);
            byte[] pngBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(pngBytes);
        } catch (WriterException | java.io.IOException e) {
            log.error("Failed to generate QR code image", e);
            throw new RuntimeException("QR code generation failed", e);
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    qrSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hmacBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC signing failed", e);
        }
    }

    public record QrResult(String payload, String base64Png, String signature) {}

    public record QrPayload(Long ticketId, Long userId) {}
}
