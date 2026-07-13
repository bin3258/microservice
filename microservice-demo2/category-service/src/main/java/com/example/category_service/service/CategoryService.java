package com.example.category_service.service;

import com.example.category_service.client.ProductClient;
import com.example.category_service.dto.CategoryRequest;
import com.example.category_service.dto.CategoryResponse;
import com.example.category_service.dto.ProductSummary;
import com.example.category_service.entity.Category;
import com.example.category_service.messaging.CategoryEventPublisher;
import com.example.category_service.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final ProductClient productClient;
	private final CategoryEventPublisher categoryEventPublisher;

	public CategoryService(CategoryRepository categoryRepository, ProductClient productClient, CategoryEventPublisher categoryEventPublisher) {
		this.categoryRepository = categoryRepository;
		this.productClient = productClient;
		this.categoryEventPublisher = categoryEventPublisher;
	}

	public List<CategoryResponse> getAllCategories() {
		return categoryRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	public List<CategoryResponse> getCategoryTree() {
		List<Category> all = categoryRepository.findAll();
		List<CategoryResponse> roots = new ArrayList<>();
		for (Category cat : all) {
			if (cat.getParentId() == null) {
				roots.add(buildTree(cat, all));
			}
		}
		return roots;
	}

	private CategoryResponse buildTree(Category node, List<Category> all) {
		CategoryResponse res = toResponse(node);
		List<CategoryResponse> children = new ArrayList<>();
		for (Category cat : all) {
			if (node.getId().equals(cat.getParentId())) {
				children.add(buildTree(cat, all));
			}
		}
		res.setChildren(children.isEmpty() ? null : children);
		return res;
	}

	public CategoryResponse getCategoryById(Long id) {
		return categoryRepository.findById(id)
				.map(this::toResponse)
				.orElse(null);
	}

	public List<ProductSummary> getProductsByCategory(Long categoryId) {
		ensureCategoryExists(categoryId);
		return productClient.getProductsByCategory(categoryId);
	}

	public List<Long> getSubcategoryIds(Long categoryId) {
		List<Long> ids = new ArrayList<>();
		ids.add(categoryId);
		List<Category> children = categoryRepository.findByParentId(categoryId);
		for (Category child : children) {
			ids.add(child.getId());
		}
		return ids;
	}

	public CategoryResponse createCategory(CategoryRequest request) {
		validateRequest(request);
		if (request.getParentId() != null) {
			ensureCategoryExists(request.getParentId());
		}
		Category category = new Category();
		category.setName(request.getName());
		category.setDescription(request.getDescription());
		category.setParentId(request.getParentId());
		Category savedCategory = categoryRepository.save(category);
		categoryEventPublisher.publishCategoryChanged("CREATED", savedCategory.getId(), savedCategory.getName(), savedCategory.getDescription());
		return toResponse(savedCategory);
	}

	public CategoryResponse updateCategory(Long id, CategoryRequest request) {
		validateRequest(request);
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục với mã " + id));

		if (request.getParentId() != null && request.getParentId().equals(id)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh mục không thể là cha của chính nó");
		}

		category.setName(request.getName());
		category.setDescription(request.getDescription());
		category.setParentId(request.getParentId());
		Category savedCategory = categoryRepository.save(category);
		categoryEventPublisher.publishCategoryChanged("UPDATED", savedCategory.getId(), savedCategory.getName(), savedCategory.getDescription());
		return toResponse(savedCategory);
	}

	public void deleteCategory(Long id) {
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục với mã " + id));
		List<Category> children = categoryRepository.findByParentId(id);
		for (Category child : children) {
			child.setParentId(null);
			categoryRepository.save(child);
		}
		categoryRepository.delete(category);
		categoryEventPublisher.publishCategoryChanged("DELETED", category.getId(), category.getName(), category.getDescription());
	}

	private CategoryResponse toResponse(Category category) {
		return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), category.getParentId());
	}

	private void validateRequest(CategoryRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu danh mục không được để trống");
		}
		if (request.getName() == null || request.getName().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tên danh mục");
		}
	}

	private void ensureCategoryExists(Long categoryId) {
		if (!categoryRepository.existsById(categoryId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục với mã " + categoryId);
		}
	}
}
