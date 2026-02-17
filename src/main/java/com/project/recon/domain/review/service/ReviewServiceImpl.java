package com.project.recon.domain.review.service;

import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.domain.review.dto.ReviewRequestDTO;
import com.project.recon.domain.review.dto.ReviewResponseDTO;
import com.project.recon.domain.review.entity.Review;
import com.project.recon.domain.review.entity.ReviewImage;
import com.project.recon.domain.review.repository.ReviewRepository;
import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReviewServiceImpl implements ReviewService {


    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public ReviewResponseDTO.CreateReviewResponseDTO createReview(Long userId, Long productId, ReviewRequestDTO.CreateReviewRequestDTO request, List<MultipartFile> images) {

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND));

        // 판매자인지 확인
        if (product.getSeller().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.REVIEW_SELLER_NOT_ALLOWED);
        }

        // 중복 후기 검증
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new GeneralException(GeneralErrorCode.DUPLICATE_REVIEW);
        }

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));


        // 이미지 유효성 검증
        if (images != null && !images.isEmpty()) {
            images.forEach(this::validateImageFile);
        }

        // 후기 생성
        Review review = Review.createReview(
                request.getContent(),
                request.getRating(),
                product,
                user
        );

        // 이미지 S3 업로드
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = s3Service.uploadFiles(images, "review-image");

            imageUrls.forEach(url -> {
                ReviewImage reviewImage = ReviewImage.builder().imageUrl(url).build();
                review.addImage(reviewImage);
            });
        }

        // 후기 저장
        reviewRepository.save(review);

        return ReviewResponseDTO.CreateReviewResponseDTO.builder()
                .id(review.getId())
                .build();

    }

    @Override
    @Transactional
    public ReviewResponseDTO.DeleteReviewResponseDTO deleteReview(Long userId, Long productId, Long reviewId) {

        // 후기 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.REVIEW_NOT_FOUND));

        // 해당 상품의 후기인지 확인
        if (!review.getProduct().getId().equals(productId)) {
            throw new GeneralException(GeneralErrorCode.REVIEW_NOT_FOUND);
        }

        // 작성자인지 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.REVIEW_NOT_WRITER);
        }

        // 삭제할 이미지 url 미리 추출
        List<String> imageUrls = review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        // 후기 삭제
        reviewRepository.deleteById(reviewId);

        // S3에서 이미지 삭제
        imageUrls.forEach(s3Service::delete);

        return ReviewResponseDTO.DeleteReviewResponseDTO.builder()
                .id(reviewId)
                .build();
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new GeneralException(GeneralErrorCode.INVALID_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new GeneralException(GeneralErrorCode.INVALID_FILE_TYPE);
        }
    }
}
