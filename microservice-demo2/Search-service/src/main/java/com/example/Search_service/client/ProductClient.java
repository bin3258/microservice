package com.example.Search_service.client;

import com.example.Search_service.document.ProductDocument;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {
    @GetMapping
    List<ProductDocument> getAllProducts();
}
