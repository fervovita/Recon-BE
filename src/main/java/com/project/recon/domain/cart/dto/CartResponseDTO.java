package com.project.recon.domain.cart.dto;

import com.project.recon.domain.product.dto.ProductResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CartListResponseDTO {
        private List<CartItemDTO> cartItems;
        private Long cartTotalPrice;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateCartItemResponseDTO {
        private Long cartItemId;
        private Integer quantity;
        private Long totalPrice;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CartItemDTO {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private Long price;
        private Integer quantity;
        private String thumbnail;
        private ProductResponseDTO.StockInfo stock;
        private Long totalPrice;
    }
}
