package com.project.recon.domain.product.controller;

import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.service.ProductService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
@Tag(name = "Product", description = "상품 관련 API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 조회")
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponseDTO.ProductDetailResponseDTO> getProduct(@PathVariable Long productId) {
        ProductResponseDTO.ProductDetailResponseDTO response = productService.getProduct(productId);
        return ApiResponse.onSuccess("상품 조회 성공", response);
    }
}
