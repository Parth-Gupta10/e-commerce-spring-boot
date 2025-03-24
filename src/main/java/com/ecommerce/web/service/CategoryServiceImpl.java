package com.ecommerce.web.service;

import com.ecommerce.web.model.Category;
import com.ecommerce.web.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (categoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID cannot be null");
        }

        Category categoryToDelete = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        categoryRepository.delete(categoryToDelete);
    }

    public Category updateCategory(Long categoryId, Category updatedCategory) {
        if (categoryId == null || updatedCategory == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID or updated data cannot be null");
        }

        Category categoryToUpdate = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        categoryToUpdate.setCategoryName(updatedCategory.getCategoryName());

        categoryRepository.save(categoryToUpdate);
        return categoryToUpdate;
    }
}
