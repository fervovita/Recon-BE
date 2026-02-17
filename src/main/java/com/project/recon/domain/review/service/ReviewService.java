package com.project.recon.domain.review.service;

import com.project.recon.domain.review.dto.ReviewRequestDTO;
import com.project.recon.domain.review.dto.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO.CreateReviewResponseDTO createReview(Long userId, Long productId, ReviewRequestDTO.CreateReviewRequestDTO request, List<MultipartFile> images);

    Page<ReviewResponseDTO.ReviewListResponseDTO> getReviews(Long productId, Pageable pageable);

    ReviewResponseDTO.DeleteReviewResponseDTO deleteReview(Long userId, Long productId, Long reviewId);

    ReviewResponseDTO.UpdateReviewResponseDTO updateReview(Long userId, Long productId, Long reviewId, ReviewRequestDTO.UpdateReviewRequestDTO request, List<MultipartFile> newImages);
}
