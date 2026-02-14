package com.project.recon.domain.product.repository;

import com.project.recon.domain.product.entity.CategoryType;
import com.project.recon.domain.product.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProductRepositoryCustom {

    Slice<Product> searchProducts(String keyword, CategoryType category, Pageable pageable);
}
