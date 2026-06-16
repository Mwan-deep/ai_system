package AI_Study_Hub.service;

import AI_Study_Hub.dto.request.AuthenticateRequest;
import AI_Study_Hub.dto.request.IntroSpecRequest;
import AI_Study_Hub.dto.request.LogoutRequest;
import AI_Study_Hub.dto.request.OtpNewDeviceRequest;
import AI_Study_Hub.dto.response.AuthenticateResponse;
import AI_Study_Hub.dto.response.IntroSpecResponse;
import AI_Study_Hub.entity.Device;
import AI_Study_Hub.entity.OtpVerification;
import AI_Study_Hub.exception.AppException;
import AI_Study_Hub.exception.ErrorCode;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.InvalidatedtokenSession;
import AI_Study_Hub.repository.AccountRespository;
import AI_Study_Hub.repository.DeviceRepository;
import AI_Study_Hub.repository.InvalidationRespository;
import AI_Study_Hub.repository.OtpVerificationRespository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticateService {
    @Value("${jwt.signerToken}")
    @NonFinal
    protected String signerToken;
    AccountRespository accountRespository;
    InvalidationRespository invalidationRespository;
    DeviceRepository deviceRepository;
    OtpVerificationRespository otpVerificationRespository;
    Map<String, Integer> otpAttempts = new HashMap<>();
    OtpService otpService;
    EmailService emailService;


    public AuthenticateResponse authenticate (AuthenticateRequest request){
        var account = accountRespository.findAccountByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXITS));
        
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPasswordHash(), account.getPasswordHash());
        if(!authenticated){
            throw new AppException(ErrorCode.PASSWORD_INCORRECTLY);
        }


        //------Verify Device------

        Optional<Device> deviceOptional = deviceRepository.findByDeviceId(request.getDeviceId());
        if(deviceOptional.isPresent() && deviceOptional.get().isTrusted()){
            var token = generateToken(account);
            return AuthenticateResponse.builder()
                    .token(token)
                    .authenticated(authenticated)
                    .build();
        }else{
            String optVf = otpService.generarteOtp();
            OtpVerification otpVerification = new OtpVerification();
            otpVerification.setGmail(request.getEmail());
            otpVerification.setOtp(optVf);
            otpVerification.setExpireTime(LocalDateTime.now().plus(1, ChronoUnit.MINUTES));

            otpVerificationRespository.save(otpVerification);

            emailService.sendGmail(
                    account.getEmail(),
                    " AI STUDY HUB - OTP Verification",
                    "=====================================\n" +
                            "        AI STUDY HUB SECURITY\n" +
                            "=====================================\n\n" +
                            "Hello,\n\n" +
                            "You are trying to sign in to AI STUDY HUB.\n\n" +
                            "Your One-Time Password (OTP) is:\n\n" +
                            "        " + optVf + "\n\n" +
                            "This code will expire in 1 minutes.\n\n" +
                            "If you did NOT request this login, please ignore this email.\n\n" +
                            "-------------------------------------\n" +
                            "AI STUDY HUB Team\n" +
                            "Secure Learning Platform\n" +
                            "-------------------------------------");

            return AuthenticateResponse.builder()
                    .authenticated(false)
                    .build();
        }

    }
    public AuthenticateResponse verifyOtpNewDevice(OtpNewDeviceRequest request){

        OtpVerification otpVerification =
                otpVerificationRespository
                        .findByGmailAndOtp(request.getEmail(), request.getOtp())
                        .orElse(null);

        //OTP không đúng (không match)
        if (!otpVerification.getOtp().equals(request.getOtp())) {

            // ===== FAIL COUNT LOGIC =====
            int count = otpAttempts.getOrDefault(request.getEmail(), 0);
            count++;

            if (count >= 5) {
                otpAttempts.remove(request.getEmail()); // reset counter
                otpVerificationRespository.delete(otpVerification); // xoá OTP luôn
                throw new AppException(ErrorCode.TOO_MANY_REQUEST);
            }

            otpAttempts.put(request.getEmail(), count);

            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // ✔ check expire
        if (LocalDateTime.now().isAfter(otpVerification.getExpireTime())) {
            otpVerificationRespository.delete(otpVerification);
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // ✔ SUCCESS → delete OTP
        otpVerificationRespository.delete(otpVerification);

        var account = accountRespository.findAccountByEmail(otpVerification.getGmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXITS));

        Device device = new Device();
        device.setDeviceId(request.getDeviceId());
        device.setTrusted(true);
        device.setAccount(account);
        device.setLastLogin(LocalDateTime.now());

        deviceRepository.save(device);

        String token = generateToken(account);

        return AuthenticateResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public String generateToken(Account account){
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getUserName())
                .issuer("devteria")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope" , buildScope(account))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(signerToken.getBytes()));
            return jwsObject.serialize();
        }catch (JOSEException e){
            log.info("CAN NOT GET TOKEN");
            throw new AppException(ErrorCode.TOKEN_INVALID);
        }
    }

    public String buildScope(Account account){
        StringJoiner stringJoiner = new StringJoiner(" ");

        if(!CollectionUtils.isEmpty(account.getRoles())){
            account.getRoles().forEach(role -> stringJoiner.add("ROLE_"+role.getRoleId()));
        }
        return stringJoiner.toString();
    }

    public IntroSpecResponse introSpec (IntroSpecRequest request)
            throws JOSEException, ParseException{
        boolean isValid = true;

        try {
            verifyToken(request.getToken());
        }catch (AppException e) {
            isValid = false;
        }
        return IntroSpecResponse.builder()
                .authenticated(isValid)
                .build();
    }

    public SignedJWT verifyToken(String token)
            throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(signerToken.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expireTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        String jit = signedJWT.getJWTClaimsSet().getJWTID();

        boolean invalidatedToken = invalidationRespository.existsById(jit);

        if(!(verified && expireTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidatedToken){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;

    }

    public void logout(LogoutRequest request)
            throws ParseException, JOSEException {
        try{
            var signedJWT = verifyToken(request.getToken());

            LocalDateTime expireTime =  signedJWT.getJWTClaimsSet()
                    .getExpirationTime()
                    .toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            String jit = signedJWT.getJWTClaimsSet().getJWTID();

            InvalidatedtokenSession invalidatedtokenSession = InvalidatedtokenSession.builder()
                    .invalidId(jit)
                    .expireTime(expireTime)
                    .build();

            invalidationRespository.save(invalidatedtokenSession);
        }catch (AppException e){
            log.info("Token already expired");
        }


    }
}
