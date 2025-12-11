//package services;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.Duration;
//
//import java.util.Objects;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import dto.OrderResponseDTO;
//import jakarta.mail.internet.MimeMessage;
//
//@Service
//public class EmailService {
//
//    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
//
//    @Autowired(required = false)
//    private JavaMailSender mailSender; // may be null if not configured
//
//    @Autowired
//    private Environment env; // used to read MAIL_USERNAME / SENDGRID_API_KEY / MAIL_FROM
//
//    // -------------------------
//    // Public API (unchanged signature)
//    // -------------------------
//    /**
//     * Send OTP email (HTML + plain text fallback).
//     *
//     * @param toEmail recipient email
//     * @param otp one-time password
//     */
//    public void sendOtp(String toEmail, String otp) {
//        String subject = "Your OTP Verification Code - Mathews Kitchen";
//
//        // HTML content (kept from your original template)
//        String htmlContent =
//                "<!DOCTYPE html>" +
//                "<html lang='en'>" +
//                "<head>" +
//                "  <meta charset='UTF-8' />" +
//                "  <meta name='viewport' content='width=device-width, initial-scale=1' />" +
//                "  <title>Mathews Kitchen - OTP</title>" +
//                "  <style>" +
//                "    body { background:#f2f4f7; margin:0; padding:20px; font-family: Arial, sans-serif; }" +
//                "    .card { max-width:560px; margin:20px auto; background:#ffffff; border-radius:12px; box-shadow:0 6px 20px rgba(0,0,0,0.08); padding:24px; }" +
//                "    .logo { display:block; margin:0 auto 16px auto; width:110px; height:auto; }" +
//                "    h1 { margin:0 0 8px 0; font-size:20px; color:#d35400; text-align:center; }" +
//                "    p { color:#333; line-height:1.5; font-size:14px; text-align:center; }" +
//                "    .otp { display:block; width:fit-content; margin:18px auto; padding:14px 22px; font-size:30px; font-weight:700; letter-spacing:6px; background:#f7f7f7; border-radius:10px; color:#2c3e50; }" +
//                "    .meta { text-align:center; color:#666; font-size:13px; margin-top:12px; }" +
//                "    .footer { text-align:center; color:#999; font-size:12px; margin-top:18px; }" +
//                "  </style>" +
//                "</head>" +
//                "<body>" +
//                "  <div class='card'>" +
//                "    <img class='logo' src='https://images.scalebranding.com/8a60c40c-e99c-4a18-ae81-43d8d7feab29.jpg' alt='Mathews Kitchen'/>" +
//                "    <h1>Welcome to Mathews Kitchen</h1>" +
//                "    <p>Use the following One-Time Password (OTP) to verify your email address:</p>" +
//                "    <div class='otp' style='font-family: monospace;'>" + escapeHtml(otp) + "</div>" +
//                "    <p class='meta'>This OTP is valid for <b>5 minutes</b>. Do not share it with anyone.</p>" +
//                "    <hr style='border:none;border-top:1px solid #eee;margin:18px 0' />" +
//                "    <p style='font-size:13px;color:#444;'>If you didn’t request this, you can safely ignore the email.</p>" +
//                "    <div class='footer'>© 2025 Mathews Kitchen — All Rights Reserved</div>" +
//                "  </div>" +
//                "</body>" +
//                "</html>";
//
//        String plainText = "Your Mathews Kitchen OTP is: " + otp + " (valid for 5 minutes). If you did not request this, ignore this email.";
//
//        // Try SendGrid first (HTTP) if configured
//        String sgKey = env.getProperty("SENDGRID_API_KEY");
//        if (sgKey != null && !sgKey.isBlank()) {
//            try {
//                sendViaSendGrid(toEmail, subject, plainText, htmlContent);
//                log.info("[EMAIL] (SendGrid) OTP email sent to {}", toEmail);
//                return;
//            } catch (Exception e) {
//                log.error("[EMAIL] SendGrid attempt failed, falling back to SMTP: {}", e.getMessage(), e);
//                // fall through to SMTP fallback
//            }
//        }
//
//        // Fallback to SMTP using JavaMailSender (existing behavior)
//        if (mailSender == null) {
//            log.error("[EMAIL] No JavaMailSender configured and SendGrid unavailable - cannot send OTP to {}", toEmail);
//            throw new IllegalStateException("No email sender available");
//        }
//
//        MimeMessage message = null;
//        try {
//            message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            // Use MAIL_USERNAME as From (falls back to no-reply@... if not set)
//            String from = env.getProperty("MAIL_USERNAME");
//            if (from == null || from.isBlank()) {
//                from = env.getProperty("MAIL_FROM", "no-reply@mathewskitchen.com");
//                log.warn("MAIL_USERNAME not set; using fallback from address {}", from);
//            }
//            helper.setFrom(from);
//            helper.setTo(toEmail);
//            helper.setSubject(subject);
//
//            // Set both plain and html parts
//            helper.setText(plainText, htmlContent);
//
//            mailSender.send(message);
//            log.info("[EMAIL] (SMTP) OTP email sent to {}", toEmail);
//
//        } catch (Exception e) {
//            log.error("[EMAIL] Failed to send OTP to {} via SMTP: {}", toEmail, e.getMessage(), e);
//            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Send an order invoice email with HTML table + plain text fallback.
//     *
//     * @param order the order DTO containing items and user details
//     */
//    public void sendOrderInvoice(OrderResponseDTO order) {
//        if (order == null) {
//            throw new IllegalArgumentException("Order cannot be null");
//        }
//
//        String subject = "Your Order Invoice - " + order.getOrderId();
//
//        // Build HTML and plain text content (kept your original formatting)
//        StringBuilder itemRows = new StringBuilder();
//        if (order.getItems() != null) {
//            for (OrderResponseDTO.OrderItemDTO item : order.getItems()) {
//                itemRows.append(
//                    "<tr>" +
//                        "<td style='padding:8px; border:1px solid #ddd;'>" + escapeHtml(item.getFoodName()) + "</td>" +
//                        "<td style='padding:8px; border:1px solid #ddd; text-align:center;'>" + item.getQuantity() + "</td>" +
//                        "<td style='padding:8px; border:1px solid #ddd;'>₹" + item.getPriceAtOrder() + "</td>" +
//                    "</tr>"
//                );
//            }
//        }
//
//        String htmlContent =
//                "<html><body style='font-family: Arial; background:#f8f8f8; padding:20px;'>" +
//                "<div style='max-width:700px; margin:auto; background:white; padding:20px; border-radius:10px;'>" +
//                "<h2 style='text-align:center; color:#d35400;'>Mathews Kitchen</h2>" +
//                "<hr>" +
//                "<h3>Order Summary</h3>" +
//                "<p><b>Order ID:</b> " + escapeHtml(order.getOrderId()) + "</p>" +
//                "<p><b>Name:</b> " + escapeHtml(order.getName()) + "</p>" +
//                "<p><b>Email:</b> " + escapeHtml(order.getUserEmail()) + "</p>" +
//                "<p><b>Address:</b> " + escapeHtml(order.getAddress()) + "</p>" +
//                "<p><b>Ordered At:</b> " + escapeHtml(Objects.toString(order.getOrderedAt(), "")) + "</p>" +
//                "<h3>Items</h3>" +
//                "<table style='width:100%; border-collapse:collapse;'>" +
//                "<thead><tr><th style='text-align:left;padding:8px;border:1px solid #ddd;'>Item</th><th style='padding:8px;border:1px solid #ddd;'>Qty</th><th style='padding:8px;border:1px solid #ddd;'>Price</th></tr></thead>" +
//                "<tbody>" +
//                itemRows.toString() +
//                "</tbody>" +
//                "</table>" +
//                "<h3>Final Amount: ₹" + order.getFinalAmount() + "</h3>" +
//                "<div style='font-size:12px;color:#777;margin-top:12px;'>If you have any questions, reply to this email.</div>" +
//                "</div></body></html>";
//
//        StringBuilder plain = new StringBuilder();
//        plain.append("Mathews Kitchen - Order Invoice\n");
//        plain.append("Order ID: ").append(order.getOrderId()).append("\n");
//        plain.append("Name: ").append(order.getName()).append("\n");
//        plain.append("Email: ").append(order.getUserEmail()).append("\n");
//        plain.append("Address: ").append(order.getAddress()).append("\n");
//        plain.append("Ordered At: ").append(Objects.toString(order.getOrderedAt(), "")).append("\n\n");
//        plain.append("Items:\n");
//        if (order.getItems() != null) {
//            for (OrderResponseDTO.OrderItemDTO item : order.getItems()) {
//                plain.append("- ").append(item.getFoodName()).append(" x").append(item.getQuantity()).append(" : ₹").append(item.getPriceAtOrder()).append("\n");
//            }
//        }
//        plain.append("\nFinal Amount: ₹").append(order.getFinalAmount()).append("\n");
//
//        // Try SendGrid first
//        String sgKey = env.getProperty("SENDGRID_API_KEY");
//        if (sgKey != null && !sgKey.isBlank()) {
//            try {
//                sendViaSendGrid(order.getUserEmail(), subject, plain.toString(), htmlContent);
//                log.info("[EMAIL] (SendGrid) Order invoice sent to {}", order.getUserEmail());
//                return;
//            } catch (Exception e) {
//                log.error("[EMAIL] SendGrid invoice attempt failed, falling back to SMTP: {}", e.getMessage(), e);
//            }
//        }
//
//        // Fallback SMTP
//        if (mailSender == null) {
//            log.error("[EMAIL] No JavaMailSender configured and SendGrid unavailable - cannot send invoice to {}", order.getUserEmail());
//            throw new IllegalStateException("No email sender available");
//        }
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            String from = env.getProperty("MAIL_USERNAME");
//            if (from == null || from.isBlank()) {
//                from = env.getProperty("MAIL_FROM", "no-reply@mathewskitchen.com");
//            }
//            helper.setFrom(from);
//            helper.setTo(order.getUserEmail());
//            helper.setSubject(subject);
//            helper.setText(plain.toString(), htmlContent);
//
//            mailSender.send(message);
//            log.info("[EMAIL] (SMTP) Order invoice sent to {} for order {}", order.getUserEmail(), order.getOrderId());
//
//        } catch (Exception e) {
//            log.error("[EMAIL] Failed to send order invoice for order {}: {}", order != null ? order.getOrderId() : "unknown", e.getMessage(), e);
//            throw new RuntimeException("Failed to send order invoice: " + e.getMessage(), e);
//        }
//    }
//
//    // -------------------------
//    // Helper: Send via SendGrid HTTP API (java.net.http)
//    // -------------------------
//    private void sendViaSendGrid(String to, String subject, String plainText, String html) throws Exception {
//        String apiKey = env.getProperty("SENDGRID_API_KEY");
//        if (apiKey == null || apiKey.isBlank()) {
//            throw new IllegalStateException("SENDGRID_API_KEY not configured");
//        }
//        String from = env.getProperty("MAIL_FROM", env.getProperty("MAIL_USERNAME", "no-reply@mathewskitchen.com"));
//
//        HttpClient client = HttpClient.newBuilder()
//                .connectTimeout(Duration.ofSeconds(10))
//                .build();
//
//        // Build simple JSON payload (escape quotes/newlines)
//        String payload = "{"
//                + "\"personalizations\":[{\"to\":[{\"email\":\"" + escapeJson(to) + "\"}]}],"
//                + "\"from\":{\"email\":\"" + escapeJson(from) + "\"},"
//                + "\"subject\":\"" + escapeJson(subject) + "\","
//                + "\"content\":["
//                + "{\"type\":\"text/plain\",\"value\":\"" + escapeJson(plainText) + "\"},"
//                + "{\"type\":\"text/html\",\"value\":\"" + escapeJson(html) + "\"}"
//                + "]"
//                + "}";
//
//        HttpRequest req = HttpRequest.newBuilder()
//                .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
//                .timeout(Duration.ofSeconds(20))
//                .header("Authorization", "Bearer " + apiKey)
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(payload))
//                .build();
//
//        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
//        if (resp.statusCode() >= 400) {
//            log.error("[SENDGRID] Send failed: status={} body={}", resp.statusCode(), resp.body());
//            throw new RuntimeException("SendGrid error: " + resp.statusCode() + " - " + resp.body());
//        }
//        log.info("[SENDGRID] Sent to {} (status {})", to, resp.statusCode());
//    }
//
//    // -------------------------
//    // Small escaping helpers
//    // -------------------------
//    private String escapeHtml(String s) {
//        if (s == null) return "";
//        return s
//                .replace("&", "&amp;")
//                .replace("<", "&lt;")
//                .replace(">", "&gt;");
//    }
//
//    private String escapeJson(String s) {
//        if (s == null) return "";
//        return s.replace("\\", "\\\\")
//                .replace("\"", "\\\"")
//                .replace("\n", "\\n")
//                .replace("\r", "\\r");
//    }
//}


