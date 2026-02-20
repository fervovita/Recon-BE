package com.project.recon.domain.cart.service;

import com.project.recon.domain.cart.dto.CartRequestDTO;
import com.project.recon.domain.cart.dto.CartResponseDTO;

public interface CartService {

    CartResponseDTO.AddCartItemResponseDTO addCartItem(Long userId, CartRequestDTO.AddCartItemRequestDTO request);

    CartResponseDTO.CartListResponseDTO getCartItems(Long userId);
}
