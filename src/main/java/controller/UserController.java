//package controller;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import dbmodel.EmailOtp;
//import dbmodel.users;
//import services.Response;
//import services.UserService;
//
//@RestController
//@RequestMapping("/users")
//@CrossOrigin(origins = "*")
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/register")   // ✅ This accepts only POST
//    public Response registerUser(@RequestBody users u) {
//    	System.out.println(u.getEmail()+""+u.getPassword());
//    	return userService.registerUser(u);
//    }
//
//    @PostMapping("/login")      // ✅ POST only
//    public Response loginUser(@RequestBody users u) {
//        return userService.loginUser(u.getEmail(), u.getPassword());
//    }
//   
//}



package controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailOtpRepository otpRepo;

    @Autowired
    private EmailService emailService;

    @GetMapping("/test_email")
    public String testEmail() {
        emailService.sendOtp("naveenkishore20022@gmail.com", "123456");
        return "Test Email Sent!";
    }


    // ---------------------------------------------------------
    // REGISTER — SEND OTP
    // ---------------------------------------------------------
    @PostMapping("/register")
    public Response registerUser(@RequestBody users u) {

        if (userRepo.findByEmail(u.getEmail()).isPresent()) {
            return new Response("Email already exists", null, 400);
        }

        u.setEnabled(false);
        userRepo.save(u);

        // Generate OTP
        String otp = OtpUtil.generateOtp();

        EmailOtp emailOtp = new EmailOtp(
                u.getEmail(),
                otp,
                LocalDateTime.now().plusMinutes(5)
        );
        otpRepo.save(emailOtp);

        // Send OTP Email
        emailService.sendOtp(u.getEmail(), otp);

        return new Response("OTP sent to your email", null, 200);
    }


    // ---------------------------------------------------------
    // VERIFY OTP
    // ---------------------------------------------------------
    @PostMapping("/verify-otp")
    public Response verifyOtp(@RequestBody Map<String, String> data) {

        String email = data.get("email");
        String otp = data.get("otp");

        Optional<EmailOtp> otpObj = otpRepo.findByEmail(email);

        if (otpObj.isEmpty()) {
            return new Response("OTP not found, register again", null, 400);
        }

        EmailOtp emailOtp = otpObj.get();

        if (!emailOtp.getOtp().equals(otp)) {
            return new Response("Invalid OTP", null, 400);
        }

        if (emailOtp.getExpiry().isBefore(LocalDateTime.now())) {
            return new Response("OTP expired", null, 400);
        }

        users user = userRepo.findByEmail(email).get();
        user.setEnabled(true);
        userRepo.save(user);

        otpRepo.delete(emailOtp);

        return new Response("Email verified successfully!", null, 200);
    }


    // ---------------------------------------------------------
    // RESEND OTP
    // ---------------------------------------------------------
    @PostMapping("/resend-otp")
    public Response resendOtp(@RequestBody Map<String, String> data) {

        String email = data.get("email");

        Optional<users> u = userRepo.findByEmail(email);

        if (u.isEmpty()) {
            return new Response("Email not registered", null, 400);
        }

        if (u.get().isEnabled()) {
            return new Response("Email already verified", null, 200);
        }

        String otp = OtpUtil.generateOtp();
        EmailOtp emailOtp = new EmailOtp(email, otp, LocalDateTime.now().plusMinutes(5));
        otpRepo.save(emailOtp);

        emailService.sendOtp(email, otp);

        return new Response("OTP resent successfully", null, 200);
    }


    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------
    @PostMapping("/login")
    public Response loginUser(@RequestBody users u) {

        Optional<users> opt = userRepo.findByEmail(u.getEmail());

        if (opt.isEmpty()) {
            return new Response("Invalid Email", null, 400);
        }

        users user = opt.get();

        if (!user.isEnabled()) {
            return new Response("Email not verified. Please verify OTP.", null, 401);
        }

        return userService.loginUser(u.getEmail(), u.getPassword());
    }
}

