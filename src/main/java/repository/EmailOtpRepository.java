package repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dbmodel.EmailOtp;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    // Find OTP by email
    Optional<EmailOtp> findByEmail(String email);
}
