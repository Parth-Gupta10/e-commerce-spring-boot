package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CategoryDTO;
import com.ecommerce.web.dto.response.CategoryResponse;
import com.ecommerce.web.exception.APIException;
import com.ecommerce.web.exception.ResourceNotFoundException;
import com.ecommerce.web.model.Category;
import com.ecommerce.web.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize) {
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();

        List<CategoryDTO> categoryDTOS = categories.stream().
                map(category -> modelMapper.map(category, CategoryDTO.class)).toList();
        return new CategoryResponse(categoryDTOS, categoryPage.getNumber(),
                categoryPage.getSize(), categoryPage.getNumberOfElements(), categoryPage.getTotalPages(), categoryPage.isLast());
    }

    @Override
    public void createCategory(CategoryDTO category) {
        Category categoryEntity = modelMapper.map(category, Category.class);
        categoryRepository.findByCategoryName(categoryEntity.getCategoryName()).
                ifPresentOrElse(
                        (savedCategory) -> {
                            throw new APIException("Category with name " + categoryEntity.getCategoryName() +
                                    " already exists!!", HttpStatus.BAD_REQUEST);
                        },
                        () -> categoryRepository.save(categoryEntity)
                );
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (categoryId == null) {
            throw new APIException("Category ID cannot be null", HttpStatus.BAD_REQUEST);
        }

        Category categoryToDelete = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(categoryToDelete);
    }

    public Category updateCategory(Long categoryId, CategoryDTO updatedCategory) {
        if (categoryId == null || updatedCategory == null) {
            throw new APIException("Category ID or updated data cannot be null", HttpStatus.BAD_REQUEST);
        }

        categoryRepository.findByCategoryName(updatedCategory.getCategoryName()).
                ifPresent(
                        (savedCategory) -> {
                            throw new APIException("Category with name " + updatedCategory.getCategoryName() +
                                    " already exists!!", HttpStatus.BAD_REQUEST);
                        });

        Category categoryToUpdate = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        categoryToUpdate.setCategoryName(updatedCategory.getCategoryName());

        categoryRepository.save(categoryToUpdate);
        return categoryToUpdate;
    }
}
