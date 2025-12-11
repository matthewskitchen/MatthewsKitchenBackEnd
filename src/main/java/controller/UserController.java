package controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import dbmodel.EmailOtp;
import dbmodel.users;
import dto.OtpUtil;
import repository.EmailOtpRepository;
import repository.UserRepo;
import services.EmailService;
import services.Response;
import services.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailOtpRepository otpRepo;

    @Autowired
    private EmailService emailService;

    // ---------------------------------------------------------
    // TEST EMAIL
    // ---------------------------------------------------------
    @GetMapping("/test_email")
    public String testEmail() {
        log.info("[TEST_EMAIL] Sending test OTP email");
        try {
            emailService.sendOtp("naveenkishore20022@gmail.com", "123456");
            log.info("[TEST_EMAIL] Email send attempted");
        } catch (Exception e) {
            log.error("[TEST_EMAIL] Error sending test email: {}", e.getMessage(), e);
        }
        return "Test Email Attempted";
    }

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------
    @PostMapping("/register")
    public Response registerUser(@RequestBody users u) {

        log.info("[REGISTER] New registration request for email={}", u.getEmail());

        if (userRepo.findByEmail(u.getEmail()).isPresent()) {
            log.warn("[REGISTER] Email already exists: {}", u.getEmail());
            return new Response("Email already exists", null, 400);
        }

        u.setEnabled(false);
        userRepo.save(u);
        log.info("[REGISTER] User saved as disabled: {}", u.getEmail());

        // Generate OTP
        String otp = OtpUtil.generateOtp();
        EmailOtp emailOtp = new EmailOtp(
                u.getEmail(),
                otp,
                LocalDateTime.now().plusMinutes(5)
        );
        otpRepo.save(emailOtp);
        log.info("[REGISTER] OTP stored for email={} with expiry={}", u.getEmail(), emailOtp.getExpiry());

        // Send OTP Email (non-fatal)
        try {
            emailService.sendOtp(u.getEmail(), otp);
            log.info("[REGISTER] OTP email triggered for email={}", u.getEmail());
        } catch (Exception e) {
            log.error("[REGISTER] Failed to send OTP email to {}: {}", u.getEmail(), e.getMessage());
        }

        return new Response("OTP sent to your email (or attempted)", null, 200);
    }

    // ---------------------------------------------------------
    // VERIFY OTP
    // ---------------------------------------------------------
    @PostMapping("/verify-otp")
    public Response verifyOtp(@RequestBody Map<String, String> data) {

        String email = data.get("email");
        String otp = data.get("otp");
        log.info("[VERIFY_OTP] Verifying OTP for email={}", email);

        Optional<EmailOtp> otpObj = otpRepo.findByEmail(email);

        if (otpObj.isEmpty()) {
            log.warn("[VERIFY_OTP] No OTP found for email={}", email);
            return new Response("OTP not found, register again", null, 400);
        }

        EmailOtp emailOtp = otpObj.get();

        if (!emailOtp.getOtp().equals(otp)) {
            log.warn("[VERIFY_OTP] Invalid OTP for email={}", email);
            return new Response("Invalid OTP", null, 400);
        }

        if (emailOtp.getExpiry().isBefore(LocalDateTime.now())) {
            log.warn("[VERIFY_OTP] OTP expired for email={}", email);
            return new Response("OTP expired", null, 400);
        }

        users user = userRepo.findByEmail(email).get();
        user.setEnabled(true);
        userRepo.save(user);
        log.info("[VERIFY_OTP] Email verified & user enabled: {}", email);

        otpRepo.delete(emailOtp);
        log.debug("[VERIFY_OTP] OTP deleted");

        return new Response("Email verified successfully!", null, 200);
    }

    // ---------------------------------------------------------
    // RESEND OTP
    // ---------------------------------------------------------
    @PostMapping("/resend-otp")
    public Response resendOtp(@RequestBody Map<String, String> data) {

        String email = data.get("email");
        log.info("[RESEND_OTP] Resend OTP requested for email={}", email);

        Optional<users> u = userRepo.findByEmail(email);

        if (u.isEmpty()) {
            log.warn("[RESEND_OTP] Email not registered: {}", email);
            return new Response("Email not registered", null, 400);
        }

        if (u.get().isEnabled()) {
            log.info("[RESEND_OTP] Email already verified: {}", email);
            return new Response("Email already verified", null, 200);
        }

        String otp = OtpUtil.generateOtp();
        EmailOtp emailOtp = new EmailOtp(
                email,
                otp,
                LocalDateTime.now().plusMinutes(5)
        );
        otpRepo.save(emailOtp);
        log.info("[RESEND_OTP] New OTP stored with expiry={}", emailOtp.getExpiry());

        try {
            emailService.sendOtp(email, otp);
            log.info("[RESEND_OTP] OTP sent to email={}", email);
        } catch (Exception e) {
            log.error("[RESEND_OTP] Failed to resend OTP to {}: {}", email, e.getMessage());
        }

        return new Response("OTP resent (or attempted)", null, 200);
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------
    @PostMapping("/login")
    public Response loginUser(@RequestBody users u) {

        log.info("[LOGIN] Login attempt for email={}", u.getEmail());

        Optional<users> opt = userRepo.findByEmail(u.getEmail());

        if (opt.isEmpty()) {
            log.warn("[LOGIN] Invalid email: {}", u.getEmail());
            return new Response("Invalid Email", null, 400);
        }

        users user = opt.get();

        if (!user.isEnabled()) {
            log.warn("[LOGIN] Email not verified for email={}", u.getEmail());
            return new Response("Email not verified. Please verify OTP.", null, 401);
        }

        Response resp = userService.loginUser(u.getEmail(), u.getPassword());
        log.info("[LOGIN] Login result: status={}", resp.getStatus());

        return resp;
    }

    // ---------------------------------------------------------
    // FORGOT PASSWORD — SEND OTP
    // ---------------------------------------------------------
    @PostMapping("/forgot-password")
    public Response forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        log.info("[FORGOT_PWD] Forgot password requested for email={}", email);

        if (email == null || email.isBlank()) {
            return new Response("Email is required", null, 400);
        }

        Optional<users> optUser = userRepo.findByEmail(email);
        if (optUser.isEmpty()) {
            return new Response("Email not registered", null, 400);
        }

        String otp = OtpUtil.generateOtp();
        EmailOtp emailOtp = new EmailOtp(
                email,
                otp,
                LocalDateTime.now().plusMinutes(10)
        );
        otpRepo.save(emailOtp);
        log.info("[FORGOT_PWD] OTP stored for reset");

        try {
            emailService.sendOtp(email, otp);
            log.info("[FORGOT_PWD] OTP email sent");
        } catch (Exception e) {
            log.error("[FORGOT_PWD] Failed to send reset OTP: {}", e.getMessage());
        }

        return new Response("Reset OTP sent (or attempted)", null, 200);
    }

    // ---------------------------------------------------------
    // VERIFY RESET OTP
    // ---------------------------------------------------------
    @PostMapping("/verify-reset-otp")
    public Response verifyResetOtp(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp = body.get("otp");
        log.info("[VERIFY_RESET_OTP] Verifying reset OTP for email={}", email);

        if (email == null || otp == null) {
            return new Response("Email and OTP are required", null, 400);
        }

        Optional<EmailOtp> otpOpt = otpRepo.findByEmail(email);
        if (otpOpt.isEmpty()) {
            return new Response("OTP not found", null, 400);
        }

        EmailOtp emailOtp = otpOpt.get();

        if (!emailOtp.getOtp().equals(otp)) {
            return new Response("Invalid OTP", null, 400);
        }

        if (emailOtp.getExpiry().isBefore(LocalDateTime.now())) {
            return new Response("OTP expired", null, 400);
        }

        return new Response("OTP verified, you can reset password", null, 200);
    }

    // ---------------------------------------------------------
    // RESET PASSWORD
    // ---------------------------------------------------------
    @PostMapping("/reset-password")
    public Response resetPassword(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");

        log.info("[RESET_PWD] Reset password request for {}", email);

        if (email == null || otp == null || newPassword == null) {
            return new Response("Missing fields", null, 400);
        }

        Optional<EmailOtp> otpOpt = otpRepo.findByEmail(email);
        if (otpOpt.isEmpty()) {
            return new Response("OTP not found", null, 400);
        }

        EmailOtp emailOtp = otpOpt.get();

        if (!emailOtp.getOtp().equals(otp)) {
            return new Response("Invalid OTP", null, 400);
        }

        if (emailOtp.getExpiry().isBefore(LocalDateTime.now())) {
            return new Response("OTP expired", null, 400);
        }

        Optional<users> optUser = userRepo.findByEmail(email);
        if (optUser.isEmpty()) {
            return new Response("User not found", null, 400);
        }

        users user = optUser.get();
        user.setPassword(newPassword); // ⚠️ In real projects hash this
        userRepo.save(user);

        otpRepo.delete(emailOtp);

        log.info("[RESET_PWD] Password reset successful");
        return new Response("Password reset successfully", null, 200);
    }

    // ---------------------------------------------------------
    // ADD ADMIN
    // ---------------------------------------------------------
    @PostMapping("/add-admin")
    public Response addAdmin(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String password = body.get("password");

        log.info("[ADD_ADMIN] Request to add admin {}", email);

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return new Response("Email and password are required", null, 400);
        }

        if (userRepo.findByEmail(email).isPresent()) {
            return new Response("User already exists", null, 400);
        }

        users admin = new users(email, password, "ADMIN");
        admin.setEnabled(true);
        userRepo.save(admin);

        return new Response("Admin created successfully", null, 200);
    }
    
    

