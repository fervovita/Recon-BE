package com.project.recon.global.sms;

import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.encryption.AesEncryptor;
import com.project.recon.global.verification.CodeStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private static final String CODE_PREFIX = "SMS_CODE:";
    private static final String VERIFIED_PREFIX = "SMS_VERIFIED:";
    private static final String RESEND_PREFIX = "SMS_RESEND:";
    private static final long CODE_EXPIRATION = 3;          // 인증 코드 유효시간 3분
    private static final long VERIFIED_EXPIRATION = 30;     // 인증 완료 유효시간 30분
    private static final long RESEND_LIMIT = 1;             // 코드 재발송 제한시간 1분

    private final CodeStoreService codeStoreService;
    private final SmsSender smsSender;
    private final AesEncryptor aesEncryptor;

    public void sendVerificationCode(String phoneNumber) {
        String hashedPhone = aesEncryptor.hash(phoneNumber);
        String resendKey = RESEND_PREFIX + hashedPhone;

        if (codeStoreService.hasKey(resendKey)) {
            throw new GeneralException(GeneralErrorCode.SMS_CODE_ALREADY_SENT);
        }

        String code = generateCode();

        // 인증 코드 저장 (TTL: 3분)
        codeStoreService.save(CODE_PREFIX + hashedPhone, code, CODE_EXPIRATION);

        // 코드 재발송 제한 저장 (TTL: 1분)
        codeStoreService.save(resendKey, "true", RESEND_LIMIT);

        // SMS 발송(비동기)
        smsSender.sendVerificationSms(phoneNumber, code);
    }

    public void verifyCode(String phoneNumber, String code) {
        String hashedPhone = aesEncryptor.hash(phoneNumber);
        String key = CODE_PREFIX + hashedPhone;
        String savedCode = codeStoreService.get(key);

        if (savedCode == null) {
            throw new GeneralException(GeneralErrorCode.SMS_CODE_EXPIRED);
        }

        if (!savedCode.equals(code)) {
            throw new GeneralException(GeneralErrorCode.SMS_CODE_INVALID);
        }

        codeStoreService.delete(key);

        //  인증된 전화번호 저장 (TTL: 30분)
        codeStoreService.save(VERIFIED_PREFIX + hashedPhone, "true", VERIFIED_EXPIRATION);
    }

    public boolean isVerified(String phoneNumber) {
        String verified = codeStoreService.get(VERIFIED_PREFIX + aesEncryptor.hash(phoneNumber));
        return "true".equals(verified);
    }

    public void deleteVerified(String phoneNumber) {
        codeStoreService.delete(VERIFIED_PREFIX + aesEncryptor.hash(phoneNumber));
    }


    private String generateCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
