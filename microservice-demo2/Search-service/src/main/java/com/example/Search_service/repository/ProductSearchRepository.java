package com.example.Search_service.repository;

import com.example.Search_service.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
    List<ProductDocument> findByNameContainingIgnoreCase(String name);
    List<ProductDocument> findByCategoryId(Long categoryId);
}
