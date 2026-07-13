package com.example.post_service.dto;

public class PostResponse {

	private Long id;
	private String title;
	private String content;
	private String img;
	private Long categoryId;
	private String categoryName;
	private String status;

	public PostResponse() {
	}

	public PostResponse(Long id, String title, String content, String img, Long categoryId, String categoryName, String status) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.img = img;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
