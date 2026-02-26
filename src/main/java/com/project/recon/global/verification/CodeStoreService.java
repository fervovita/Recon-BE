package com.project.recon.global.verification;

import com.project.recon.global.verification.entity.VerificationCode;
import com.project.recon.global.verification.repository.VerificationCodeRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@Slf4j
public class CodeStoreService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final VerificationCodeRepository verificationCodeRepository;
    private final CircuitBreaker circuitBreaker;

    public CodeStoreService(
            RedisTemplate<String, Object> redisTemplate,
            VerificationCodeRepository verificationCodeRepository,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.redisTemplate = redisTemplate;
        this.verificationCodeRepository = verificationCodeRepository;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("redis");

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.info("[CircuitBreaker] 상태 변경: {}", event.getStateTransition()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(String key, String value, long ttlMinutes) {
        try {
            circuitBreaker.executeRunnable(() -> redisTemplate.opsForValue().set(key, value, ttlMinutes, TimeUnit.MINUTES));
        } catch (Exception e) {
            log.warn("[CodeStoreService] Redis 저장 실패, DB fallback: key={}", key, e);
            verificationCodeRepository.deleteByCodeKey(key);
            verificationCodeRepository.save(VerificationCode.createVerificationCode(key, value, ttlMinutes));
        }
    }

    public String get(String key) {
        try {
            String redisValue = circuitBreaker.executeSupplier(() -> {
                Object value = redisTemplate.opsForValue().get(key);
                return value != null ? value.toString() : null;
            });

            if (redisValue != null) {
                return redisValue;
            }
        } catch (Exception e) {
            log.warn("[CodeStoreService] Redis 조회 실패, DB fallback: key={}", key, e);
        }

        // Redis에 없거나 실패 시 항상 DB 확인
        return verificationCodeRepository.findByCodeKey(key)
                .filter(vc -> !vc.isExpired())
                .map(VerificationCode::getCodeValue)
                .orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(String key) {
        try {
            circuitBreaker.executeRunnable(() -> redisTemplate.delete(key));
        } catch (Exception e) {
            log.warn("[CodeStoreService] Redis 삭제 실패, DB fallback: key={}", key, e);
        }

        verificationCodeRepository.deleteByCodeKey(key);
    }

    public boolean hasKey(String key) {
        try {
            boolean exists = circuitBreaker.executeSupplier(() -> Boolean.TRUE.equals(redisTemplate.hasKey(key)));
            if (exists) {
                return true;
            }
        } catch (Exception e) {
            log.warn("[CodeStoreService] Redis 조회 실패, DB fallback: key={}", key, e);
        }

        // CircuitBreaker가 CLOSED 상태가 아닐 때 DB 확인
        if (circuitBreaker.getState() != CircuitBreaker.State.CLOSED) {
            return verificationCodeRepository.findByCodeKey(key)
                    .filter(vc -> !vc.isExpired())
                    .isPresent();
        }

        return false;
    }
}
