package com.project.recon.domain.user.service;

import com.project.recon.domain.user.dto.UserResponseDTO;
import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
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

    @Override
    public UserResponseDTO.UserProfileResponseDTO getUserProfile(Long userId) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));


        return UserResponseDTO.UserProfileResponseDTO.builder()
                .id(user.getId())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .birthDate(user.getBirthDate())
                .provider(user.getProvider())
                .build();
    }
}
