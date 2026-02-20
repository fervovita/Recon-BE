package com.project.recon.global.sms;

import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

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

    private final RedisTemplate<String, Object> redisTemplate;
    private final SmsSender smsSender;

    public void sendVerificationCode(String phoneNumber) {
        String resendKey = RESEND_PREFIX + phoneNumber;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(resendKey))) {
            throw new GeneralException(GeneralErrorCode.SMS_CODE_ALREADY_SENT);
        }

        String code = generateCode();

        // Redis에 인증 코드 저장 (TTL: 3분)
        redisTemplate.opsForValue().set(
                CODE_PREFIX + phoneNumber,
                code,
                CODE_EXPIRATION,
                TimeUnit.MINUTES
        );

        // Redis에 코드 재발송 제한 저장 (TTL: 1분)
        redisTemplate.opsForValue().set(
                resendKey,
                "true",
                RESEND_LIMIT,
                TimeUnit.MINUTES
        );

        // SMS 발송(비동기)
        smsSender.sendVerificationSms(phoneNumber, code);
    }

    public void verifyCode(String phoneNumber, String code) {
        String key = CODE_PREFIX + phoneNumber;
        Object savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new GeneralException(GeneralErrorCode.SMS_CODE_EXPIRED);
        }

        if (!savedCode.toString().equals(code)) {
            throw new GeneralException(GeneralErrorCode.SMS_CODE_INVALID);
        }

        redisTemplate.delete(key);

        // Redis에 인증된 전화번호 저장 (TTL: 30분)
        redisTemplate.opsForValue().set(
                VERIFIED_PREFIX + phoneNumber,
                "true",
                VERIFIED_EXPIRATION,
                TimeUnit.MINUTES
        );
    }

    public boolean isVerified(String phoneNumber) {
        Object verified = redisTemplate.opsForValue().get(VERIFIED_PREFIX + phoneNumber);
        return "true".equals(verified);
    }

    public void deleteVerified(String phoneNumber) {
        redisTemplate.delete(VERIFIED_PREFIX + phoneNumber);
    }


    private String generateCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
