package com.project.recon.domain.product.controller;

import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.entity.CategoryType;
import com.project.recon.domain.product.service.ProductService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import com.project.recon.global.apiPayload.response.SliceResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "상품 목록 조회")
    @GetMapping
    public ApiResponse<SliceResponseDTO<ProductResponseDTO.ProductListResponseDTO>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CategoryType category,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Slice<ProductResponseDTO.ProductListResponseDTO> response = productService.getProducts(keyword, category, pageable);
        return ApiResponse.onSuccess("상품 목록 조회 성공", SliceResponseDTO.of(response));
    }
}
