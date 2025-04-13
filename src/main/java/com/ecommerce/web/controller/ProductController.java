package com.ecommerce.web.controller;

import com.ecommerce.web.config.AppConstants;
import com.ecommerce.web.dto.request.ProductDTO;
import com.ecommerce.web.dto.response.ProductResponse;
import com.ecommerce.web.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO product) {
        ProductDTO addedProduct = productService.addProduct(product);
        return new ResponseEntity<>(addedProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "page", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy", required = false) String sortingField,
            @RequestParam(name = "sortingDir", defaultValue = AppConstants.SORT_DIR) String sortingDirection
    ) {
        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortingField, sortingDirection);
        return ResponseEntity.ok().body(productResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<ProductResponse> searchProductsByQuery(@RequestParam String query,
                                                                 @RequestParam(name = "page", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                 @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                 @RequestParam(name = "sortBy", required = false) String sortingField,
                                                                 @RequestParam(name = "sortingDir", defaultValue = AppConstants.SORT_DIR) String sortingDirection) {
        ProductResponse productResponse = productService.searchProductsByQuery(query, pageNumber, pageSize, sortingField, sortingDirection);
        return ResponseEntity.ok().body(productResponse);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductDTO product) {
        ProductDTO updatedProduct = productService.updateProduct(productId, product);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId) {
        ProductDTO deletedProduct = productService.deleteProduct(productId);
        return ResponseEntity.ok().body(deletedProduct);
    }
}
