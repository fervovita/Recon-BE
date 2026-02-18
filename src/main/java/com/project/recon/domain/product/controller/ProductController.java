package com.project.recon.domain.product.controller;

import com.project.recon.domain.product.dto.ProductRequestDTO;
import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.entity.CategoryType;
import com.project.recon.domain.product.service.ProductService;
import com.project.recon.global.apiPayload.response.ApiResponse;
import com.project.recon.global.apiPayload.response.SliceResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "Product", description = "상품 관련 API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 조회")
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponseDTO.ProductDetailResponseDTO> getProduct(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        ProductResponseDTO.ProductDetailResponseDTO response = productService.getProduct(userId, productId);
        return ApiResponse.onSuccess("상품 조회 성공", response);
    }

    @Operation(summary = "상품 목록 조회")
    @GetMapping
    public ApiResponse<SliceResponseDTO<ProductResponseDTO.ProductListResponseDTO>> getProducts(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CategoryType category,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Slice<ProductResponseDTO.ProductListResponseDTO> response = productService.getProducts(userId, keyword, category, pageable);
        return ApiResponse.onSuccess("상품 목록 조회 성공", SliceResponseDTO.of(response));
    }

    @Operation(summary = "상품 등록")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponseDTO.CreateProductResponseDTO> createProduct(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestPart("productData") ProductRequestDTO.CreateProductRequestDTO request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ProductResponseDTO.CreateProductResponseDTO response = productService.createProduct(userId, request, images);
        return ApiResponse.onSuccess("상품 등록 성공", response);
    }

    @Operation(summary = "상품 삭제")
    @DeleteMapping("/{productId}")
    public ApiResponse<ProductResponseDTO.DeleteProductResponseDTO> deleteProduct(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        ProductResponseDTO.DeleteProductResponseDTO response = productService.deleteProduct(userId, productId);
        return ApiResponse.onSuccess("상품 삭제 성공", response);
    }

    @Operation(summary = "상품 수정", description = """
            **imageOrder 사용법:**
            - 기존 이미지 유지: S3 URL을 그대로 입력 (예: "https://s3.../image1.jpg")
            - 새 이미지 추가: "NEW_인덱스" 형식 (예: "NEW_0" → images의 0번째 파일)
            - imageOrder의 순서가 곧 사용자가 첨부한 이미지 파일의 순서가 됩니다.
            - imageOrder에 포함되지 않은 기존 이미지는 자동 삭제됩니다.
            
            **예시:**
            - imageOrder: ["https://s3.../url1", "NEW_0", "https://s3.../url2", "NEW_1"]
            - images: [새파일A.png, 새파일B.png]
            - 결과: url1 → 새파일A → url2 → 새파일B
            """)
    @PatchMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponseDTO.UpdateProductResponseDTO> updateProduct(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId,
            @Valid @RequestPart(value = "productData", required = false) ProductRequestDTO.UpdateProductRequestDTO request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ProductResponseDTO.UpdateProductResponseDTO response = productService.updateProduct(userId, productId, request, images);
        return ApiResponse.onSuccess("상품 수정 성공", response);
    }

    @Operation(summary = "상품 좋아요 토글")
    @PostMapping("/{productId}/like")
    public ApiResponse<ProductResponseDTO.ProductLikeResponseDTO> toggleLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        ProductResponseDTO.ProductLikeResponseDTO response = productService.toggleLike(userId, productId);
        return ApiResponse.onSuccess("좋아요 처리 성공", response);
    }
}
