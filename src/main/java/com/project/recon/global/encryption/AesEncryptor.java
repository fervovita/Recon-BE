package com.project.recon.global.encryption;

import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
@Slf4j
public class AesEncryptor {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    @Value("${encrypt.secret-key}")
    private String secretKey;

    private SecretKeySpec secretKeySpec;
    private IvParameterSpec ivSpec;

    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(secretKey.getBytes(StandardCharsets.UTF_8));
            this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            this.ivSpec = new IvParameterSpec(Arrays.copyOfRange(keyBytes, 0, 16));
        } catch (Exception e) {
            throw new RuntimeException("AES 암호화 초기화 실패", e);
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return null;

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new GeneralException(GeneralErrorCode.ENCRYPTION_FAILED);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) return null;

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            byte[] decrypted = Base64.getDecoder().decode(encryptedText);
            return new String(cipher.doFinal(decrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new GeneralException(GeneralErrorCode.DECRYPTION_FAILED);
        }
    }


}
