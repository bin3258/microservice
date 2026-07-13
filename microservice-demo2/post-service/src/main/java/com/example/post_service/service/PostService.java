package com.example.post_service.service;

import com.example.post_service.dto.PostRequest;
import com.example.post_service.dto.PostResponse;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.PostCategory;
import com.example.post_service.messaging.PostEventPublisher;
import com.example.post_service.repository.PostCategoryRepository;
import com.example.post_service.repository.PostRepository;
import com.example.post_service.storage.LocalFileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PostService {

	private final PostRepository postRepository;
	private final PostCategoryRepository postCategoryRepository;
	private final PostEventPublisher postEventPublisher;
	private final LocalFileStorageService localFileStorageService;

	public PostService(PostRepository postRepository, PostCategoryRepository postCategoryRepository, PostEventPublisher postEventPublisher, LocalFileStorageService localFileStorageService) {
		this.postRepository = postRepository;
		this.postCategoryRepository = postCategoryRepository;
		this.postEventPublisher = postEventPublisher;
		this.localFileStorageService = localFileStorageService;
	}

	public List<PostResponse> getAllPosts() {
		return postRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	public PostResponse getPostById(Long id) {
		return postRepository.findById(id)
				.map(this::toResponse)
				.orElse(null);
	}

	public List<PostResponse> getPostsByCategory(Long categoryId) {
		return postRepository.findByCategoryId(categoryId).stream()
				.map(this::toResponse)
				.toList();
	}

	public PostResponse createPost(PostRequest request) {
		validateRequest(request);
		PostCategory category = getCategoryOrThrow(request.getCategoryId());
		Post post = new Post();
		post.setTitle(request.getTitle());
		post.setContent(request.getContent());
		post.setImg(resolveImagePath(request, null));
		post.setCategoryId(category.getId());
		post.setStatus(normalizeStatus(request.getStatus()));
		Post savedPost = postRepository.save(post);
		postEventPublisher.publishPostChanged("CREATED", savedPost.getId(), savedPost.getTitle(), savedPost.getCategoryId(), category.getName(), savedPost.getStatus(), savedPost.getImg());
		return toResponse(savedPost);
	}

	public PostResponse updatePost(Long id, PostRequest request) {
		validateRequest(request);
		Post post = postRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết với mã " + id));
		PostCategory category = getCategoryOrThrow(request.getCategoryId());
		post.setTitle(request.getTitle());
		post.setContent(request.getContent());
		post.setImg(resolveImagePath(request, post.getImg()));
		post.setCategoryId(category.getId());
		post.setStatus(normalizeStatus(request.getStatus()));
		Post savedPost = postRepository.save(post);
		postEventPublisher.publishPostChanged("UPDATED", savedPost.getId(), savedPost.getTitle(), savedPost.getCategoryId(), category.getName(), savedPost.getStatus(), savedPost.getImg());
		return toResponse(savedPost);
	}

	public void deletePost(Long id) {
		if (!postRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết với mã " + id);
		}
		postRepository.deleteById(id);
	}

	private PostResponse toResponse(Post post) {
		String categoryName = null;
		if (post.getCategoryId() != null) {
			categoryName = postCategoryRepository.findById(post.getCategoryId())
					.map(PostCategory::getName)
					.orElse(null);
		}
		return new PostResponse(
				post.getId(),
				post.getTitle(),
				post.getContent(),
				post.getImg(),
				post.getCategoryId(),
				categoryName,
				post.getStatus()
		);
	}

	private void validateRequest(PostRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu bài viết không được để trống");
		}
		if (request.getTitle() == null || request.getTitle().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập tiêu đề bài viết");
		}
		if (request.getContent() == null || request.getContent().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng nhập nội dung bài viết");
		}
		if (request.getCategoryId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng cung cấp categoryId");
		}
	}

	private String resolveImagePath(PostRequest request, String currentPath) {
		if (request.getImgFile() != null && !request.getImgFile().isEmpty()) {
			return localFileStorageService.saveImage(request.getImgFile());
		}
		if (request.getImg() != null && !request.getImg().isBlank()) {
			return request.getImg();
		}
		return currentPath;
	}

	private PostCategory getCategoryOrThrow(Long categoryId) {
		return postCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Danh mục bài viết không tồn tại với mã " + categoryId));
	}

	private String normalizeStatus(String status) {
		if (status == null || status.isBlank()) {
			return "PUBLISHED";
		}
		return status.trim().toUpperCase();
	}
}
