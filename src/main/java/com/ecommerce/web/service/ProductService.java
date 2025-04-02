package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.ProductDTO;
import com.ecommerce.web.dto.response.ProductResponse;

public interface ProductService {
    ProductDTO addProduct(ProductDTO product);

    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortingField, String sortingDirection);

    ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortingField, String sortingDirection);

    ProductResponse searchProductsByQuery(String query, Integer pageNumber, Integer pageSize, String sortingField, String sortingDirection);

    ProductDTO updateProduct(Long productId, ProductDTO product);

    ProductDTO deleteProduct(Long productId);
}
