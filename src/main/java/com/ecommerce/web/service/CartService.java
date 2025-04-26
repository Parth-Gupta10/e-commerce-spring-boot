package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CartDTO;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getUserCart();

    CartDTO updateProductQuantityInCart(Long productId, int quantity);

    CartDTO removeProductFromCart(Long cartId, Long productId);
}
