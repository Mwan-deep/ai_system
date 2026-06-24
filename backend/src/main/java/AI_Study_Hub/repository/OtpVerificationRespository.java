package AI_Study_Hub.repository;

import AI_Study_Hub.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRespository extends JpaRepository<OtpVerification, String> {
    Optional<OtpVerification> findByGmailAndOtp(String gmail,  String otp);
}
