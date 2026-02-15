package com.project.recon.domain.product.service;

import com.project.recon.domain.product.dto.ProductRequestDTO;
import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.entity.CategoryType;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.entity.ProductImage;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final S3Service s3Service;

    @Override
    public ProductResponseDTO.ProductDetailResponseDTO getProduct(Long productId) {

        // 상품 정보 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND));

        // 상품 이미지 조회(imageOrder 순으로)
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

    @Override
    public Slice<ProductResponseDTO.ProductListResponseDTO> getProducts(String keyword, CategoryType category, Pageable pageable) {
        return productRepository.searchProducts(keyword, category, pageable)
                .map(this::toListDTO);
    }

    private ProductResponseDTO.ProductListResponseDTO toListDTO(Product product) {

        // thumbnail 조회 (imageOrder가 0이 thumbnail)
        String thumbnail = product.getImages().stream()
                .filter(img -> img.getImageOrder() == 0)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);

        return ProductResponseDTO.ProductListResponseDTO.builder()
                .id(product.getId())
                .name(product.getProductName())
                .price(product.getPrice())
                .category(product.getCategory())
                .thumbnail(thumbnail)
                .createdAt(product.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public ProductResponseDTO.CreateProductResponseDTO createProduct(Long userId, ProductRequestDTO.CreateProductRequestDTO request, List<MultipartFile> images) {

        // 이미지 유효성 사전 검증
        if (images != null && !images.isEmpty()) {
            images.forEach(this::validateImageFile);
        }

        // 유저 조회
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));

        // 상품 생성
        Product product = Product.createProduct(
                request.getName(),
                request.getPrice(),
                request.getCategory(),
                request.getDescription(),
                seller
        );

        // 상품 이미지를 S3에 업로드
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = s3Service.uploadFiles(images, "product-image");

            imageUrls.forEach(url -> {
                ProductImage productImage = ProductImage.builder().imageUrl(url).build();
                product.addImage(productImage);
            });
        }

        // 상품 저장
        productRepository.save(product);

        return ProductResponseDTO.CreateProductResponseDTO.builder()
                .id(product.getId())
                .build();
    }

    @Override
    @Transactional
    public ProductResponseDTO.DeleteProductResponseDTO deleteProduct(Long userId, Long productId) {

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND));

        // 판매자인지 확인
        if (!product.getSeller().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.PRODUCT_NOT_SELLER);
        }

        // 삭제할 이미지 url 미리 추출
        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .toList();

        // 상품 삭제
        productRepository.deleteById(productId);

        // S3에서 이미지 삭제
        imageUrls.forEach(s3Service::delete);

        return ProductResponseDTO.DeleteProductResponseDTO.builder()
                .id(product.getId())
                .build();
    }

    @Override
    @Transactional
    public ProductResponseDTO.UpdateProductResponseDTO updateProduct(Long userId, Long productId, ProductRequestDTO.UpdateProductRequestDTO request, List<MultipartFile> newImages) {

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND));

        // 판매자인지 확인
        if (!product.getSeller().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.PRODUCT_NOT_SELLER);
        }

        // 상품 정보 수정
        if (request != null) {
            product.update(
                    request.getName(),
                    request.getPrice(),
                    request.getCategory(),
                    request.getDescription()
            );
        }


        // 이미지 교체
        if (newImages != null && !newImages.isEmpty()) {
            newImages.forEach(this::validateImageFile);

            // 삭제할 이미지 url 미리 추출
            List<String> oldImageUrls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .toList();

            // 새 이미지 업로드 후 교체
            List<String> newUrls = s3Service.uploadFiles(newImages, "product-image");
            product.getImages().clear();
            newUrls.forEach(url -> product.addImage(ProductImage.builder().imageUrl(url).build()));

            // S3에서 이미지 삭제
            oldImageUrls.forEach(s3Service::delete);
        }

        return ProductResponseDTO.UpdateProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getProductName())
                .price(product.getPrice())
                .category(product.getCategory())
                .description(product.getDescription())
                .seller(ProductResponseDTO.SellerInfo.from(product.getSeller()))
                .imageUrls(product.getImages().stream().map(ProductImage::getImageUrl).toList())
                .createdAt(product.getCreatedAt())
                .build();
    }


    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new GeneralException(GeneralErrorCode.INVALID_FILE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new GeneralException(GeneralErrorCode.INVALID_FILE_TYPE);
        }
    }
}
