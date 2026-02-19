package com.project.recon.domain.auth.controller;

import com.project.recon.domain.auth.dto.AuthRequestDTO;
import com.project.recon.domain.auth.dto.AuthResponseDTO;
import com.project.recon.domain.auth.service.AuthService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 로그인")
    @PostMapping("/kakao/login")
    public ApiResponse<AuthResponseDTO.LoginResponseDTO> kakaoLogin(@Valid @RequestBody AuthRequestDTO.KakaoLoginRequestDTO request) {
        AuthResponseDTO.LoginResponseDTO response = authService.kakaoLogin(request);
        return ApiResponse.onSuccess("로그인 성공", response);
    }

    @Operation(summary = "일반 로그인")
    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO.LoginResponseDTO> emailLogin(@Valid @RequestBody AuthRequestDTO.EmailLoginRequestDTO request) {
        AuthResponseDTO.LoginResponseDTO response = authService.emailLogin(request);
        return ApiResponse.onSuccess("로그인 성공", response);
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ApiResponse<AuthResponseDTO.ReissueTokenResponseDTO> reissueToken(@Valid @RequestBody AuthRequestDTO.ReissueTokenRequestDTO request) {
        AuthResponseDTO.ReissueTokenResponseDTO response = authService.reissueToken(request);
        return ApiResponse.onSuccess("토큰 재발급 성공", response);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody AuthRequestDTO.LogoutRequestDTO request) {
        authService.logout(request);
        return ApiResponse.onSuccess("로그아웃 성공");
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<AuthResponseDTO.SignupResponseDTO> signup(@Valid @RequestBody AuthRequestDTO.SignupRequestDTO request) {
        AuthResponseDTO.SignupResponseDTO response = authService.signup(request);
        return ApiResponse.onSuccess("회원가입 성공", response);
    }

    @Operation(summary = "이메일 인증 코드 발송")
    @PostMapping("/email/send")
    public ApiResponse<Void> sendEmailCode(@Valid @RequestBody AuthRequestDTO.EmailSendRequestDTO request) {
        authService.sendEmailCode(request);
        return ApiResponse.onSuccess("인증 코드가 발송되었습니다.");
    }

    @Operation(summary = "이메일 인증 코드 검증")
    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmailCode(@Valid @RequestBody AuthRequestDTO.EmailVerifyRequestDTO request) {
        authService.verifyEmailCode(request);
        return ApiResponse.onSuccess("이메일 인증 성공");
    }
}
