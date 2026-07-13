package com.example.productservice.client;

import com.example.productservice.dto.CategoryInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "category-service")
public interface CategoryClient {

	@GetMapping("/api/categories/{id}")
	CategoryInfo getCategoryById(@PathVariable("id") Long id);
}
