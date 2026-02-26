package com.project.recon.global.verification.repository;

import com.project.recon.global.verification.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByCodeKey(String code);

    @Modifying
    @Query("DELETE FROM VerificationCode vc WHERE vc.codeKey = :codeKey")
    void deleteByCodeKey(String codeKey);

    @Modifying
    @Query("DELETE FROM VerificationCode vc WHERE vc.expiresAt < :dateTime")
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
