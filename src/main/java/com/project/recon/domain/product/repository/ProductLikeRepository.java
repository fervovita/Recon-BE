package com.project.recon.domain.product.repository;

import com.project.recon.domain.product.entity.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {

    Optional<ProductLike> findByProductIdAndUserId(Long productId, Long userId);

    long countByProductId(Long productId);

    @Query("SELECT pl.product.id, COUNT(pl) FROM ProductLike pl WHERE pl.product.id IN :productIds GROUP BY pl.product.id")
    List<Object[]> countByProductIds(List<Long> productIds);

    boolean existsByProductIdAndUserId(Long productId, Long userId);

    @Query("SELECT pl.product.id FROM ProductLike pl WHERE pl.product.id IN :productIds AND pl.user.id = :userId")
    List<Long> findLikeProductsIds(List<Long> productIds, Long userId);
}
