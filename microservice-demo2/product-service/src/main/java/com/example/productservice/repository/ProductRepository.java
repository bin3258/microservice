package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, Long> {

	List<Product> findByCategoryIdAndDeletedFalse(Long categoryId);

	List<Product> findAllByDeletedFalse();

	List<Product> findAllByDeletedTrue();
}
