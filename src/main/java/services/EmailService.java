package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import dto.OrderResponseDTO;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ---------------------------------------------------------
    // SEND OTP EMAIL
    // ---------------------------------------------------------
    public void sendOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Your OTP Verification Code");

            // -------------------------------
            // NEW HTML TEMPLATE (FAST + CLEAN)
            // -------------------------------
            String htmlContent =
                    "<!DOCTYPE html>" +
                    "<html lang='en'>" +
                    "<head>" +
                    "  <meta charset='UTF-8' />" +
                    "  <meta name='viewport' content='width=device-width, initial-scale=1' />" +
                    "  <title>Mathews Kitchen - OTP</title>" +
                    "  <style>" +
                    "    body { background:#f2f4f7; margin:0; padding:20px; font-family: Arial, sans-serif; }" +
                    "    .card { max-width:560px; margin:20px auto; background:#ffffff; border-radius:12px; box-shadow:0 6px 20px rgba(0,0,0,0.08); padding:24px; }" +
                    "    .logo { display:block; margin:0 auto 16px auto; width:110px; height:auto; }" +
                    "    h1 { margin:0 0 8px 0; font-size:20px; color:#d35400; text-align:center; }" +
                    "    p { color:#333; line-height:1.5; font-size:14px; text-align:center; }" +
                    "    .otp { display:block; width:fit-content; margin:18px auto; padding:14px 22px; font-size:30px; font-weight:700; letter-spacing:6px; background:#f7f7f7; border-radius:10px; color:#2c3e50; }" +
                    "    .meta { text-align:center; color:#666; font-size:13px; margin-top:12px; }" +
                    "    .footer { text-align:center; color:#999; font-size:12px; margin-top:18px; }" +
                    "  </style>" +
                    "</head>" +
                    "<body>" +
                    "  <div class='card'>" +
                    "    <img class='logo' src='https://images.scalebranding.com/8a60c40c-e99c-4a18-ae81-43d8d7feab29.jpg' alt='Mathews Kitchen'/>" +
                    "    <h1>Welcome to Mathews Kitchen</h1>" +
                    "    <p>Use the following One-Time Password (OTP) to verify your email address:</p>" +
                    "    <div class='otp'>" + otp + "</div>" +
                    "    <p class='meta'>This OTP is valid for <b>5 minutes</b>. Do not share it with anyone.</p>" +
                    "    <hr style='border:none;border-top:1px solid #eee;margin:18px 0' />" +
                    "    <p style='font-size:13px;color:#444;'>If you didn’t request this, you can safely ignore the email.</p>" +
                    "    <div class='footer'>© 2025 Mathews Kitchen — All Rights Reserved</div>" +
                    "  </div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);

            // Optional plain text fallback (safe for some mail apps)
            helper.setText("Your Mathews Kitchen OTP is: " + otp + " (valid for 5 minutes)");

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Error sending OTP email: " + e.getMessage());
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // SEND ORDER INVOICE (unchanged)
    // ---------------------------------------------------------
    public void sendOrderInvoice(OrderResponseDTO order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(order.getUserEmail());
            helper.setSubject("Your Order Invoice - " + order.getOrderId());

            StringBuilder itemRows = new StringBuilder();
            for (OrderResponseDTO.OrderItemDTO item : order.getItems()) {
                itemRows.append(
                    "<tr>" +
                        "<td style='padding:8px; border:1px solid #ddd;'>" + item.getFoodName() + "</td>" +
                        "<td style='padding:8px; border:1px solid #ddd; text-align:center;'>" + item.getQuantity() + "</td>" +
                        "<td style='padding:8px; border:1px solid #ddd;'>₹" + item.getPriceAtOrder() + "</td>" +
                    "</tr>"
                );
            }

            String htmlContent =
                    "<html><body style='font-family: Arial; background:#f8f8f8; padding:20px;'>" +
                    "<div style='max-width:600px; margin:auto; background:white; padding:20px; border-radius:10px;'>" +
                    "<h2 style='text-align:center; color:#d35400;'>Mathews Kitchen</h2>" +
                    "<hr>" +
                    "<h3>Order Summary</h3>" +
                    "<p><b>Order ID:</b> " + order.getOrderId() + "</p>" +
                    "<p><b>Name:</b> " + order.getName() + "</p>" +
                    "<p><b>Email:</b> " + order.getUserEmail() + "</p>" +
                    "<p><b>Address:</b> " + order.getAddress() + "</p>" +
                    "<p><b>Ordered At:</b> " + order.getOrderedAt() + "</p>" +
                    "<h3>Items</h3>" +
                    "<table style='width:100%; border-collapse:collapse;'>" +
                    "<tr><th>Item</th><th>Qty</th><th>Price</th></tr>" +
                    itemRows +
                    "</table>" +
                    "<h3>Final Amount: ₹" + order.getFinalAmount() + "</h3>" +
                    "</div></body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send invoice: " + e.getMessage());
        }
    }
}
