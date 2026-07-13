package com.example.productservice.controller;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	private void requireAdmin(HttpServletRequest request) {
		String role = (String) request.getAttribute("X-User-Role");
		if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chi ADMIN/MANAGER moi co quyen thuc hien thao tac nay");
		}
	}

	@GetMapping
	public List<ProductResponse> getAllProducts() {
		return productService.getAllProducts();
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
		ProductResponse response = productService.getProductById(id);
		if (response == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/category/{categoryId}")
	public List<ProductResponse> getProductsByCategory(@PathVariable Long categoryId) {
		return productService.getProductsByCategory(categoryId);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProductResponse> createProduct(HttpServletRequest servletRequest, @ModelAttribute ProductRequest request) {
		requireAdmin(servletRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProductResponse> updateProduct(HttpServletRequest servletRequest, @PathVariable Long id, @ModelAttribute ProductRequest request) {
		requireAdmin(servletRequest);
		return ResponseEntity.ok(productService.updateProduct(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(HttpServletRequest servletRequest, @PathVariable Long id) {
		requireAdmin(servletRequest);
		productService.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/trash")
	public List<ProductResponse> getTrash() {
		return productService.getDeletedProducts();
	}

	@PutMapping("/{id}/restore")
	public ResponseEntity<ProductResponse> restoreProduct(HttpServletRequest servletRequest, @PathVariable Long id) {
		requireAdmin(servletRequest);
		return ResponseEntity.ok(productService.restoreProduct(id));
	}

	@DeleteMapping("/{id}/hard")
	public ResponseEntity<Void> hardDeleteProduct(HttpServletRequest servletRequest, @PathVariable Long id) {
		requireAdmin(servletRequest);
		productService.hardDeleteProduct(id);
		return ResponseEntity.noContent().build();
	}
}
