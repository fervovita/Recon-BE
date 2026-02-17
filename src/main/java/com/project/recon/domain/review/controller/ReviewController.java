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

    @Operation(summary = "후기 수정")
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
