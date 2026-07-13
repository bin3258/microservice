package com.example.Search_service.repository;

import com.example.Search_service.document.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {
    List<PostDocument> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content);
    List<PostDocument> findByCategoryId(Long categoryId);
    List<PostDocument> findByStatus(String status);
}
