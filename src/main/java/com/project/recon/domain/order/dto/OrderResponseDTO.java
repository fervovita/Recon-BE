package com.project.recon.domain.order.dto;

import com.project.recon.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderDetailResponseDTO {
        private Long orderId;
        private OrderStatus status;
        private Long totalPrice;
        private List<OrderItemDTO> items;
        private LocalDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderItemDTO {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Long orderPrice;
        private Long totalPrice;
    }

}
