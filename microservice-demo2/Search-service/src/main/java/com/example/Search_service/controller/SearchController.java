package com.example.Search_service.controller;

import com.example.Search_service.document.PostDocument;
import com.example.Search_service.document.ProductDocument;
import com.example.Search_service.dto.SearchResult;
import com.example.Search_service.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<SearchResult> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(searchService.search(q, categoryId));
    }

    @PostMapping("/index/product")
    public ResponseEntity<Void> indexProduct(@RequestBody ProductDocument doc) {
        searchService.indexProduct(doc);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/index/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        searchService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/index/post")
    public ResponseEntity<Void> indexPost(@RequestBody PostDocument doc) {
        searchService.indexPost(doc);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/index/post/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        searchService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
