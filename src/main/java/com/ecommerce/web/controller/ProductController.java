package com.ecommerce.web.controller;

import com.ecommerce.web.dto.request.ProductDTO;
import com.ecommerce.web.dto.response.ProductResponse;
import com.ecommerce.web.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO product) {
        ProductDTO addedProduct = productService.addProduct(product);
        return new ResponseEntity<>(addedProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ProductResponse> getAllProducts() {
        ProductResponse productResponse = productService.getAllProducts();
        return ResponseEntity.ok().body(productResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<ProductResponse> searchProductsByQuery(@RequestParam String query) {
        ProductResponse productResponse = productService.searchProductsByQuery(query);
        return ResponseEntity.ok().body(productResponse);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductDTO product) {
        ProductDTO updatedProduct = productService.updateProduct(productId, product);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId) {
        ProductDTO deletedProduct = productService.deleteProduct(productId);
        return ResponseEntity.ok().body(deletedProduct);
    }
}
