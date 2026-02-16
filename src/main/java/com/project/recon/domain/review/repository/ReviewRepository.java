package com.project.recon.domain.review.repository;

import com.project.recon.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByProductIdAndUserId(Long productId, Long userId);
}
