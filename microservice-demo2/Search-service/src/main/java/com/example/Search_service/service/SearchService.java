package com.example.Search_service.service;

import com.example.Search_service.document.PostDocument;
import com.example.Search_service.document.ProductDocument;
import com.example.Search_service.dto.SearchResult;
import com.example.Search_service.repository.PostSearchRepository;
import com.example.Search_service.repository.ProductSearchRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {
    private final ProductSearchRepository productSearchRepository;
    private final PostSearchRepository postSearchRepository;

    public SearchService(ProductSearchRepository productSearchRepository, PostSearchRepository postSearchRepository) {
        this.productSearchRepository = productSearchRepository;
        this.postSearchRepository = postSearchRepository;
    }

    public SearchResult search(String query, Long categoryId) {
        List<ProductDocument> products;
        List<PostDocument> posts;

        if (categoryId != null) {
            products = productSearchRepository.findByCategoryId(categoryId);
            posts = postSearchRepository.findByCategoryId(categoryId);
        } else if (query != null && !query.isBlank()) {
            products = productSearchRepository.findByNameContainingIgnoreCase(query);
            posts = postSearchRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query);
        } else {
            products = toList(productSearchRepository.findAll());
            posts = toList(postSearchRepository.findAll());
        }

        List<SearchResult.ProductSearchHit> productHits = products.stream()
                .map(p -> new SearchResult.ProductSearchHit(p.getId(), p.getName(), p.getPrice(), p.getImg(), p.getCategoryId(), p.getCategoryName()))
                .toList();

        List<SearchResult.PostSearchHit> postHits = posts.stream()
                .map(p -> new SearchResult.PostSearchHit(p.getId(), p.getTitle(), p.getContent(), p.getImg(), p.getCategoryId(), p.getCategoryName(), p.getStatus()))
                .toList();

        long totalHits = productHits.size() + postHits.size();
        return new SearchResult(productHits, postHits, totalHits);
    }

    public void indexProduct(ProductDocument doc) {
        productSearchRepository.save(doc);
    }

    public void deleteProduct(Long id) {
        productSearchRepository.deleteById(id);
    }

    public void indexPost(PostDocument doc) {
        postSearchRepository.save(doc);
    }

    public void deletePost(Long id) {
        postSearchRepository.deleteById(id);
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
