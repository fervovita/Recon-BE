package com.project.recon.domain.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CartResponseDTO {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AddCartItemResponseDTO {
        private Long cartItemId;
        private Long productId;
        private Integer quantity;
    }
}
