package com.example.shared.messaging;

public class PostCreatedEvent {

	private String action;
	private Long postId;
	private String title;
	private Long categoryId;
	private String categoryName;
	private String status;
	private String img;

	public PostCreatedEvent() {
	}

	public PostCreatedEvent(String action, Long postId, String title, Long categoryId, String categoryName, String status, String img) {
		this.action = action;
		this.postId = postId;
		this.title = title;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.status = status;
		this.img = img;
	}

	public PostCreatedEvent(Long postId, String title, Long categoryId, String categoryName, String status, String img) {
		this(null, postId, title, categoryId, categoryName, status, img);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}
}
