package com.project.recon.domain.product.repository;

import com.project.recon.domain.product.entity.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    Optional<ProductLike> findByProductIdAndUserId(Long productId, Long userId);

    long countByProductId(Long productId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);
}
