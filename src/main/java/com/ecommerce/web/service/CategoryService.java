package com.ecommerce.web.service;

import com.ecommerce.web.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    void createCategory(Category category);
    void deleteCategory(Long categoryId);
    Category updateCategory(Long categoryId, Category updatedCategory);
}
