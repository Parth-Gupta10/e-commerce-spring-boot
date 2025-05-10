package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CategoryDTO;
import com.ecommerce.web.dto.request.ProductDTO;
import com.ecommerce.web.dto.response.ProductResponse;
import com.ecommerce.web.exception.APIException;
import com.ecommerce.web.exception.ResourceNotFoundException;
import com.ecommerce.web.model.Cart;
import com.ecommerce.web.model.CartItem;
import com.ecommerce.web.model.Category;
import com.ecommerce.web.model.Product;
import com.ecommerce.web.repository.CartItemRepository;
import com.ecommerce.web.repository.CartRepository;
import com.ecommerce.web.repository.CategoryRepository;
import com.ecommerce.web.repository.ProductRepository;
import com.ecommerce.web.util.PaginationUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;


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
    @Autowired
    private CartRepository cartRepository;

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private CartItemRepository cartItemRepository;

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


    private void updateCartsToReflectProductChanges(Long productId, Double originalProductDiscountedPrice,
                                                    Double newProductDiscountedPrice, Double newProductDiscount) {
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        for (Cart cart : carts) {
            Iterator<CartItem> iterator = cart.getCartItems().iterator();
            while (iterator.hasNext()) {
                CartItem cartItem = iterator.next();
                if (Objects.equals(cartItem.getProduct().getProductId(), productId)) {
                    Double currentCartTotalPrice = cart.getTotalPrice();

                    if (newProductDiscountedPrice == 0.0) {
                        // Product is being deleted, remove the cart item
                        cart.setTotalPrice(currentCartTotalPrice - originalProductDiscountedPrice * cartItem.getQuantity());
                        iterator.remove(); // Use iterator to remove the item
                    } else {
                        // Product price is being updated, adjust the total price
                        cart.setTotalPrice(currentCartTotalPrice - originalProductDiscountedPrice * cartItem.getQuantity() +
                                newProductDiscountedPrice * cartItem.getQuantity());
                    }
                }

                cartItem.setProductPrice(newProductDiscountedPrice);
                cartItem.setDiscount(newProductDiscount);
                cartItemRepository.save(cartItem);
            }
            cartRepository.save(cart);
        }
    }

    @Override
    public ProductDTO addProduct(ProductDTO newProductDTO) {
        if (newProductDTO.getCategory() == null) {
            throw new APIException("Category cannot be null", HttpStatus.BAD_REQUEST);
        }

        // Normalize category name
        String normalizedCategoryName = newProductDTO.getCategory().getCategoryName().trim().toLowerCase();

        // Check if category exists -> if it does then return it else create a new category
        Category category = categoryRepository
                .findByCategoryName(normalizedCategoryName)
                .orElseGet(() -> {
                    // Create a new category if not found
                    Category newCategory = new Category();
                    newCategory.setCategoryName(normalizedCategoryName);
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
        Pageable pageDetails = PaginationUtil.createPageable(pageNumber, pageSize, sortingField, sortingDirection);
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

        Pageable pageDetails = PaginationUtil.createPageable(pageNumber, pageSize, sortingField, sortingDirection);
        Page<Product> productsPage = productRepository.findByCategory(category, pageDetails);
        List<Product> products = productsPage.getContent();

        List<ProductDTO> productDTOS = convertProductsToProductsDTOS(products);

        return new ProductResponse(productDTOS, productsPage.getNumber(), productsPage.getSize(),
                productsPage.getNumberOfElements(), productsPage.getTotalPages(), productsPage.isLast());
    }

    @Override
    public ProductResponse searchProductsByQuery(String query, Integer pageNumber, Integer pageSize,
                                                 String sortingField, String sortingDirection) {
        Pageable pageDetails = PaginationUtil.createPageable(pageNumber, pageSize, sortingField, sortingDirection);
        Page<Product> productsPage = productRepository.findByProductNameContainingIgnoreCase(query, pageDetails);
        List<Product> products = productsPage.getContent();

        List<ProductDTO> productDTOS = convertProductsToProductsDTOS(products);

        return new ProductResponse(productDTOS, productsPage.getNumber(), productsPage.getSize(),
                productsPage.getNumberOfElements(), productsPage.getTotalPages(), productsPage.isLast());
    }

    @Transactional
    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO updatedProductDTO) {
        if (productId == null || updatedProductDTO == null) {
            throw new APIException("product id or updated data cannot be null", HttpStatus.BAD_REQUEST);
        }

        Product originalProduct = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        Double originalProductDiscountedPrice = calculateDiscountedPrice(originalProduct);

        originalProduct.setProductName(updatedProductDTO.getProductName());
        originalProduct.setProductDescription(updatedProductDTO.getProductDescription());
        originalProduct.setProductPrice(updatedProductDTO.getProductPrice());
        originalProduct.setProductDiscount(updatedProductDTO.getProductDiscount());
        originalProduct.setProductQuantity(updatedProductDTO.getProductQuantity());

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

        //update carts tp reflect the new product changes i.e. price changes
        updateCartsToReflectProductChanges(productId, originalProductDiscountedPrice,
                savedProductDTO.getProductDiscountedPrice(), originalProduct.getProductDiscount());

        return savedProductDTO;
    }

    @Transactional
    @Override
    public ProductDTO deleteProduct(Long productId) {
        if (productId == null) {
            throw new APIException("product id cannot be null", HttpStatus.BAD_REQUEST);
        }

        Product productToDelete = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        Double originalProductDiscountedPrice = calculateDiscountedPrice(productToDelete);

        // Detach the product entity
        entityManager.detach(productToDelete);

        // Update carts before deleting the product
        updateCartsToReflectProductChanges(productId, originalProductDiscountedPrice, 0.0, 0.0);

        // Now delete the product
        productRepository.delete(productToDelete);

        return modelMapper.map(productToDelete, ProductDTO.class);
    }
}
