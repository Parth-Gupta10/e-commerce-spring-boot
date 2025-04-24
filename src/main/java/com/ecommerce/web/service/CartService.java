package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CartDTO;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);
}
