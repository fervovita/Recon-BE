package com.project.recon.domain.user.service;

import com.project.recon.domain.user.dto.UserRequestDTO;
import com.project.recon.domain.user.dto.UserResponseDTO;
import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SmsService smsService;

    @Override
    public UserResponseDTO.UserProfileResponseDTO getUserProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));


        return UserResponseDTO.UserProfileResponseDTO.builder()
                .id(user.getId())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .phoneNumberVerified(user.isPhoneNumberVerified())
                .birthDate(user.getBirthDate())
                .provider(user.getProvider())
                .build();
    }

    @Override
    public void sendSmsCode(Long userId) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));

        // SMS 인증 여부 확인
        if (user.isPhoneNumberVerified()) {
            throw new GeneralException(GeneralErrorCode.SMS_ALREADY_VERIFIED);
        }

        // 인증 코드 전송
        smsService.sendVerificationCode(user.getPhoneNumber());
    }

    @Override
    @Transactional
    public void verifySmsCode(Long userId, UserRequestDTO.SmsVerifyRequestDTO request) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));

        // SMS 인증 여부 확인
        if (user.isPhoneNumberVerified()) {
            throw new GeneralException(GeneralErrorCode.SMS_ALREADY_VERIFIED);
        }

        // 인증 코드 검증
        smsService.verifyCode(user.getPhoneNumber(), request.getCode());

        // 유저 필드값 변경
        user.verifyPhoneNumber();

        // 인증 코드 삭제
        try {
            smsService.deleteVerified(user.getPhoneNumber());
        } catch (Exception e) {
            log.warn("SMS 인증 정보 삭제 실패: {}", e.getMessage());
        }
    }
}
