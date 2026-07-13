package com.example.productservice.dto;

import java.util.List;

public class ReorderRequest {

	private List<Item> items;

	public List<Item> getItems() { return items; }
	public void setItems(List<Item> items) { this.items = items; }

	public static class Item {
		private String id;
		private Integer sortOrder;

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public Integer getSortOrder() { return sortOrder; }
		public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
	}
}
