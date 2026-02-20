package com.project.recon.domain.cart.service;

import com.project.recon.domain.cart.dto.CartRequestDTO;
import com.project.recon.domain.cart.dto.CartResponseDTO;
import com.project.recon.domain.cart.entity.Cart;
import com.project.recon.domain.cart.entity.CartItem;
import com.project.recon.domain.cart.repository.CartItemRepository;
import com.project.recon.domain.cart.repository.CartRepository;
import com.project.recon.domain.product.entity.Product;
import com.project.recon.domain.product.repository.ProductRepository;
import com.project.recon.domain.user.entity.User;
import com.project.recon.domain.user.repository.UserRepository;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
