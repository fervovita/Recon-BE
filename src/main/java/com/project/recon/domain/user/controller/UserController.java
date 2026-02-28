package com.project.recon.domain.user.controller;

import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.service.ProductService;
import com.project.recon.domain.user.dto.UserRequestDTO;
import com.project.recon.domain.user.dto.UserResponseDTO;
import com.project.recon.domain.user.service.UserService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import com.project.recon.global.apiPayload.response.SliceResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;
    private final ProductService productService;

    @Operation(summary = "유저 정보 조회")
    @GetMapping("/profile")
    public ApiResponse<UserResponseDTO.UserProfileResponseDTO> getProfile(@AuthenticationPrincipal Long userId) {
        UserResponseDTO.UserProfileResponseDTO response = userService.getUserProfile(userId);
        return ApiResponse.onSuccess("유저 정보 조회 성공", response);
    }

    @Operation(summary = "좋아요한 상품 목록 조회")
    @GetMapping("/liked-products")
    public ApiResponse<SliceResponseDTO<ProductResponseDTO.ProductListResponseDTO>> getLikedProducts(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        Slice<ProductResponseDTO.ProductListResponseDTO> response = productService.getLikedProducts(userId, pageable);
        return ApiResponse.onSuccess("좋아요한 상품 목록 조회 성공", SliceResponseDTO.of(response));
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

    @Operation(summary = "전화번호 인증 코드 발송")
    @PostMapping("/phone/send")
    public ApiResponse<Void> sendPhoneCode(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDTO.PhoneSendRequestDTO request) {
        userService.sendPhoneCode(userId, request);
        return ApiResponse.onSuccess("인증 코드가 발송되었습니다.");
    }

    @Operation(summary = "전화번호 변경 및 인증 코드 검증", description = "최초 전화번호 인증 또는 새 전화번호로 변경시 사용됩니다.")
    @PatchMapping("/phone")
    public ApiResponse<UserResponseDTO.UserProfileResponseDTO> updatePhoneNumber(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserRequestDTO.UpdatePhoneRequestDTO request) {
        UserResponseDTO.UserProfileResponseDTO response = userService.updatePhoneNumber(userId, request);
        return ApiResponse.onSuccess("전화번호 변경 및 인증 성공", response);
    }
}
