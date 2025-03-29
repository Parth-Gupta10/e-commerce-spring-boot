package com.ecommerce.web.controller;

import com.ecommerce.web.model.Category;
import com.ecommerce.web.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

//    To remove this constructor i.e. clean the code we can use Autowired annotation to
//    inject dependency by field
//    public CategoryController(CategoryService categoryService) {
//        this.categoryService = categoryService;
//    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return new ResponseEntity<>(categoryService.getAllCategories(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> createCategory(@Valid @RequestBody Category category) {
        categoryService.createCategory(category);
        return new ResponseEntity<>("Successfully added " + category.getCategoryName() + " to the categories", HttpStatus.CREATED);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long categoryId, @Valid @RequestBody Category updatedCategory) {
        Category updated = categoryService.updateCategory(categoryId, updatedCategory);
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Successfully deleted category with ID: " + categoryId);
    }


}