@GetMapping("/debug-smtp")
public Response debugSmtp() {
    String host = "smtp.gmail.com";
    int port = 587;
    int timeoutMs = 5000; // 5s - keeps request fast
    try (Socket socket = new Socket()) {
        long start = System.currentTimeMillis();
        socket.connect(new InetSocketAddress(host, port), timeoutMs);
        long took = System.currentTimeMillis() - start;
        return new Response("OK: Connected to " + host + ":" + port + " in " + took + "ms", null, 200);
    } catch (Exception e) {
        // return helpful debug text and log full stacktrace
        log.error("[DEBUG_SMTP] Connection test failed: {}", e.getMessage(), e);
        return new Response("ERROR: Could not connect to " + host + ":" + port + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage(), null, 500);
    }
}


//imports: java.net.InetSocketAddress; java.net.Socket;
@GetMapping("/debug-smtp-465")
public Response debugSmtp465() {
 String host = "smtp.gmail.com";
 int port = 465;
 int timeoutMs = 5000;
 try (Socket socket = new Socket()) {
     long start = System.currentTimeMillis();
     socket.connect(new InetSocketAddress(host, port), timeoutMs);
     long took = System.currentTimeMillis() - start;
     return new Response("OK: Connected to " + host + ":" + port + " in " + took + "ms", null, 200);
 } catch (Exception e) {
     log.error("[DEBUG_SMTP_465] Connection test failed: {}", e.getMessage(), e);
     return new Response("ERROR: Could not connect to " + host + ":" + port + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage(), null, 500);
 }
}
}
