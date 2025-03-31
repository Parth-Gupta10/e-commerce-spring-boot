package com.ecommerce.web.controller;

import com.ecommerce.web.config.AppConstants;
import com.ecommerce.web.dto.request.CategoryDTO;
import com.ecommerce.web.dto.request.ProductDTO;
import com.ecommerce.web.dto.response.CategoryResponse;
import com.ecommerce.web.dto.response.ProductResponse;
import com.ecommerce.web.exception.APIException;
import com.ecommerce.web.model.Category;
import com.ecommerce.web.service.CategoryService;
import com.ecommerce.web.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<String> createCategory(@Valid @RequestBody CategoryDTO category) {
        try {
            categoryService.createCategory(category);
            return new ResponseEntity<>("Successfully added " + category.getCategoryName() + " to the categories", HttpStatus.CREATED);
        } catch (APIException e) {
            return new ResponseEntity<>(e.getMessage(), e.getHttpStatus());
        }
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long categoryId,
                                                   @Valid @RequestBody CategoryDTO updatedCategory) {
        Category updated = categoryService.updateCategory(categoryId, updatedCategory);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Successfully deleted category with ID: " + categoryId);
    }

    @GetMapping("/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategoryId(@PathVariable Long categoryId) {
        ProductResponse products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }
}
