package com.project.recon.global.scheduler;

import com.project.recon.global.verification.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeCleanupScheduler {

    private final VerificationCodeRepository verificationCodeRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanup() {
        log.info("[Scheduler] 만료된 인증 코드 정리 시작");

        verificationCodeRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        log.info("[Scheduler] 만료된 인증 코드 정리 완료");
    }
}
