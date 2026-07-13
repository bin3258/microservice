package com.example.category_service.dto;

import java.util.List;

public class CategoryResponse {

	private Long id;
	private String name;
	private String description;
	private Long parentId;
	private List<CategoryResponse> children;

	public CategoryResponse() {
	}

	public CategoryResponse(Long id, String name, String description, Long parentId) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.parentId = parentId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public List<CategoryResponse> getChildren() {
		return children;
	}

	public void setChildren(List<CategoryResponse> children) {
		this.children = children;
	}
}
