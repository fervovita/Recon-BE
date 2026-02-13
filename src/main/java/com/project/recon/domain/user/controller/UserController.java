package com.project.recon.domain.user.controller;

import com.project.recon.domain.user.dto.UserResponseDTO;
import com.project.recon.domain.user.service.UserService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "유저 정보 조회")
    @GetMapping("/profile")
    public ApiResponse<UserResponseDTO.UserProfileResponseDTO> getProfile(@AuthenticationPrincipal Long userId) {
        UserResponseDTO.UserProfileResponseDTO response = userService.getUserProfile(userId);
        return ApiResponse.onSuccess("유저 정보 조회 성공", response);
    }
}
