package com.project.recon.domain.product.service;

import com.project.recon.domain.product.dto.ProductResponseDTO;

public interface ProductService {

    ProductResponseDTO.ProductDetailResponseDTO getProduct(Long productId);
}
