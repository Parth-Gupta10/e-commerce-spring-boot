package com.ecommerce.web.repository;

import com.ecommerce.web.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
