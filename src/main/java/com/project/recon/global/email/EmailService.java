package com.project.recon.global.email;

import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.verification.CodeStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String CODE_PREFIX = "EMAIL_CODE:";
    private static final String VERIFIED_PREFIX = "EMAIL_VERIFIED:";
    private static final String RESEND_PREFIX = "EMAIL_RESEND:";
    private static final long CODE_EXPIRATION = 5;          // 인증 코드 유효시간 5분
    private static final long VERIFIED_EXPIRATION = 30;     // 인증 완료 유효시간 30분
    private static final long RESEND_LIMIT = 1;             // 코드 재발송 제한시간 1분


    private final CodeStoreService codeStoreService;
    private final EmailSender emailSender;

    public void sendVerificationCode(String email) {
        String resendKey = RESEND_PREFIX + email;

        if (codeStoreService.hasKey(resendKey)) {
            throw new GeneralException(GeneralErrorCode.EMAIL_CODE_ALREADY_SENT);
        }

        String code = generateCode();

        // 인증 코드 저장 (TTL: 5분)
        codeStoreService.save(CODE_PREFIX + email, code, CODE_EXPIRATION);

        // 코드 재발송 제한 저장 (TTL: 1분)
        codeStoreService.save(resendKey, "true", RESEND_LIMIT);

        // 이메일 발송(비동기)
        emailSender.sendVerificationEmail(email, code);
    }

    public void verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String savedCode = codeStoreService.get(key);

        if (savedCode == null) {
            throw new GeneralException(GeneralErrorCode.EMAIL_CODE_EXPIRED);
        }

        if (!savedCode.equals(code)) {
            throw new GeneralException(GeneralErrorCode.EMAIL_CODE_INVALID);
        }

        codeStoreService.delete(key);

        // 인증된 이메일 저장 (TTL: 30분)
        codeStoreService.save(VERIFIED_PREFIX + email, "true", VERIFIED_EXPIRATION);
    }

    public boolean isVerified(String email) {
        String verified = codeStoreService.get(VERIFIED_PREFIX + email);
        return "true".equals(verified);
    }

    public void deleteVerified(String email) {
        codeStoreService.delete(VERIFIED_PREFIX + email);
    }


    private String generateCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
