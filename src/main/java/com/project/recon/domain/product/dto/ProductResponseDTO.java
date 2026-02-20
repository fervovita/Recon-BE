package com.project.recon.domain.product.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.recon.domain.product.entity.CategoryType;
import com.project.recon.domain.product.entity.StockStatus;
import com.project.recon.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponseDTO {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductDetailResponseDTO {
        private Long id;
        private String name;
        private String description;
        private List<String> imageUrls;
        private Long price;
        private SellerInfo seller;
        private CategoryType category;
        private StockInfo stock;
        private long likeCount;
        private boolean liked;
        private LocalDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductListResponseDTO {
        private Long id;
        private String name;
        private Long price;
        private CategoryType category;
        private String thumbnail;
        private StockInfo stock;
        private long likeCount;
        private boolean liked;
        private LocalDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateProductResponseDTO {
        private Long id;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DeleteProductResponseDTO {
        private Long id;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateProductResponseDTO {
        private Long id;
        private String name;
        private String description;
        private List<String> imageUrls;
        private Long price;
        private SellerInfo seller;
        private CategoryType category;
        private LocalDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductLikeResponseDTO {
        private Long productId;
        private boolean liked;
        private long likeCount;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SellerInfo {
        private Long id;
        private String nickName;

        public static SellerInfo from(User user) {
            return SellerInfo.builder()
                    .id(user.getId())
                    .nickName(user.getNickName())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class StockInfo {
        private StockStatus status;
        private Integer quantity;

        public static StockInfo from(int stock) {
            StockStatus status;
            Integer quantity = null;

            if (stock <= 0) {
                status = StockStatus.OUT_OF_STOCK;
            } else if (stock <= 10) {
                status = StockStatus.LOW_STOCK;
                quantity = stock;
            } else {
                status = StockStatus.IN_STOCK;
            }

            return StockInfo.builder()
                    .status(status)
                    .quantity(quantity)
                    .build();
        }
    }
}
