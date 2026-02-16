package com.project.recon.domain.review.service;

import com.project.recon.domain.review.dto.ReviewRequestDTO;
import com.project.recon.domain.review.dto.ReviewResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO.CreateReviewResponseDTO createReview(Long userId, Long productId, ReviewRequestDTO.CreateReviewRequestDTO request, List<MultipartFile> images);
}
