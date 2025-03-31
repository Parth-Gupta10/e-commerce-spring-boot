package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CategoryDTO;
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
    @Autowired
    private CategoryService categoryService;

    private Double calculateDiscountedPrice(Product product) {
        return product.getProductPrice() - (product.getProductDiscount() * 0.01 * product.getProductPrice());
    }

    private List<ProductDTO> convertProductsToProductsDTOS(List<Product> products) {
        return products.stream().
                map(product -> {
                    ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                    productDTO.setProductDiscountedPrice(calculateDiscountedPrice(product));
                    return productDTO;
                }).toList();
    }


    @Override
    public ProductDTO addProduct(ProductDTO productDTO) {
        if (productDTO.getCategory() == null) {
            throw new APIException("category_id cannot be null", HttpStatus.BAD_REQUEST);
        }

        // Fetch category
        Category category = categoryRepository.findById(productDTO.getCategory().getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", productDTO.getCategory().getCategoryId()));

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

        List<ProductDTO> productDTOS = convertProductsToProductsDTOS(products);

        return new ProductResponse(productDTOS);
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId) {
        // Fetch category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", categoryId));
        List<Product> products = productRepository.findByCategoryOrderByProductPriceAsc(category);

        List<ProductDTO> productDTOS = convertProductsToProductsDTOS(products);

        return new ProductResponse(productDTOS);
    }

    @Override
    public ProductResponse searchProductsByQuery(String query) {
        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(query);

        List<ProductDTO> productDTOS = convertProductsToProductsDTOS(products);

        return new ProductResponse(productDTOS);
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO updatedProductDTO) {
        if (productId == null || updatedProductDTO == null) {
            throw new APIException("product id or updated data cannot be null", HttpStatus.BAD_REQUEST);
        }

        Product originalProduct = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        originalProduct.setProductName(updatedProductDTO.getProductName());
        originalProduct.setProductDescription(updatedProductDTO.getProductDescription());
        originalProduct.setProductPrice(updatedProductDTO.getProductPrice());
        originalProduct.setProductDiscount(updatedProductDTO.getProductDiscount());

        // Update category if category is provided
        if (updatedProductDTO.getCategory() != null) {
            CategoryDTO updatedCategory = categoryService.updateCategory(updatedProductDTO.getCategory().getCategoryId(),
                    updatedProductDTO.getCategory());
            originalProduct.setCategory(modelMapper.map(updatedCategory, Category.class));
        }

        Product savedProduct = productRepository.save(originalProduct);

        ProductDTO savedProductDTO = modelMapper.map(savedProduct, ProductDTO.class);
        savedProductDTO.setProductDiscountedPrice(calculateDiscountedPrice(originalProduct));
        savedProductDTO.setCategory(modelMapper.map(originalProduct.getCategory(), CategoryDTO.class));

        return savedProductDTO;
    }
}
