package com.project.recon.domain.product.service;

import com.project.recon.domain.product.dto.ProductRequestDTO;
import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.entity.CategoryType;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.entity.ProductImage;
import com.project.recon.domain.product.entity.ProductLike;
import com.project.recon.domain.product.repository.ProductLikeRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final S3Service s3Service;
    private final ProductLikeRepository productLikeRepository;

    @Override
    public ProductResponseDTO.ProductDetailResponseDTO getProduct(Long userId, Long productId) {

        // 상품 정보 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND));

        // 상품 이미지 조회(imageOrder 순으로)
        List<String> imageUrls = product.getImages().stream()
                .sorted((a, b) -> a.getImageOrder() - b.getImageOrder())
                .map(ProductImage::getImageUrl)
                .toList();

        // 좋아요 수
        long likeCount = productLikeRepository.countByProductId(productId);

        // 좋아요 여부
        boolean liked = (userId != null) && productLikeRepository.existsByProductIdAndUserId(productId, userId);

        return ProductResponseDTO.ProductDetailResponseDTO.builder()
                .id(product.getId())
                .name(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .seller(ProductResponseDTO.SellerInfo.from(product.getSeller()))
                .imageUrls(imageUrls)
                .likeCount(likeCount)
                .liked(liked)
                .createdAt(product.getCreatedAt())
                .build();
    }

    @Override
    public Slice<ProductResponseDTO.ProductListResponseDTO> getProducts(Long userId, String keyword, CategoryType category, Pageable pageable) {

        // 상품 목록 조회
        Slice<Product> products = productRepository.searchProducts(keyword, category, pageable);

        // 조회된 상품의 id 추출
        List<Long> productIds = products.getContent().stream()
                .map(Product::getId)
                .toList();

        // 상품별 좋아요 수 조회
        Map<Long, Long> likeCountMap = productLikeRepository.countByProductIds(productIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],   // productId
                        row -> (Long) row[1]    // likeCount
                ));

        // 상품별 좋아요 여부 조회
        Set<Long> likedProductIds = (userId != null)
                ? new HashSet<>(productLikeRepository.findLikeProductsIds(productIds, userId))      // likedProductIds.contains를 할 때 Set은 O(1)이기 때문에 Set 사용
                : Set.of();

        return products.map(
                product -> toListDTO(product, likeCountMap, likedProductIds)
        );

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
        List<String> imageOrder = (request != null) ? request.getImageOrder() : null;
        if (imageOrder != null) {
            replaceImageUrl(product, imageOrder, newImages);
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

    @Override
    @Transactional
    public ProductResponseDTO.ProductLikeResponseDTO toggleLike(Long userId, Long productId) {

        // 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND));

        // 좋아요 여부 조회
        Optional<ProductLike> existingLike = productLikeRepository.findByProductIdAndUserId(productId, userId);

        boolean liked;
        if (existingLike.isPresent()) {
            // 좋아요 취소
            productLikeRepository.delete(existingLike.get());
            liked = false;
        } else {
            // 좋아요 등록
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));
            productLikeRepository.save(ProductLike.createProductLike(product, user));
            liked = true;
        }

        // 상품의 좋아요 개수 조회
        long likeCount = productLikeRepository.countByProductId(productId);

        return ProductResponseDTO.ProductLikeResponseDTO.builder()
                .productId(productId)
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }

    private ProductResponseDTO.ProductListResponseDTO toListDTO(Product product, Map<Long, Long> likeCountMap, Set<Long> likedProductIds) {

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
                .likeCount(likeCountMap.getOrDefault(product.getId(), 0L))
                .liked(likedProductIds.contains(product.getId()))
                .createdAt(product.getCreatedAt())
                .build();
    }

    private void replaceImageUrl(Product product, List<String> imageOrder, List<MultipartFile> newImages) {


        // 새 파일 유효성 검증
        if (newImages != null && !newImages.isEmpty()) {
            newImages.forEach(this::validateImageFile);
        }

        // 삭제할 이미지 url 미리 추출
        List<String> oldImageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .toList();

        // 새 파일 업로드
        List<String> uploadedUrls = (newImages != null && !newImages.isEmpty())
                ? s3Service.uploadFiles(newImages, "product-image")
                : List.of();

        // DB 이미지 삭제
        product.getImages().clear();

        // imageOrder 순서대로 이미지 추가
        Set<Integer> usedNewIndexes = new HashSet<>();

        for (String entry : imageOrder) {
            String url = resolveImageUrl(entry, uploadedUrls, oldImageUrls, usedNewIndexes);
            product.addImage(ProductImage.builder().imageUrl(url).build());
        }

        // S3에 업로드한 사용되지 않는 이미지 삭제 (새로 업로드한 이미지 중 실제로는 imageOrder에 없어 사용 X)
        for (int i = 0; i < uploadedUrls.size(); i++) {
            if (!usedNewIndexes.contains(i)) {
                s3Service.delete(uploadedUrls.get(i));
            }
        }

        // S3에서 imageOrder에 없는 이미지 삭제
        List<String> keepUrls = imageOrder.stream()
                .filter(entry -> !entry.startsWith("NEW_"))
                .toList();

        oldImageUrls.stream()
                .filter(url -> !keepUrls.contains(url))
                .forEach(s3Service::delete);
    }

    private String resolveImageUrl(String entry, List<String> uploadedUrls, List<String> oldImageUrls, Set<Integer> usedNewIndexes) {

        if (entry.startsWith("NEW_")) {
            int index;

            try {
                index = Integer.parseInt(entry.substring(4));
            } catch (NumberFormatException e) {
                uploadedUrls.forEach(s3Service::delete);
                throw new GeneralException(GeneralErrorCode.INVALID_FILE_ORDER);
            }

            if (index < 0 || index >= uploadedUrls.size()) {
                uploadedUrls.forEach(s3Service::delete);    // 이미 업로드된 S3 파일 삭제
                throw new GeneralException(GeneralErrorCode.INVALID_FILE_ORDER);
            }

            usedNewIndexes.add(index);
            return uploadedUrls.get(index);
        }

        if (!oldImageUrls.contains(entry)) {
            uploadedUrls.forEach(s3Service::delete);    // 이미 업로드된 S3 파일 삭제
            throw new GeneralException(GeneralErrorCode.INVALID_FILE_ORDER);
        }

        return entry;

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
