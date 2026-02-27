package com.project.recon.global.verification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_code")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codeKey;

    @Column(nullable = false)
    private String codeValue;

    @Column(nullable = false)
    private LocalDateTime expiresAt;


    public static VerificationCode createVerificationCode(String codeKey, String codeValue, long ttlMinutes) {
        VerificationCode vc = new VerificationCode();
        vc.codeKey = codeKey;
        vc.codeValue = codeValue;
        vc.expiresAt = LocalDateTime.now().plusMinutes(ttlMinutes);

        return vc;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
