package com.example.productservice.dto;

public class BannerDTO {

	private String title;
	private String subtitle;
	private String link;
	private Integer sortOrder;
	private boolean active = true;

	public BannerDTO() {
	}

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getSubtitle() { return subtitle; }
	public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
	public String getLink() { return link; }
	public void setLink(String link) { this.link = link; }
	public Integer getSortOrder() { return sortOrder; }
	public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
}
