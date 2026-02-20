package com.project.recon.domain.cart.service;

import com.project.recon.domain.cart.dto.CartRequestDTO;
import com.project.recon.domain.cart.dto.CartResponseDTO;
import com.project.recon.domain.cart.entity.Cart;
import com.project.recon.domain.cart.entity.CartItem;
import com.project.recon.domain.cart.repository.CartItemRepository;
import com.project.recon.domain.cart.repository.CartRepository;
import com.project.recon.domain.product.dto.ProductResponseDTO;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.entity.ProductImage;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
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
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponseDTO.AddCartItemResponseDTO addCartItem(Long userId, CartRequestDTO.AddCartItemRequestDTO request) {

        // 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.PRODUCT_NOT_FOUND));

        // 본인 상품인지 확인
        if (product.getSeller().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.CART_SELLER_NO_ALLOWED);
        }

        // 장바구니 조회
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));
                    return cartRepository.save(Cart.createCart(user));
                });

        // 장바구니에 상품 추가
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId())
                .map(existingItem -> {
                    if (existingItem.getQuantity() + request.getQuantity() > product.getStock()) {
                        throw new GeneralException(GeneralErrorCode.OUT_OF_STOCK);
                    }

                    existingItem.addQuantity(request.getQuantity());
                    return existingItem;
                })
                .orElseGet(() -> {
                    if (request.getQuantity() > product.getStock()) {
                        throw new GeneralException(GeneralErrorCode.OUT_OF_STOCK);
                    }
                    CartItem newItem = CartItem.createCartItem(cart, product, request.getQuantity());
                    return cartItemRepository.save(newItem);
                });


        return CartResponseDTO.AddCartItemResponseDTO.builder()
                .cartItemId(cartItem.getId())
                .productId(product.getId())
                .quantity(cartItem.getQuantity())
                .build();

    }

    @Override
    public CartResponseDTO.CartListResponseDTO getCartItems(Long userId) {

        // 장바구니 조회
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);

        // 장바구니가 존재하지 않으면
        if (cart == null) {
            return CartResponseDTO.CartListResponseDTO.builder()
                    .cartItems(List.of())
                    .cartTotalPrice(0L)
                    .build();
        }

        // 장바구니 상품 조회
        List<CartItem> cartItems = cartItemRepository.findByCartIdWithProductId(cart.getId());

        // DTO에 맞게 변환
        List<CartResponseDTO.CartItemDTO> cartItemDTOs = cartItems.stream()
                .map(this::toCartItemDTO)
                .toList();

        // 장바구니 상품의 총 가격 계산
        Long cartTotalPrice = cartItemDTOs.stream()
                .mapToLong(CartResponseDTO.CartItemDTO::getTotalPrice)
                .sum();

        return CartResponseDTO.CartListResponseDTO.builder()
                .cartItems(cartItemDTOs)
                .cartTotalPrice(cartTotalPrice)
                .build();
    }

    private CartResponseDTO.CartItemDTO toCartItemDTO(CartItem cartItem) {
        Product product = cartItem.getProduct();

        String thumbnail = product.getImages().stream()
                .filter(img -> img.getImageOrder() == 0)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);

        return CartResponseDTO.CartItemDTO.builder()
                .cartItemId(cartItem.getId())
                .productId(product.getId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .quantity(cartItem.getQuantity())
                .thumbnail(thumbnail)
                .stock(ProductResponseDTO.StockInfo.from(product.getStock()))
                .totalPrice(product.getPrice() * cartItem.getQuantity())
                .build();
    }

    @Override
    @Transactional
    public CartResponseDTO.UpdateCartItemResponseDTO updateCartItem(Long userId, Long cartItemId, CartRequestDTO.UpdateCartItemRequestDTO request) {

        // 장바구니 상품 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.CART_ITEM_NOT_FOUND));

        // 본인 장바구니인지 확인
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.FORBIDDEN);
        }

        // 재고 검증
        if (request.getQuantity() > cartItem.getProduct().getStock()) {
            throw new GeneralException(GeneralErrorCode.OUT_OF_STOCK);
        }

        // 수량 변경
        cartItem.updateQuantity(request.getQuantity());

        return CartResponseDTO.UpdateCartItemResponseDTO.builder()
                .cartItemId(cartItem.getId())
                .quantity(cartItem.getQuantity())
                .totalPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity())
                .build();
    }

    @Override
    @Transactional
    public CartResponseDTO.DeleteCartItemResponseDTO deleteCartItem(Long userId, Long cartItemId) {

        // 장바구니 상품 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.CART_ITEM_NOT_FOUND));

        // 본인 장바구니인지 확인
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new GeneralException(GeneralErrorCode.FORBIDDEN);
        }

        // 장바구니 상품 삭제
        cartItemRepository.deleteById(cartItemId);

        return CartResponseDTO.DeleteCartItemResponseDTO.builder()
                .cartItemId(cartItem.getId())
                .build();
    }
}
