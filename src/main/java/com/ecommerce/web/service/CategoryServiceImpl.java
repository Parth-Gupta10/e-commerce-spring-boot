package com.ecommerce.web.service;

import com.ecommerce.web.dto.request.CategoryDTO;
import com.ecommerce.web.dto.response.CategoryResponse;
import com.ecommerce.web.exception.APIException;
import com.ecommerce.web.exception.ResourceNotFoundException;
import com.ecommerce.web.model.Category;
import com.ecommerce.web.repository.CategoryRepository;
import com.ecommerce.web.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ProductRepository productRepository;

    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortingField, String sortingDir) {
        Pageable pageDetails;
        if (sortingField != null) {
            Sort sortDetails = Sort.by(Sort.Direction.fromString(sortingDir), sortingField);
            pageDetails = PageRequest.of(pageNumber, pageSize, sortDetails);
        } else {
            pageDetails = PageRequest.of(pageNumber, pageSize);
        }

        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);
        List<Category> categories = categoryPage.getContent();

        List<CategoryDTO> categoryDTOS = categories.stream().
                map(category -> modelMapper.map(category, CategoryDTO.class)).toList();
        return new CategoryResponse(categoryDTOS, categoryPage.getNumber(),
                categoryPage.getSize(), categoryPage.getNumberOfElements(), categoryPage.getTotalPages(), categoryPage.isLast());
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO category) {
        Category categoryEntity = modelMapper.map(category, Category.class);

        Optional<Category> existingCategory = categoryRepository.findByCategoryName(categoryEntity.getCategoryName());

        if (existingCategory.isPresent()) {
            throw new APIException("Category with name " + categoryEntity.getCategoryName() +
                    " already exists!!", HttpStatus.BAD_REQUEST);
        }

        Category savedCategory = categoryRepository.save(categoryEntity);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }


    @Override
    public void deleteCategory(Long categoryId) {
        if (categoryId == null) {
            throw new APIException("Category ID cannot be null", HttpStatus.BAD_REQUEST);
        }

        Category categoryToDelete = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Check if there are associated products
        if (productRepository.existsByCategoryCategoryId(categoryId)) {
            throw new APIException("Cannot delete category as it contains products", HttpStatus.BAD_REQUEST);
        }

        categoryRepository.delete(categoryToDelete);
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO updatedCategory) {
        if (categoryId == null) {
            return createCategory(updatedCategory);
        }

        if (updatedCategory == null) {
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
        return modelMapper.map(categoryToUpdate, CategoryDTO.class);
    }
}
