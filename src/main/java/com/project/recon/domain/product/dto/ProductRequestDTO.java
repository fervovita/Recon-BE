package com.project.recon.domain.product.dto;


import com.project.recon.domain.product.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class ProductRequestDTO {

    @Getter
    public static class CreateProductRequestDTO {
        @NotBlank(message = "상품명이 없습니다.")
        private String name;

        @NotNull(message = "가격이 없습니다.")
        @Positive(message = "가격은 0보다 커야 합니다.")
        private Long price;

        @NotNull(message = "카테고리가 없습니다.")
        private CategoryType category;

        @NotBlank(message = "상품 설명이 없습니다.")
        @Size(max = 15000, message = "상품 설명은 15,000자 이내로 입력해주세요.")  // TEXT 기준
        private String description;
    }
}
