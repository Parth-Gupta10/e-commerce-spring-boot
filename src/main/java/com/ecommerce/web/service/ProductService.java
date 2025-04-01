package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.ProductDTO;
import com.ecommerce.web.dto.response.ProductResponse;

public interface ProductService {
    ProductDTO addProduct(ProductDTO product);

    ProductResponse getAllProducts();

    ProductResponse getProductsByCategory(Long categoryId);

    ProductResponse searchProductsByQuery(String query);

    ProductDTO updateProduct(Long productId, ProductDTO product);

    ProductDTO deleteProduct(Long productId);
}
