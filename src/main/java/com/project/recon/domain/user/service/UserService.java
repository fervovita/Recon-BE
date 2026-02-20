package com.project.recon.domain.user.service;

import com.project.recon.domain.user.dto.UserRequestDTO;
import com.project.recon.domain.user.dto.UserResponseDTO;

public interface UserService {

    UserResponseDTO.UserProfileResponseDTO getUserProfile(Long userId);

    void sendSmsCode(Long userId);

    void verifySmsCode(Long userId, UserRequestDTO.SmsVerifyRequestDTO request);
}
