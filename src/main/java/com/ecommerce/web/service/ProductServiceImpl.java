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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ProductDTO addProduct(ProductDTO newProductDTO) {
        if (newProductDTO.getCategory() == null) {
            throw new APIException("Category cannot be null", HttpStatus.BAD_REQUEST);
        }

        // Check if category exists -> if it does then return it else create a new category
        Category category = categoryRepository
                .findByCategoryName(newProductDTO.getCategory().getCategoryName())
                .orElseGet(() -> {
                    // Create a new category if not found
                    Category newCategory = new Category();
                    newCategory.setCategoryName(newProductDTO.getCategory().getCategoryName());
                    return categoryRepository.save(newCategory);
                });

        // Map DTO to Entity
        Product product = modelMapper.map(newProductDTO, Product.class);
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
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortingField, String sortingDirection) {
        Pageable pageDetails;
        if (sortingField != null) {
            Sort sortDetails = Sort.by(Sort.Direction.fromString(sortingDirection), sortingField);
            pageDetails = PageRequest.of(pageNumber, pageSize, sortDetails);
        } else {
            pageDetails = PageRequest.of(pageNumber, pageSize);
        }
        Page<Product> productsPage = productRepository.findAll(pageDetails);
        List<Product> products = productsPage.getContent();

        List<ProductDTO> productDTOS = convertProductsToProductsDTOS(products);

        return new ProductResponse(productDTOS, productsPage.getNumber(), productsPage.getSize(),
                productsPage.getNumberOfElements(), productsPage.getTotalPages(), productsPage.isLast());
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize,
                                                 String sortingField, String sortingDirection) {
        // Fetch category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", categoryId));

        Pageable pageDetails;
        if (sortingField != null) {
            Sort sortDetails = Sort.by(Sort.Direction.fromString(sortingDirection), sortingField);
            pageDetails = PageRequest.of(pageNumber, pageSize, sortDetails);
        } else {
            pageDetails = PageRequest.of(pageNumber, pageSize);
        }
        Page<Product> productsPage = productRepository.findByCategory(category, pageDetails);
        List<Product> products = productsPage.getContent();


        List<ProductDTO> productDTOS = convertProductsToProductsDTOS(products);

        return new ProductResponse(productDTOS, productsPage.getNumber(), productsPage.getSize(),
                productsPage.getNumberOfElements(), productsPage.getTotalPages(), productsPage.isLast());
    }

    @Override
    public ProductResponse searchProductsByQuery(String query, Integer pageNumber, Integer pageSize,
                                                 String sortingField, String sortingDirection) {
        Pageable pageDetails;
        if (sortingField != null) {
            Sort sortDetails = Sort.by(Sort.Direction.fromString(sortingDirection), sortingField);
            pageDetails = PageRequest.of(pageNumber, pageSize, sortDetails);
        } else {
            pageDetails = PageRequest.of(pageNumber, pageSize);
        }
        Page<Product> productsPage = productRepository.findByProductNameContainingIgnoreCase(query, pageDetails);
        List<Product> products = productsPage.getContent();

        List<ProductDTO> productDTOS = convertProductsToProductsDTOS(products);

        return new ProductResponse(productDTOS, productsPage.getNumber(), productsPage.getSize(),
                productsPage.getNumberOfElements(), productsPage.getTotalPages(), productsPage.isLast());
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

    @Override
    public ProductDTO deleteProduct(Long productId) {
        if (productId == null) {
            throw new APIException("product id cannot be null", HttpStatus.BAD_REQUEST);
        }

        Product productToDelete = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        productRepository.delete(productToDelete);
        return modelMapper.map(productToDelete, ProductDTO.class);
    }
}
