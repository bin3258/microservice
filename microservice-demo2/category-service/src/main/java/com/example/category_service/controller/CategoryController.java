package com.example.category_service.controller;

import com.example.category_service.dto.CategoryRequest;
import com.example.category_service.dto.CategoryResponse;
import com.example.category_service.dto.ProductSummary;
import com.example.category_service.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public List<CategoryResponse> getAllCategories() {
		return categoryService.getAllCategories();
	}

	@GetMapping("/tree")
	public List<CategoryResponse> getCategoryTree() {
		return categoryService.getCategoryTree();
	}

	@GetMapping("/{id}/subcategory-ids")
	public List<Long> getSubcategoryIds(@PathVariable Long id) {
		return categoryService.getSubcategoryIds(id);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
		CategoryResponse response = categoryService.getCategoryById(id);
		if (response == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}/products")
	public List<ProductSummary> getProductsByCategory(@PathVariable("id") Long id) {
		return categoryService.getProductsByCategory(id);
	}

	@PostMapping
	public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
		return ResponseEntity.ok(categoryService.updateCategory(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
		categoryService.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}
}
