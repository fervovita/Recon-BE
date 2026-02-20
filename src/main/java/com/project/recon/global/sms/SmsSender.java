package com.project.recon.global.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsSender {

    @Value("${coolsms.api-key}")
    private String apiKey;

    @Value("${coolsms.api-secret}")
    private String apiSecret;

    @Value("${coolsms.sender-number}")
    private String senderNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
    }

    @Async
    public void sendVerificationSms(String phoneNumber, String code) {
        try {
            Message message = new Message();

            message.setFrom(senderNumber);
            message.setTo(phoneNumber);
            message.setText("[Recon] 인증번호 [" + code + "]를 입력해주세요.");

            messageService.send(message);
        } catch (Exception e) {
            log.error("SMS 발송 실패: {}", e.getMessage());
        }
    }

}
