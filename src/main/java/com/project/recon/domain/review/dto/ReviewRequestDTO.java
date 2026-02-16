package com.project.recon.domain.review.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

public class ReviewRequestDTO {

    @Getter
    public static class CreateReviewRequestDTO {

        @NotBlank(message = "후기 내용이 없습니다.")
        @Size(max = 1000, message = "후기 내용은 1,000자 이내로 입력해주세요.")
        private String content;

        @NotNull(message = "별점이 없습니다.")
        @Min(value = 1, message = "별점은 1 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5 이하여야 합니다.")
        private Integer rating;
    }
}
