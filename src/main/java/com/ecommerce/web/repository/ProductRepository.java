package com.ecommerce.web.repository;

import com.ecommerce.web.model.Category;
import com.ecommerce.web.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageDetails);

    Page<Product> findByProductNameContainingIgnoreCase(String query, Pageable pageDetails);

    boolean existsByCategoryCategoryId(Long categoryId);
}
