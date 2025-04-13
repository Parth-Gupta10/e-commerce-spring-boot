package com.ecommerce.web.controller;

import com.ecommerce.web.config.AppConstants;
import com.ecommerce.web.dto.request.CategoryDTO;
import com.ecommerce.web.dto.response.CategoryResponse;
import com.ecommerce.web.dto.response.ProductResponse;
import com.ecommerce.web.service.CategoryService;
import com.ecommerce.web.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

//    To remove this constructor i.e. clean the code we can use Autowired annotation to
//    inject dependency by field
//    public CategoryController(CategoryService categoryService) {
//        this.categoryService = categoryService;
//    }

    @GetMapping
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name = "page", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy", required = false) String sortingField,
            @RequestParam(name = "sortingDir", defaultValue = AppConstants.SORT_DIR) String sortingDirection) {
        return ResponseEntity.ok(categoryService.getAllCategories(pageNumber, pageSize, sortingField, sortingDirection));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO category) {
        CategoryDTO newCategory = categoryService.createCategory(category);
        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId,
                                                      @Valid @RequestBody CategoryDTO updatedCategory) {
        CategoryDTO updated = categoryService.updateCategory(categoryId, updatedCategory);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Successfully deleted category with ID: " + categoryId);
    }

    @GetMapping("/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategoryId(@PathVariable Long categoryId,
                                                                   @RequestParam(name = "page", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                   @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                   @RequestParam(name = "sortBy", required = false) String sortingField,
                                                                   @RequestParam(name = "sortingDir", defaultValue = AppConstants.SORT_DIR) String sortingDirection) {
        ProductResponse products = productService.getProductsByCategory(categoryId, pageNumber, pageSize, sortingField, sortingDirection);
        return ResponseEntity.ok(products);
    }
}
