package com.example.shared.messaging;

public class CategoryChangedEvent {

	private String action;
	private Long categoryId;
	private String categoryName;
	private String description;

	public CategoryChangedEvent() {
	}

	public CategoryChangedEvent(String action, Long categoryId, String categoryName, String description) {
		this.action = action;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.description = description;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
