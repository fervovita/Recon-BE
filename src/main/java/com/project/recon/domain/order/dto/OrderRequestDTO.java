package com.project.recon.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

public class OrderRequestDTO {

    @Getter
    public static class DirectOrderRequestDTO {

        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;

        @NotNull(message = "수량이 없습니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;
    }

    @Getter
    public static class CartOrderRequestDTO {

        @NotNull(message = "장바구니 상품을 선택해주세요")
        @Size(min = 1, message = "최소 1개 이상 선택해야 합니다.")
        private List<Long> cartItemIds;
    }
}
