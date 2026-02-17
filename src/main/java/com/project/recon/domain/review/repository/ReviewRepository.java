package com.project.recon.domain.review.repository;

import com.project.recon.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    Page<Review> findByProductId(Long productId, Pageable pageable);
}
