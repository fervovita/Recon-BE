package com.project.recon.domain.auth.controller;

import com.project.recon.domain.auth.dto.AuthRequestDTO;
import com.project.recon.domain.auth.dto.AuthResponseDTO;
import com.project.recon.domain.auth.service.AuthService;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.apiPayload.response.ApiResponse;
import com.project.recon.global.jwt.CookieProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
    private final CookieProvider cookieProvider;

    @Operation(summary = "카카오 로그인")
    @PostMapping("/kakao/login")
    public ApiResponse<Void> kakaoLogin(@Valid @RequestBody AuthRequestDTO.KakaoLoginRequestDTO request, HttpServletResponse response) {
        AuthResponseDTO.TokenResponseDTO tokens = authService.kakaoLogin(request);
        addTokenCookies(response, tokens);
        return ApiResponse.onSuccess("로그인 성공");
    }

    @Operation(summary = "일반 로그인")
    @PostMapping("/login")
    public ApiResponse<Void> emailLogin(@Valid @RequestBody AuthRequestDTO.EmailLoginRequestDTO request, HttpServletResponse response) {
        AuthResponseDTO.TokenResponseDTO tokens = authService.emailLogin(request);
        addTokenCookies(response, tokens);
        return ApiResponse.onSuccess("로그인 성공");
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ApiResponse<Void> reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        AuthResponseDTO.TokenResponseDTO tokens = authService.reissueToken(refreshToken);
        addTokenCookies(response, tokens);
        return ApiResponse.onSuccess("토큰 재발급 성공");
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        authService.logout(refreshToken);
        clearTokenCookies(response);
        return ApiResponse.onSuccess("로그아웃 성공");
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody AuthRequestDTO.SignupRequestDTO request, HttpServletResponse response) {
        AuthResponseDTO.TokenResponseDTO tokens = authService.signup(request);
        addTokenCookies(response, tokens);
        return ApiResponse.onSuccess("회원가입 성공");
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

    private void addTokenCookies(HttpServletResponse response, AuthResponseDTO.TokenResponseDTO tokens) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieProvider.createAccessTokenCookie(tokens.getAccessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieProvider.createRefreshTokenCookie(tokens.getRefreshToken()).toString());
    }

    private void clearTokenCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieProvider.deleteAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieProvider.deleteRefreshTokenCookie().toString());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refresh_token")) {
                    return cookie.getValue();
                }
            }
        }

        throw new GeneralException(GeneralErrorCode.INVALID_TOKEN);
    }
}
