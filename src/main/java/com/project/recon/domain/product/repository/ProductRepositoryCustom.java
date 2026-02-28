package com.project.recon.domain.product.repository;

import com.project.recon.domain.product.entity.CategoryType;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.entity.ProductSortType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ProductRepositoryCustom {

    Slice<Product> searchProducts(String keyword, CategoryType category, ProductSortType sortBy, String sortDirection, Pageable pageable);

    Slice<Product> searchProductByIds(List<Long> productIds, CategoryType category, ProductSortType sortBy, String sortDirection, Pageable pageable);

    Slice<Product> searchLikedProducts(Long userId, Pageable pageable);
}
