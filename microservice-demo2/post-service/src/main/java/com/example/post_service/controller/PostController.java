package com.example.post_service.controller;

import com.example.post_service.dto.PostRequest;
import com.example.post_service.dto.PostResponse;
import com.example.post_service.service.PostService;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	public List<PostResponse> getAllPosts() {
		return postService.getAllPosts();
	}

	@GetMapping("/{id}")
	public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
		PostResponse response = postService.getPostById(id);
		if (response == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/category/{categoryId}")
	public List<PostResponse> getPostsByCategory(@PathVariable Long categoryId) {
		return postService.getPostsByCategory(categoryId);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<PostResponse> createPost(@ModelAttribute PostRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @ModelAttribute PostRequest request) {
		return ResponseEntity.ok(postService.updatePost(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePost(@PathVariable Long id) {
		postService.deletePost(id);
		return ResponseEntity.noContent().build();
	}
}
