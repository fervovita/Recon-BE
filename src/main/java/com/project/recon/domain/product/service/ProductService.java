package com.project.recon.domain.product.service;

import com.project.recon.domain.product.dto.ProductRequestDTO;
import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.entity.CategoryType;
import com.project.recon.domain.product.entity.ProductSortType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponseDTO.ProductDetailResponseDTO getProduct(Long userId, Long productId);

    Slice<ProductResponseDTO.ProductListResponseDTO> getProducts(Long userId, String keyword, CategoryType category, ProductSortType sortBy, String sortDirection, Pageable pageable);

    ProductResponseDTO.CreateProductResponseDTO createProduct(Long userId, ProductRequestDTO.CreateProductRequestDTO request, List<MultipartFile> images);

    ProductResponseDTO.DeleteProductResponseDTO deleteProduct(Long userId, Long productId);

    ProductResponseDTO.UpdateProductResponseDTO updateProduct(Long userId, Long productId, ProductRequestDTO.UpdateProductRequestDTO request, List<MultipartFile> newImages);

    ProductResponseDTO.ProductLikeResponseDTO toggleLike(Long userId, Long productId);
}
