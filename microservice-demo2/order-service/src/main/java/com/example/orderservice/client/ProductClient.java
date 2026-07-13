package com.example.orderservice.client;

import com.example.orderservice.payload.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {

	@GetMapping("/api/products/{id}")
	ProductInfo getProductById(@PathVariable("id") Long id);
}
