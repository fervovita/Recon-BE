package com.project.recon.domain.product.dto;


import com.project.recon.domain.product.entity.CategoryType;
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

        private LocalDateTime createdAt;
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
}
