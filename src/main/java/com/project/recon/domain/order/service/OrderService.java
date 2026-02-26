package com.project.recon.domain.order.service;

import com.project.recon.domain.order.dto.OrderRequestDTO;
import com.project.recon.domain.order.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO.OrderDetailResponseDTO createOrderFromCart(Long userId, OrderRequestDTO.CartOrderRequestDTO request);

    OrderResponseDTO.OrderDetailResponseDTO createDirectOrder(Long userId, OrderRequestDTO.DirectOrderRequestDTO request);

    OrderResponseDTO.OrderDetailResponseDTO payOrder(Long userId, Long orderId);

    void cancelOrder(Long userId, Long orderId);

    List<OrderResponseDTO.OrderDetailResponseDTO> getOrders(Long userId);

    OrderResponseDTO.OrderDetailResponseDTO getOrder(Long userId, Long orderId);
}
