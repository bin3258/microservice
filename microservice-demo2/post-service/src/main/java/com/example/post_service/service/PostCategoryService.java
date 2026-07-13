package com.example.post_service.service;

import com.example.post_service.dto.PostCategoryRequest;
import com.example.post_service.dto.PostCategoryResponse;
import com.example.post_service.entity.PostCategory;
import com.example.post_service.repository.PostCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PostCategoryService {

    private final PostCategoryRepository repository;

    public PostCategoryService(PostCategoryRepository repository) {
        this.repository = repository;
    }

    public List<PostCategoryResponse> getAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PostCategoryResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục bài viết"));
    }

    public PostCategoryResponse create(PostCategoryRequest request) {
        PostCategory entity = new PostCategory();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        return toResponse(repository.save(entity));
    }

    public PostCategoryResponse update(Long id, PostCategoryRequest request) {
        PostCategory entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục bài viết"));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục bài viết");
        }
        repository.deleteById(id);
    }

    private PostCategoryResponse toResponse(PostCategory entity) {
        return new PostCategoryResponse(entity.getId(), entity.getName(), entity.getDescription());
    }
}
