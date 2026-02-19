package com.project.recon.global.email;

import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String CODE_PREFIX = "EMAIL_CODE:";
    private static final String VERIFIED_PREFIX = "EMAIL_VERIFIED:";
    private static final long CODE_EXPIRATION = 5;          // 인증 코드 유효시간 5분
    private static final long VERIFIED_EXPIRATION = 30;     // 인증 완료 유효시간 30분

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SpringTemplateEngine templateEngine;

    public void sendVerificationCode(String email) {
        String code = generateCode();

        // Redis에 인증 코드 저장 (TTL: 5분)
        redisTemplate.opsForValue().set(
                CODE_PREFIX + email,
                code,
                CODE_EXPIRATION,
                TimeUnit.MINUTES
        );

        // 이메일 발송
        sendEmail(email, code);
    }

    public void verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        Object savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new GeneralException(GeneralErrorCode.EMAIL_CODE_EXPIRED);
        }

        if (!savedCode.toString().equals(code)) {
            throw new GeneralException(GeneralErrorCode.EMAIL_CODE_INVALID);
        }

        redisTemplate.delete(key);

        // Redis에 인증된 이메일 저장 (TTL: 30분)
        redisTemplate.opsForValue().set(
                VERIFIED_PREFIX + email,
                "true",
                VERIFIED_EXPIRATION,
                TimeUnit.MINUTES
        );
    }

    public boolean isVerified(String email) {
        Object verified = redisTemplate.opsForValue().get(VERIFIED_PREFIX + email);
        return "true".equals(verified);
    }

    public void deleteVerified(String email) {
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }


    private String generateCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private void sendEmail(String email, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[Recon] 이메일 인증 코드");
            helper.setText(buildHtml(code), true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
            throw new GeneralException(GeneralErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String buildHtml(String code) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process("email/verificationCode", context);
    }
}
