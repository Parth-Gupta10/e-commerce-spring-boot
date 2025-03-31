package com.ecommerce.web.repository;

import com.ecommerce.web.model.Category;
import com.ecommerce.web.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryOrderByProductPriceAsc(Category category);

    List<Product> findByProductNameContainingIgnoreCase(String query);
}
