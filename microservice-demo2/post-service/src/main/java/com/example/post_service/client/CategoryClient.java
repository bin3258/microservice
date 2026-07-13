package com.example.post_service.client;

import com.example.post_service.dto.CategoryInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "category-service")
public interface CategoryClient {

	@GetMapping("/api/categories/{id}")
	CategoryInfo getCategoryById(@PathVariable("id") Long id);
}
