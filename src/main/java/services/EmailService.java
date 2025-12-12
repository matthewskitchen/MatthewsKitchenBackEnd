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

    // -----------------------------------------------------
    // PUBLIC METHODS
    // -----------------------------------------------------

    /** Sends OTP email */
    public void sendOtp(String toEmail, String otp) {
        String subject = "Your Mathews Kitchen OTP";
        String text = "Your OTP is: " + otp + " (valid for 5 mins)";
        String html = buildOtpHtml(otp);

        sendViaSendGrid(toEmail, subject, text, html);
        log.info("[EMAIL] OTP sent to {}", toEmail);
    }

    /** Sends order invoice email */
    public void sendOrderInvoice(OrderResponseDTO order) {
        if (order == null) throw new IllegalArgumentException("Order cannot be null");

        String subject = "Order Invoice - " + order.getOrderId();
        String text = buildInvoicePlain(order);
        String html = buildInvoiceHtml(order);

        sendViaSendGrid(order.getUserEmail(), subject, text, html);
        log.info("[EMAIL] Invoice sent to {} for order {}", order.getUserEmail(), order.getOrderId());
    }

    // -----------------------------------------------------
    // SENDGRID API SENDER
    // -----------------------------------------------------
    private void sendViaSendGrid(String to, String subject, String plainText, String html) {

        String apiKey = env.getProperty("SENDGRID_API_KEY");
        String from = env.getProperty("MAIL_FROM", "no-reply@mathewskitchen.in");

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("SENDGRID_API_KEY not set");
        }

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
                log.error("[SENDGRID ERROR] status={} body={}", resp.statusCode(), resp.body());
                throw new RuntimeException("SendGrid error: " + resp.statusCode());
            }

            log.info("[SENDGRID] Email sent successfully → {}", to);

        } catch (Exception e) {
            log.error("[SENDGRID] Failed to send email: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------
    // OTP TEMPLATE (Swiggy/Zomato Premium UI)
    // -----------------------------------------------------
    private String buildOtpHtml(String otp) {
        return """
        <!DOCTYPE html>
        <html>
        <body style='margin:0;padding:0;background:#fafafa;font-family:Arial,sans-serif;'>

        <table width='100%' cellpadding='0' cellspacing='0' style='background:#fafafa;'>
        <tr><td>

        <!-- Header -->
        <table width='100%' style="background:linear-gradient(90deg,#ff6a00,#ff3d00);padding:25px 0;">
            <tr><td align='center'>
                <div style='font-size:26px;color:white;font-weight:700;'>Mathews Kitchen</div>
                <div style='color:#ffe9d6;font-size:14px;'>Fast. Fresh. Delicious.</div>
            </td></tr>
        </table>

        <!-- Content Box -->
        <table width='100%' style='max-width:520px;margin:30px auto;background:white;
               border-radius:12px;padding:28px;box-shadow:0 6px 22px rgba(0,0,0,0.08);'>
        
            <tr><td>
                <h2 style='margin:0 0 12px;color:#333;'>Your OTP Code</h2>

                <p style='color:#555;font-size:15px;margin-bottom:20px;'>
                    Use the OTP below to verify your email address.
                </p>

                <div style='text-align:center;margin:30px 0;'>
                    <div style='display:inline-block;background:#f7f7f7;border:2px dashed #ff6a00;
                                color:#ff3d00;font-size:32px;font-weight:700;
                                padding:16px 24px;border-radius:10px;'>
                        """ + otp + """
                    </div>
                </div>

                <p style='color:#777;font-size:13px;text-align:center;'>
                    This OTP is valid for 5 minutes. Do not share it.
                </p>
            </td></tr>
        </table>

        </td></tr></table>
        </body>
        </html>
        """;
    }

    // -----------------------------------------------------
    // INVOICE HTML (Swiggy/Zomato Styled)
    // -----------------------------------------------------
    private String buildInvoiceHtml(OrderResponseDTO order) {

        StringBuilder items = new StringBuilder();
        for (OrderResponseDTO.OrderItemDTO i : order.getItems()) {
            items.append("<tr>")
                 .append("<td style='padding:10px;border-bottom:1px solid #eee;'>" + escapeHtml(i.getFoodName()) + "</td>")
                 .append("<td style='padding:10px;text-align:center;border-bottom:1px solid #eee;'>" + i.getQuantity() + "</td>")
                 .append("<td style='padding:10px;text-align:right;border-bottom:1px solid #eee;'>₹" + i.getPriceAtOrder() + "</td>")
                 .append("</tr>");
        }

        return """
        <html>
        <body style='background:#fafafa;font-family:Arial,sans-serif;margin:0;padding:0;'>

        <table width='100%' cellpadding='0' cellspacing='0'>
        <tr><td>

        <!-- Header -->
        <table width='100%' style="background:linear-gradient(90deg,#ff6a00,#ff3d00);padding:26px 0;">
            <tr><td align='center'>
                <div style='font-size:26px;color:white;font-weight:700;'>Mathews Kitchen</div>
                <div style='color:#ffe9d6;font-size:14px;'>Order Invoice</div>
            </td></tr>
        </table>

        <!-- Content -->
        <table width='100%' style='max-width:650px;margin:30px auto;background:#fff;
                border-radius:12px;padding:25px;box-shadow:0 6px 22px rgba(0,0,0,0.08);'>
        
        <tr><td>

            <h2 style='margin-top:0;'>Order Details</h2>
            <p><b>Order ID:</b> """ + order.getOrderId() + """</p>
            <p><b>Name:</b> """ + escapeHtml(order.getName()) + """</p>

            <table style='width:100%;border-collapse:collapse;margin-top:20px;'>
                <thead>
                    <tr style='background:#ffe8dc;'>
                        <th style='padding:10px;text-align:left;'>Item</th>
                        <th style='padding:10px;text-align:center;'>Qty</th>
                        <th style='padding:10px;text-align:right;'>Price</th>
                    </tr>
                </thead>
                <tbody>""" + items + """</tbody>
            </table>

            <h3 style='text-align:right;margin-top:25px;'>Final Amount: ₹""" + order.getFinalAmount() + """</h3>

        </td></tr>
        </table>

        </td></tr></table>

        </body>
        </html>
        """;
    }

    // Plain text fallback
    private String buildInvoicePlain(OrderResponseDTO order) {
        StringBuilder out = new StringBuilder();
        out.append("Mathews Kitchen - Invoice\n");
        out.append("Order ID: ").append(order.getOrderId()).append("\n");
        out.append("Items:\n");
        for (OrderResponseDTO.OrderItemDTO i : order.getItems()) {
            out.append("- ").append(i.getFoodName())
               .append(" x ").append(i.getQuantity())
               .append(" = ₹").append(i.getPriceAtOrder()).append("\n");
        }
        out.append("\nTotal: ₹").append(order.getFinalAmount());
        return out.toString();
    }

    // -----------------------------------------------------
    // JSON & HTML sanitizers
    // -----------------------------------------------------
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"");
    }
}
