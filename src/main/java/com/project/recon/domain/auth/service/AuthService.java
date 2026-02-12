package com.project.recon.domain.auth.service;

import com.project.recon.domain.auth.dto.AuthRequestDTO;
import com.project.recon.domain.auth.dto.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO.LoginResponseDTO kakaoLogin(AuthRequestDTO.KakaoLoginRequestDTO request);

    AuthResponseDTO.LoginResponseDTO emailLogin(AuthRequestDTO.EmailLoginRequestDTO request);

    AuthResponseDTO.ReissueTokenResponseDTO reissueToken(AuthRequestDTO.ReissueTokenRequestDTO request);

    void logout(AuthRequestDTO.LogoutRequestDTO request);

    AuthResponseDTO.SignupResponseDTO signup(AuthRequestDTO.SignupRequestDTO request);
}
