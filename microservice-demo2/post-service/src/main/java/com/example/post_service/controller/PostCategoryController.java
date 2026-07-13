package com.example.post_service.controller;

import com.example.post_service.dto.PostCategoryRequest;
import com.example.post_service.dto.PostCategoryResponse;
import com.example.post_service.service.PostCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post-categories")
public class PostCategoryController {

    private final PostCategoryService postCategoryService;

    public PostCategoryController(PostCategoryService postCategoryService) {
        this.postCategoryService = postCategoryService;
    }

    @GetMapping
    public List<PostCategoryResponse> getAll() {
        return postCategoryService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostCategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postCategoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PostCategoryResponse> create(@RequestBody PostCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postCategoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostCategoryResponse> update(@PathVariable Long id, @RequestBody PostCategoryRequest request) {
        return ResponseEntity.ok(postCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