package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import dto.OrderResponseDTO;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final Environment env;
    private final HttpClient httpClient;

    public EmailService(Environment env) {
        this.env = env;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // -------------------------
    // SEND OTP (SendGrid HTTP)
    // -------------------------
    public void sendOtp(String toEmail, String otp) {
        String subject = "Mathews Kitchen — Your OTP";
        String plain = "Your Mathews Kitchen OTP is: " + otp + " (valid for 5 minutes). If you did not request this, ignore this email.";
        String html = buildOtpHtml(otp);

        sendViaSendGrid(toEmail, subject, plain, html);
        log.info("[EMAIL] OTP request completed for {}", toEmail);
    }

    // -------------------------
    // SEND ORDER INVOICE (SendGrid HTTP)
    // -------------------------
    public void sendOrderInvoice(OrderResponseDTO order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");

        String subject = "Your Order Invoice - " + order.getOrderId();
        String html = buildInvoiceHtml(order);
        String plain = buildInvoicePlain(order);

        sendViaSendGrid(order.getUserEmail(), subject, plain, html);
        log.info("[EMAIL] Invoice sent to {} for order {}", order.getUserEmail(), order.getOrderId());
    }

    // -------------------------
    // SendGrid HTTP helper (throws on failure)
    // -------------------------
    private void sendViaSendGrid(String to, String subject, String plainText, String html) {
        String apiKey = env.getProperty("SENDGRID_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            log.error("[SENDGRID] SENDGRID_API_KEY not configured");
            throw new IllegalStateException("SENDGRID_API_KEY not configured");
        }

        String from = env.getProperty("MAIL_FROM", "no-reply@mathewskitchen.com");

        String payload = "{"
                + "\"personalizations\":[{\"to\":[{\"email\":\"" + escapeJson(to) + "\"}]}],"
                + "\"from\":{\"email\":\"" + escapeJson(from) + "\"},"
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"content\":["
                + "{\"type\":\"text/plain\",\"value\":\"" + escapeJson(plainText) + "\"},"
                + "{\"type\":\"text/html\",\"value\":\"" + escapeJson(html) + "\"}"
                + "]"
                + "}";

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 400) {
                log.error("[SENDGRID] Send failed: status={} body={}", resp.statusCode(), resp.body());
                throw new RuntimeException("SendGrid error: " + resp.statusCode() + " - " + resp.body());
            }

            log.info("[SENDGRID] Sent to {} (status {})", to, resp.statusCode());
        } catch (Exception e) {
            log.error("[SENDGRID] Exception sending email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed sending email: " + e.getMessage(), e);
        }
    }

    // -------------------------
    // HTML builders & helpers
    // -------------------------
    private String buildOtpHtml(String otp) {
        return "<!doctype html><html><body style='font-family:Arial,sans-serif;'>" +
                "<div style='max-width:560px;margin:20px auto;padding:24px;background:#fff;border-radius:10px;'>" +
                "<h2 style='color:#d35400;text-align:center;'>Mathews Kitchen</h2>" +
                "<p style='text-align:center;'>Your OTP to verify your email:</p>" +
                "<div style='font-size:28px;font-weight:700;text-align:center;padding:12px;background:#f7f7f7;border-radius:8px;'>" + escapeHtml(otp) + "</div>" +
                "<p style='text-align:center;color:#666;'>This OTP is valid for 5 minutes. Do not share it.</p>" +
                "</div></body></html>";
    }

    private String buildInvoiceHtml(OrderResponseDTO order) {
        StringBuilder itemRows = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderResponseDTO.OrderItemDTO item : order.getItems()) {
                itemRows.append("<tr>")
                        .append("<td style='padding:8px;border:1px solid #ddd;'>").append(escapeHtml(item.getFoodName())).append("</td>")
                        .append("<td style='padding:8px;border:1px solid #ddd;text-align:center;'>").append(item.getQuantity()).append("</td>")
                        .append("<td style='padding:8px;border:1px solid #ddd;'>₹").append(item.getPriceAtOrder()).append("</td>")
                        .append("</tr>");
            }
        }

        return "<html><body style='font-family:Arial,sans-serif;'><div style='max-width:700px;margin:auto;background:#fff;padding:20px;border-radius:10px;'>"
                + "<h2 style='color:#d35400;text-align:center;'>Mathews Kitchen</h2>"
                + "<p><b>Order ID:</b> " + escapeHtml(order.getOrderId()) + "</p>"
                + "<table style='width:100%;border-collapse:collapse;'><thead><tr><th style='padding:8px;border:1px solid #ddd;'>Item</th><th style='padding:8px;border:1px solid #ddd;'>Qty</th><th style='padding:8px;border:1px solid #ddd;'>Price</th></tr></thead><tbody>"
                + itemRows.toString()
                + "</tbody></table><h3>Final Amount: ₹" + order.getFinalAmount() + "</h3></div></body></html>";
    }

    private String buildInvoicePlain(OrderResponseDTO order) {
        StringBuilder plain = new StringBuilder();
        plain.append("Mathews Kitchen - Order Invoice\n");
        plain.append("Order ID: ").append(order.getOrderId()).append("\n");
        plain.append("Name: ").append(order.getName()).append("\n");
        plain.append("Items:\n");
        if (order.getItems() != null) {
            for (OrderResponseDTO.OrderItemDTO item : order.getItems()) {
                plain.append("- ").append(item.getFoodName()).append(" x").append(item.getQuantity()).append(" : ₹").append(item.getPriceAtOrder()).append("\n");
            }
        }
        plain.append("\nFinal Amount: ₹").append(order.getFinalAmount()).append("\n");
        return plain.toString();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
