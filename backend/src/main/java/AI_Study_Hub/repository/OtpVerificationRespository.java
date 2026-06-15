package AI_Study_Hub.repository;

import AI_Study_Hub.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpVerificationRespository extends JpaRepository<OtpVerification, String> {
}
