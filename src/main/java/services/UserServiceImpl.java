package services;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import dbmodel.EmailOtp;
import dbmodel.users;
import dto.OtpUtil;
import repository.EmailOtpRepository;
import repository.UserRepo;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private EmailOtpRepository otpRepo;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;



    // ---------------------------------------------------------
    // REGISTER USER → SEND OTP
    // ---------------------------------------------------------
    @Override
    public Response registerUser(users user) {

        if (repo.findByEmail(user.getEmail()).isPresent()) {
            return new Response("Email already exists!", null, 400);
        }

        //Encryption
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set user as NOT verified
        user.setEnabled(false);
        repo.save(user);

        // Generate OTP
        String otp = OtpUtil.generateOtp();

        EmailOtp emailOtp = new EmailOtp(
                user.getEmail(),
                otp,
                LocalDateTime.now().plusMinutes(5)  // OTP valid for 5 mins
        );
        otpRepo.save(emailOtp);

        // Send OTP to email
        emailService.sendOtp(user.getEmail(), otp);

        return new Response("OTP sent to your Email", null, 200);
    }


    // ---------------------------------------------------------
    // LOGIN USER
    // ---------------------------------------------------------
    @Override
    public Response loginUser(String email, String password) {

        Optional<users> uopt = repo.findByEmail(email);

        if (uopt.isEmpty()) {
            return new Response("Invalid Email", null, 400);
        }

        users user = uopt.get();

        // BLOCK LOGIN IF EMAIL NOT VERIFIED
        if (!user.isEnabled()) {
            return new Response("Email not verified. Please verify OTP.", null, 401);
        }

        // PASSWORD CHECK
        // Check BCrypt-encrypted password
        if (passwordEncoder.matches(password, user.getPassword())) {
            // SUCCESS LOGIN
            String name = email.substring(0, email.indexOf('@'));
            return new Response("Login Successful Mr. " + name, user.getRole(), 200);
        }
        // Check old plain password (fallback)
        else if (user.getPassword().equals(password)) {
            // First login → update password to BCrypt
            user.setPassword(passwordEncoder.encode(password));
            repo.save(user);
            // SUCCESS LOGIN
            String name = email.substring(0, email.indexOf('@'));
            return new Response("Login Successful Mr. " + name, user.getRole(), 200);
        }
        else {
            return new Response("Incorrect Password", null, 400);
        }



    }
    
    // ---------------------------------------------------------
    // OTP VERIFICATION
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
            return new Response("OTP expired, request new OTP", null, 400);
        }

        users user = repo.findByEmail(email).get();
        user.setEnabled(true);
        repo.save(user);

        otpRepo.delete(emailOtp); // delete OTP after success

        return new Response("Email verified successfully!", null, 200);
    }

}
