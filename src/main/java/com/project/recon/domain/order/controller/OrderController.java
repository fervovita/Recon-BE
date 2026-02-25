package com.project.recon.domain.order.controller;

import com.project.recon.domain.order.dto.OrderRequestDTO;
import com.project.recon.domain.order.dto.OrderResponseDTO;
import com.project.recon.domain.order.service.OrderService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "장바구니 주문 생성")
    @PostMapping("/cart")
    public ApiResponse<OrderResponseDTO.OrderDetailResponseDTO> createOrderFromCart(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OrderRequestDTO.CartOrderRequestDTO request) {
        OrderResponseDTO.OrderDetailResponseDTO response = orderService.createOrderFromCart(userId, request);
        return ApiResponse.onSuccess("주문 생성 성공", response);
    }

    @Operation(summary = "바로 구매 주문 생성")
    @PostMapping("/direct")
    public ApiResponse<OrderResponseDTO.OrderDetailResponseDTO> createDirectOrder(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OrderRequestDTO.DirectOrderRequestDTO request) {
        OrderResponseDTO.OrderDetailResponseDTO response = orderService.createDirectOrder(userId, request);
        return ApiResponse.onSuccess("주문 생성 성공", response);
    }

    @Operation(summary = "주문 결제")
    @PostMapping("/{orderId}/pay")
    public ApiResponse<OrderResponseDTO.OrderDetailResponseDTO> payOrder(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long orderId) {
        OrderResponseDTO.OrderDetailResponseDTO response = orderService.payOrder(userId, orderId);
        return ApiResponse.onSuccess("결제 성공", response);
    }

}
