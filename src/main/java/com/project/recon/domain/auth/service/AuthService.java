package com.project.recon.domain.auth.service;

import com.project.recon.domain.auth.dto.AuthRequestDTO;
import com.project.recon.domain.auth.dto.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO.TokenResponseDTO kakaoLogin(AuthRequestDTO.KakaoLoginRequestDTO request);

    AuthResponseDTO.TokenResponseDTO emailLogin(AuthRequestDTO.EmailLoginRequestDTO request);

    AuthResponseDTO.TokenResponseDTO reissueToken(String refreshToken);

    void logout(String refreshToken);

    AuthResponseDTO.TokenResponseDTO signup(AuthRequestDTO.SignupRequestDTO request);

    void sendEmailCode(AuthRequestDTO.EmailSendRequestDTO request);

    void verifyEmailCode(AuthRequestDTO.EmailVerifyRequestDTO request);
}
