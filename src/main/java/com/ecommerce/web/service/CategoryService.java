package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CategoryDTO;
import com.ecommerce.web.dto.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortingField, String sortingDir);

    CategoryDTO createCategory(CategoryDTO category);

    void deleteCategory(Long categoryId);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO updatedCategory);
}
