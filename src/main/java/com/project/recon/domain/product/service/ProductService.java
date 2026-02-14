package com.project.recon.domain.product.service;

import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.entity.CategoryType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProductService {

    ProductResponseDTO.ProductDetailResponseDTO getProduct(Long productId);

    Slice<ProductResponseDTO.ProductListResponseDTO> getProducts(String keyword, CategoryType category, Pageable pageable);
}
