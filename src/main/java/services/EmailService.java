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

    public void sendOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            // REMOVED: helper.addInline("logo", new File("/Users/naveen/Downloads/mk.jpeg")); 
            // This was causing 500 errors because the path doesn't exist on your machine.

            helper.setTo(toEmail);
            helper.setSubject("Your OTP Verification Code");

            String htmlContent =
                    "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1'>" +
                    "<style>" +
                    "   .container {" +
                    "       max-width: 480px;" +
                    "       margin: auto;" +
                    "       padding: 20px;" +
                    "       background: #ffffff;" +
                    "       border-radius: 12px;" +
                    "       box-shadow: 0 4px 20px rgba(0,0,0,0.08);" +
                    "       font-family: Arial, sans-serif;" +
                    "   }" +
                    "   .logo {" +
                    "       width: 120px;" +
                    "       display: block;" +
                    "       margin: 0 auto 20px auto;" +
                    "   }" +
                    "   .title {" +
                    "       font-size: 22px;" +
                    "       font-weight: bold;" +
                    "       text-align: center;" +
                    "       color: #d35400;" +   // warm food color
                    "   }" +
                    "   .otp-box {" +
                    "       font-size: 32px;" +
                    "       font-weight: bold;" +
                    "       letter-spacing: 6px;" +
                    "       text-align: center;" +
                    "       margin: 25px 0;" +
                    "       color: #2c3e50;" +
                    "       background: #f7f7f7;" +
                    "       padding: 15px;" +
                    "       border-radius: 10px;" +
                    "   }" +
                    "   .footer {" +
                    "       text-align: center;" +
                    "       font-size: 12px;" +
                    "       color: #777;" +
                    "       margin-top: 20px;" +
                    "   }" +
                    "</style>" +
                    "</head>" +
                    "<body style='background:#f2f2f2; padding:20px;'>" +

                    "<div class='container'>" +

                    // --- LOGO IMAGE ---
                    "<img src='https://images.scalebranding.com/8a60c40c-e99c-4a18-ae81-43d8d7feab29.jpg' class='logo' alt='Mathews Kitchen'>" +

                    "<h2 class='title'>Welcome to Mathews Kitchen!</h2>" +

                    "<p style='font-size:15px; color:#333;'>Thank you for registering with <b>Mathews Kitchen Food Application</b>. To complete your registration, please use the One-Time Password (OTP) given below:</p>" +

                    "<div class='otp-box'>" + otp + "</div>" +

                    "<p style='font-size:15px; color:#333; text-align:center;'>This OTP is valid for <b>5 minutes</b>. Do not share it with anyone for security purposes.</p>" +

                    "<hr style='margin:25px 0; border:none; border-top:1px solid #eee;'>" +

                    "<p style='font-size:14px; text-align:center; color:#666;'>If you did not request this, you can safely ignore this email.</p>" +

                    "<div class='footer'>© 2025 Mathews Kitchen — All Rights Reserved</div>" +

                    "</div>" +
                    "</body>" +
                    "</html>";


            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            // Log the error but don't rethrow it immediately to avoid masking the user creation
            System.err.println("Error sending email: " + e.getMessage());
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }
    
    public void sendOrderInvoice(OrderResponseDTO order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(order.getUserEmail());
            helper.setSubject("Your Order Invoice - " + order.getOrderId());

            // Build Item Table
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
                    "<html>" +
                    "<body style='font-family: Arial, sans-serif; background:#f8f8f8; padding:20px;'>" +

                    "<div style='max-width:600px; margin:auto; background:white; padding:20px; border-radius:10px; box-shadow:0 2px 10px rgba(0,0,0,0.1);'>" +

                    // Header
                    "<h2 style='text-align:center; color:#d35400;'>Mathews Kitchen</h2>" +
                    "<p style='text-align:center;'>Thank you for ordering with us!</p>" +
                    "<hr>" +

                    // Basic Info
                    "<h3>Order Summary</h3>" +
                    "<p><b>Order ID:</b> " + order.getOrderId() + "</p>" +
                    "<p><b>Name:</b> " + (order.getName() == null ? "-" : order.getName()) + "</p>" +
                    "<p><b>Email:</b> " + order.getUserEmail() + "</p>" +
                    "<p><b>Address:</b> " + order.getAddress() + "</p>" +
                    "<p><b>Order Time:</b> " + order.getOrderedAt() + "</p>" +
                    "<p><b>Payment Mode:</b> " + order.getPaymentMode() + "</p>" +

                    "<h3>Items</h3>" +
                    "<table style='width:100%; border-collapse:collapse;'>" +
                    "<tr style='background:#f2f2f2;'>" +
                    "<th style='padding:8px; border:1px solid #ddd;'>Item</th>" +
                    "<th style='padding:8px; border:1px solid #ddd;'>Qty</th>" +
                    "<th style='padding:8px; border:1px solid #ddd;'>Price</th>" +
                    "</tr>" +
                    itemRows +
                    "</table>" +

                    "<br>" +

                    "<h3>Bill Details</h3>" +
                    "<p><b>Total Amount:</b> ₹" + order.getTotalAmount() + "</p>" +
                    "<p><b>Discount:</b> ₹" + order.getDiscount() + "</p>" +
                    "<p><b>GST:</b> ₹" + order.getGst() + "</p>" +
                    "<p><b>Delivery Fee:</b> ₹" + order.getDeliveryFee() + "</p>" +

                    "<h2 style='color:#27ae60;'>Final Amount: ₹" + order.getFinalAmount() + "</h2>" +

                    "<hr>" +
                    "<p style='text-align:center; font-size:13px; color:#777;'>We hope you enjoy your meal! 🍽️</p>" +
                    "<p style='text-align:center; font-size:12px;'>© 2025 Mathews Kitchen</p>" +

                    "</div>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send order invoice: " + e.getMessage());
        }
    }

}
