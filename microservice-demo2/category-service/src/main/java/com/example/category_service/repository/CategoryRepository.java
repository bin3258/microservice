package com.example.category_service.repository;

import com.example.category_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByParentId(Long parentId);
	List<Category> findByParentIdIsNull();
}
