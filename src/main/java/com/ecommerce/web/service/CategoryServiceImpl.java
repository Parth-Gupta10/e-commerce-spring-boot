package com.ecommerce.web.service;

import com.ecommerce.web.model.Category;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final List<Category> categories = new ArrayList<>();

    @Override
    public List<Category> getAllCategories() {
        return categories;
    }

    @Override
    public void createCategory(Category category) {
        category.setCategoryId(new Date().getTime());
        categories.add(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (categoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID cannot be null");
        }

        boolean removed = categories.removeIf(category -> category.getCategoryId().equals(categoryId));

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
    }

    public Category updateCategory(Long categoryId, Category updatedCategory) {
        if (categoryId == null || updatedCategory == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID or updated data cannot be null");
        }

        Category existingCategory = categories.stream()
                .filter(category -> category.getCategoryId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        existingCategory.setCategoryName(updatedCategory.getCategoryName());

        return existingCategory;
    }
}
