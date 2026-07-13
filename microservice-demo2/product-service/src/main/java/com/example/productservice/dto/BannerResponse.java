package com.example.productservice.dto;

public class BannerResponse {

	private String id;
	private String title;
	private String subtitle;
	private String image;
	private String link;
	private Integer sortOrder;
	private boolean active;

	public BannerResponse() {
	}

	public BannerResponse(String id, String title, String subtitle, String image, String link, Integer sortOrder, boolean active) {
		this.id = id;
		this.title = title;
		this.subtitle = subtitle;
		this.image = image;
		this.link = link;
		this.sortOrder = sortOrder;
		this.active = active;
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getSubtitle() { return subtitle; }
	public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
	public String getImage() { return image; }
	public void setImage(String image) { this.image = image; }
	public String getLink() { return link; }
	public void setLink(String link) { this.link = link; }
	public Integer getSortOrder() { return sortOrder; }
	public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
}
