package com.project.recon.domain.review.controller;

import com.project.recon.domain.review.dto.ReviewRequestDTO;
import com.project.recon.domain.review.dto.ReviewResponseDTO;
import com.project.recon.domain.review.service.ReviewService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/reviews")
@Tag(name = "Review", description = "후기 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "후기 등록")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ReviewResponseDTO.CreateReviewResponseDTO> createReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId,
            @Valid @RequestPart("reviewData") ReviewRequestDTO.CreateReviewRequestDTO request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ReviewResponseDTO.CreateReviewResponseDTO response = reviewService.createReview(userId, productId, request, images);
        return ApiResponse.onSuccess("후기 등록 성공", response);
    }

    @Operation(summary = "후기 삭제")
    @DeleteMapping("/{reviewId}")
    public ApiResponse<ReviewResponseDTO.DeleteReviewResponseDTO> deleteReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        ReviewResponseDTO.DeleteReviewResponseDTO response = reviewService.deleteReview(userId, productId, reviewId);
        return ApiResponse.onSuccess("후기 삭제 성공", response);
    }

    @Operation(summary = "후기 수정", description = """
            **이미지 수정 방법:**
            - remainingImageUrls: 유지할 기존 이미지 URL 목록
            - images: 새로 추가할 이미지 파일
            - remainingImageUrls에 포함되지 않은 기존 이미지는 자동 삭제됩니다.
            - 새 이미지는 기존 이미지 뒤에 추가됩니다.
            
            **예시:**
            - remainingImageUrls: ["https://s3.../url1", "https://s3.../url3"]
            - images: [새파일.jpg]
            - 결과: url1 → url3 → 새파일
            """)
    @PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ReviewResponseDTO.UpdateReviewResponseDTO> updateReview(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @Valid @RequestPart(value = "reviewData", required = false) ReviewRequestDTO.UpdateReviewRequestDTO request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ReviewResponseDTO.UpdateReviewResponseDTO response = reviewService.updateReview(userId, productId, reviewId, request, images);
        return ApiResponse.onSuccess("후기 수정 성공", response);
    }
}
