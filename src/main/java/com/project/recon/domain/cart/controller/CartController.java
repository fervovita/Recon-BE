package com.project.recon.domain.cart.controller;

import com.project.recon.domain.cart.dto.CartRequestDTO;
import com.project.recon.domain.cart.dto.CartResponseDTO;
import com.project.recon.domain.cart.service.CartService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
@Tag(name = "Cart", description = "장바구니 관련 API")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 상품 추가")
    @PostMapping
    public ApiResponse<CartResponseDTO.AddCartItemResponseDTO> addCartItem(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CartRequestDTO.AddCartItemRequestDTO request) {
        CartResponseDTO.AddCartItemResponseDTO response = cartService.addCartItem(userId, request);
        return ApiResponse.onSuccess("장바구니 추가 성공", response);
    }

    @Operation(summary = "장바구니 조회")
    @GetMapping
    public ApiResponse<CartResponseDTO.CartListResponseDTO> getCartItems(
            @AuthenticationPrincipal Long userId) {
        CartResponseDTO.CartListResponseDTO response = cartService.getCartItems(userId);
        return ApiResponse.onSuccess("장바구니 조회 성공", response);
    }

    @Operation(summary = "장바구니 상품 수량 변경")
    @PatchMapping("/{cartItemId}")
    public ApiResponse<CartResponseDTO.UpdateCartItemResponseDTO> updateCartItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartRequestDTO.UpdateCartItemRequestDTO request) {
        CartResponseDTO.UpdateCartItemResponseDTO response = cartService.updateCartItem(userId, cartItemId, request);
        return ApiResponse.onSuccess("장바구니 수량 변경 성공", response);
    }

    @Operation(summary = "장바구니 상품 삭제")
    @DeleteMapping("/{cartItemId}")
    public ApiResponse<CartResponseDTO.DeleteCartItemResponseDTO> deleteCartItem(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long cartItemId) {
        CartResponseDTO.DeleteCartItemResponseDTO response = cartService.deleteCartItem(userId, cartItemId);
        return ApiResponse.onSuccess("장바구니 상품 삭제 성공", response);
    }
}
