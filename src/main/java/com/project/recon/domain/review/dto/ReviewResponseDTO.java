package com.project.recon.domain.review.dto;

import com.project.recon.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponseDTO {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateReviewResponseDTO {
        private Long id;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReviewListResponseDTO {
        private Long id;
        private String content;
        private int rating;
        private List<String> imageUrls;
        private WriterInfo writer;
        private LocalDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WriterInfo {
        private Long id;
        private String nickName;

        public static WriterInfo from(User user) {
            return WriterInfo.builder()
                    .id(user.getId())
                    .nickName(user.getNickName())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DeleteReviewResponseDTO {
        private Long id;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UpdateReviewResponseDTO {
        private Long id;
        private String content;
        private Integer rating;
        private List<String> imageUrls;
    }
}
