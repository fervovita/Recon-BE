package com.project.recon.global.encryption;

import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class AesEncryptor {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    @Value("${encrypt.secret-key}")
    private String secretKey;

    @Value("${encrypt.hash-key}")
    private String hashKey;

    private SecretKeySpec secretKeySpec;
    private SecretKeySpec hmacKeySpec;

    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(secretKey.getBytes(StandardCharsets.UTF_8));
            this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] hmacBytes = MessageDigest.getInstance("SHA-256")
                    .digest(hashKey.getBytes(StandardCharsets.UTF_8));
            this.hmacKeySpec = new SecretKeySpec(hmacBytes, "HmacSHA256");
        } catch (Exception e) {
            throw new RuntimeException("AES 암호화 초기화 실패", e);
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return null;

        try {
            // 무작위 IV 생성
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Cipher 초기화 및 암호화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + 암호문 결합
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            // Base64 인코딩
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("데이터 암호화 실패: {}", e.getMessage());
            throw new GeneralException(GeneralErrorCode.ENCRYPTION_FAILED);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) return null;

        try {
            // Base64 디코딩
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // IV 추출
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 암호문 추출
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

            // Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

            // 복호화
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("데이터 복호화 실패: {}", e.getMessage());
            throw new GeneralException(GeneralErrorCode.DECRYPTION_FAILED);
        }
    }

    public String hash(String plainText) {
        if (plainText == null || plainText.isEmpty()) return null;

        try {
            // 초기화 및 해싱
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKeySpec);
            byte[] hashBytes = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 해싱값을 16진수 문자열로 변환
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("데이터 해싱 실패: {}", e.getMessage());
            throw new GeneralException(GeneralErrorCode.HASHING_FAILED);
        }
    }
}
