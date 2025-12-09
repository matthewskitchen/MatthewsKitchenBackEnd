package controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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
    // TEST
    // ---------------------------------------------------------
    @GetMapping("/test_email")
    public String testEmail() {
        log.info("[TEST_EMAIL] Sending test OTP email");
        emailService.sendOtp("naveenkishore20022@gmail.com", "123456");
        return "Test Email Sent!";
    }

    // ---------------------------------------------------------
    // REGISTER — SEND OTP FOR REGISTRATION
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
        log.debug("[REGISTER] Generated OTP for email={} (not logging value)", u.getEmail());

        EmailOtp emailOtp = new EmailOtp(
                u.getEmail(),
                otp,
                LocalDateTime.now().plusMinutes(5)
        );
        otpRepo.save(emailOtp);
        log.info("[REGISTER] OTP stored for email={} with expiry={}", u.getEmail(), emailOtp.getExpiry());

        // Send OTP Email
        emailService.sendOtp(u.getEmail(), otp);
        log.info("[REGISTER] OTP email triggered for email={}", u.getEmail());

        return new Response("OTP sent to your email", null, 200);
    }

    // ---------------------------------------------------------
    // VERIFY OTP (REGISTRATION)
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
        log.debug("[VERIFY_OTP] OTP record deleted for email={}", email);

        return new Response("Email verified successfully!", null, 200);
    }

    // ---------------------------------------------------------
    // RESEND OTP (REGISTRATION)
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
        log.info("[RESEND_OTP] New OTP stored for email={} with expiry={}", email, emailOtp.getExpiry());

        emailService.sendOtp(email, otp);
        log.info("[RESEND_OTP] OTP email resent to email={}", email);

        return new Response("OTP resent successfully", null, 200);
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
        log.info("[LOGIN] Login result for email={}: status={}", u.getEmail(), resp.getStatus());

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
            log.warn("[FORGOT_PWD] Email missing in request");
            return new Response("Email is required", null, 400);
        }

        Optional<users> optUser = userRepo.findByEmail(email);
        if (optUser.isEmpty()) {
            log.warn("[FORGOT_PWD] Email not registered: {}", email);
            return new Response("Email not registered", null, 400);
        }

        String otp = OtpUtil.generateOtp();
        EmailOtp emailOtp = new EmailOtp(
                email,
                otp,
                LocalDateTime.now().plusMinutes(10)
        );
        otpRepo.save(emailOtp);
        log.info("[FORGOT_PWD] Reset OTP stored for email={} expiry={}", email, emailOtp.getExpiry());

        emailService.sendOtp(email, otp);
        log.info("[FORGOT_PWD] Reset OTP email sent to email={}", email);

        return new Response("Reset OTP sent to your email", null, 200);
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
            log.warn("[VERIFY_RESET_OTP] Missing email or OTP in request");
            return new Response("Email and OTP are required", null, 400);
        }

        Optional<EmailOtp> otpOpt = otpRepo.findByEmail(email);
        if (otpOpt.isEmpty()) {
            log.warn("[VERIFY_RESET_OTP] OTP not found for email={}", email);
            return new Response("OTP not found, please request again", null, 400);
        }

        EmailOtp emailOtp = otpOpt.get();

        if (!emailOtp.getOtp().equals(otp)) {
            log.warn("[VERIFY_RESET_OTP] Invalid OTP for email={}", email);
            return new Response("Invalid OTP", null, 400);
        }

        if (emailOtp.getExpiry().isBefore(LocalDateTime.now())) {
            log.warn("[VERIFY_RESET_OTP] OTP expired for email={}", email);
            return new Response("OTP expired, request a new one", null, 400);
        }

        log.info("[VERIFY_RESET_OTP] OTP verification successful for email={}", email);
        return new Response("OTP verified, you can reset password now", null, 200);
    }

    // ---------------------------------------------------------
    // RESET PASSWORD
    // ---------------------------------------------------------
    @PostMapping("/reset-password")
    public Response resetPassword(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");

        log.info("[RESET_PWD] Reset password request for email={}", email);

        if (email == null || otp == null || newPassword == null) {
            log.warn("[RESET_PWD] Missing fields in request for email={}", email);
            return new Response("Email, OTP and new password are required", null, 400);
        }

        Optional<EmailOtp> otpOpt = otpRepo.findByEmail(email);
        if (otpOpt.isEmpty()) {
            log.warn("[RESET_PWD] OTP not found for email={}", email);
            return new Response("OTP not found, please request again", null, 400);
        }

        EmailOtp emailOtp = otpOpt.get();

        if (!emailOtp.getOtp().equals(otp)) {
            log.warn("[RESET_PWD] Invalid OTP for email={}", email);
            return new Response("Invalid OTP", null, 400);
        }

        if (emailOtp.getExpiry().isBefore(LocalDateTime.now())) {
            log.warn("[RESET_PWD] OTP expired for email={}", email);
            return new Response("OTP expired, please request new OTP", null, 400);
        }

        Optional<users> optUser = userRepo.findByEmail(email);
        if (optUser.isEmpty()) {
            log.error("[RESET_PWD] User not found when resetting password, email={}", email);
            return new Response("User not found", null, 400);
        }

        users user = optUser.get();
        user.setPassword(newPassword);  // ❗ In real system, hash password
        userRepo.save(user);
        log.info("[RESET_PWD] Password reset successful for email={}", email);

        otpRepo.delete(emailOtp);
        log.debug("[RESET_PWD] OTP deleted after reset for email={}", email);

        return new Response("Password reset successfully", null, 200);
    }

    // ---------------------------------------------------------
    // ADD ADMIN (from existing admin)
    // ---------------------------------------------------------
    @PostMapping("/add-admin")
    public Response addAdmin(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String password = body.get("password");

        log.info("[ADD_ADMIN] Request to add admin with email={}", email);

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.warn("[ADD_ADMIN] Email or password missing");
            return new Response("Email and password are required", null, 400);
        }

        Optional<users> existing = userRepo.findByEmail(email);
        if (existing.isPresent()) {
            log.warn("[ADD_ADMIN] User with this email already exists: {}", email);
            return new Response("User with this email already exists", null, 400);
        }

        users admin = new users(email, password, "ADMIN");
        admin.setEnabled(true); // admin is active immediately
        userRepo.save(admin);

        log.info("[ADD_ADMIN] Admin created successfully for email={}", email);
        return new Response("Admin created successfully", null, 200);
    }

}
