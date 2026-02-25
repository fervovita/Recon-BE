package com.project.recon.domain.user.controller;

import com.project.recon.domain.user.dto.UserRequestDTO;
import com.project.recon.domain.user.dto.UserResponseDTO;
import com.project.recon.domain.user.service.UserService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "유저 정보 조회")
    @GetMapping("/profile")
    public ApiResponse<UserResponseDTO.UserProfileResponseDTO> getProfile(@AuthenticationPrincipal Long userId) {
        UserResponseDTO.UserProfileResponseDTO response = userService.getUserProfile(userId);
        return ApiResponse.onSuccess("유저 정보 조회 성공", response);
    }

    @Operation(summary = "유저 닉네임 수정")
    @PatchMapping("/nickname")
    public ApiResponse<UserResponseDTO.UserProfileResponseDTO> updateNickName(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDTO.UpdateNickNameRequestDTO request) {
        UserResponseDTO.UserProfileResponseDTO response = userService.updateNickName(userId, request);
        return ApiResponse.onSuccess("유저 닉네임 수정 성공", response);
    }

    @Operation(summary = "이메일 변경 인증 코드 발송")
    @PostMapping("/email/send")
    public ApiResponse<Void> sendEmailCode(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDTO.EmailSendRequestDTO request) {
        userService.sendEmailCode(userId, request);
        return ApiResponse.onSuccess("인증 코드가 발송되었습니다.");
    }

    @Operation(summary = "이메일 변경")
    @PatchMapping("/email")
    public ApiResponse<UserResponseDTO.UserProfileResponseDTO> updateEmail(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDTO.UpdateEmailRequestDTO request) {
        UserResponseDTO.UserProfileResponseDTO response = userService.updateEmail(userId, request);
        return ApiResponse.onSuccess("이메일 변경 성공", response);
    }

    @Operation(summary = "SMS 인증 코드 발송")
    @PostMapping("/sms/send")
    public ApiResponse<Void> sendSmsCode(@AuthenticationPrincipal Long userId) {
        userService.sendSmsCode(userId);
        return ApiResponse.onSuccess("인증 코드가 발송되었습니다.");
    }

    @Operation(summary = "SMS 인증 코드 검증")
    @PostMapping("/sms/verify")
    public ApiResponse<Void> verifySmsCode(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDTO.SmsVerifyRequestDTO request) {
        userService.verifySmsCode(userId, request);
        return ApiResponse.onSuccess("전화번호 인증 성공");
    }
}
