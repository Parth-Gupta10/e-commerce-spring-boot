package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.ProductDTO;
import com.ecommerce.web.dto.response.ProductResponse;
import com.ecommerce.web.exception.APIException;
import com.ecommerce.web.exception.ResourceNotFoundException;
import com.ecommerce.web.model.Category;
import com.ecommerce.web.model.Product;
import com.ecommerce.web.repository.CategoryRepository;
import com.ecommerce.web.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    private Double calculateDiscountedPrice(Product product) {
        return product.getProductPrice() - (product.getProductDiscount() * 0.01 * product.getProductPrice());
    }

    @Override
    public ProductDTO addProduct(ProductDTO productDTO) {
        if (productDTO.getCategoryId() == null) {
            throw new APIException("category_id cannot be null", HttpStatus.BAD_REQUEST);
        }

        // Fetch category
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", productDTO.getCategoryId()));

        // Map DTO to Entity
        Product product = modelMapper.map(productDTO, Product.class);
        product.setCategory(category);
        product.setProductImageUrl("test.png");

        // Save Product
        Product savedProduct = productRepository.save(product);

        // Convert to DTO
        ProductDTO savedProductDTO = modelMapper.map(savedProduct, ProductDTO.class);
        savedProductDTO.setProductDiscountedPrice(calculateDiscountedPrice(savedProduct));

        return savedProductDTO;
    }

    @Override
    public ProductResponse getAllProducts() {
        List<Product> products = productRepository.findAll();

        List<ProductDTO> productDTOS = products.stream().
                map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        return new ProductResponse(productDTOS);
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId) {
        // Fetch category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", categoryId));
        List<Product> products = productRepository.findByCategoryOrderByProductPriceAsc(category);

        List<ProductDTO> productDTOS = products.stream().
                map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        return new ProductResponse(productDTOS);
    }
}
