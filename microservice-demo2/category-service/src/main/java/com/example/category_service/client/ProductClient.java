package com.example.category_service.client;

import com.example.category_service.dto.ProductSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductClient {

	@GetMapping("/api/products/category/{categoryId}")
	List<ProductSummary> getProductsByCategory(@PathVariable("categoryId") Long categoryId);
}
