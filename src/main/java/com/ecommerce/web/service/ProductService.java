package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.ProductDTO;
import com.ecommerce.web.dto.response.ProductResponse;

public interface ProductService {
    ProductDTO addProduct(ProductDTO product);

    ProductResponse getAllProducts();
}
