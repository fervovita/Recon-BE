package com.project.recon.domain.user.service;

import com.project.recon.domain.user.dto.UserRequestDTO;
import com.project.recon.domain.user.dto.UserResponseDTO;
import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.email.EmailService;
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
    private final EmailService emailService;

    @Override
    public UserResponseDTO.UserProfileResponseDTO getUserProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));


        return toUserProfileResponse(user);
    }

    @Override
    @Transactional
    public UserResponseDTO.UserProfileResponseDTO updateNickName(Long userId, UserRequestDTO.UpdateNickNameRequestDTO request) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));

        // 닉네임 수정
        user.updateNickName(request.getNickName());

        return toUserProfileResponse(user);
    }

    @Override
    public void sendEmailCode(Long userId, UserRequestDTO.EmailSendRequestDTO request) {

        // 유저 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND);
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new GeneralException(GeneralErrorCode.DUPLICATE_EMAIL);
        }

        // 인증 코드 발송
        emailService.sendVerificationCode(request.getEmail());
    }

    @Override
    @Transactional
    public UserResponseDTO.UserProfileResponseDTO updateEmail(Long userId, UserRequestDTO.UpdateEmailRequestDTO request) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new GeneralException(GeneralErrorCode.DUPLICATE_EMAIL);
        }

        // 인증 코드 검증
        emailService.verifyCode(request.getEmail(), request.getCode());

        // 이메일 변경
        user.updateEmail(request.getEmail());

        // 인증 정보 삭제
        try {
            emailService.deleteVerified(request.getEmail());
        } catch (Exception e) {
            log.warn("이메일 인증 정보 삭제 실패: {}", e.getMessage());
        }

        return toUserProfileResponse(user);

    }

    @Override
    public void sendPhoneCode(Long userId, UserRequestDTO.PhoneSendRequestDTO request) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));

        // 같은 번호인데 이미 인증된 경우
        if (request.getPhoneNumber().equals(user.getPhoneNumber())
                && user.isPhoneNumberVerified()) {
            throw new GeneralException(GeneralErrorCode.SMS_ALREADY_VERIFIED);
        }

        // 다른 번호로 변경하는 경우에만 중복 확인
        if (!request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new GeneralException(GeneralErrorCode.DUPLICATE_PHONE);
        }

        // 인증 코드 발송
        smsService.sendVerificationCode(request.getPhoneNumber());
    }

    @Override
    @Transactional
    public UserResponseDTO.UserProfileResponseDTO updatePhoneNumber(Long userId, UserRequestDTO.UpdatePhoneRequestDTO request) {

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));


        // 같은 번호인데 이미 인증된 경우
        if (request.getPhoneNumber().equals(user.getPhoneNumber())
                && user.isPhoneNumberVerified()) {
            throw new GeneralException(GeneralErrorCode.SMS_ALREADY_VERIFIED);
        }

        // 다른 번호로 변경하는 경우에만 중복 확인
        if (!request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new GeneralException(GeneralErrorCode.DUPLICATE_PHONE);
        }

        // 인증 코드 검증
        smsService.verifyCode(request.getPhoneNumber(), request.getCode());

        // 전화번호 변경 & verified
        user.updatePhoneNumber(request.getPhoneNumber());

        // 인증 코드 삭제
        try {
            smsService.deleteVerified(request.getPhoneNumber());
        } catch (Exception e) {
            log.warn("SMS 인증 정보 삭제 실패: {}", e.getMessage());
        }

        return toUserProfileResponse(user);

    }


    private UserResponseDTO.UserProfileResponseDTO toUserProfileResponse(User user) {
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
}
