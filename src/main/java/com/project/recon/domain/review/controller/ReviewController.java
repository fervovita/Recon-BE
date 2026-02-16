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
}
