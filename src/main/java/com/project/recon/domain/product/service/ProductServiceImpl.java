package com.project.recon.domain.product.service;

import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.entity.ProductImage;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponseDTO.ProductDetailResponseDTO getProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND));

        List<String> imageUrls = product.getImages().stream()
                .sorted((a, b) -> a.getImageOrder() - b.getImageOrder())
                .map(ProductImage::getImageUrl)
                .toList();

        return ProductResponseDTO.ProductDetailResponseDTO.builder()
                .id(product.getId())
                .name(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .seller(ProductResponseDTO.SellerInfo.from(product.getSeller()))
                .imageUrls(imageUrls)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
