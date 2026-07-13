package com.example.productservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "banners")
public class Banner {

	@Id
	private String id;
	private String title;
	private String subtitle;
	private String image;
	private String link;
	private Integer sortOrder;
	private boolean active = true;

	public Banner() {
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
