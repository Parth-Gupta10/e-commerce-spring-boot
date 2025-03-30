package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CategoryDTO;
import com.ecommerce.web.dto.response.CategoryResponse;
import com.ecommerce.web.model.Category;

public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortingField, String sortingDir);

    void createCategory(CategoryDTO category);

    void deleteCategory(Long categoryId);

    Category updateCategory(Long categoryId, CategoryDTO updatedCategory);
}
