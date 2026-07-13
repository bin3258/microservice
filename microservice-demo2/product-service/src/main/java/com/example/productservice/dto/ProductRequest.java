package com.example.productservice.dto;

import org.springframework.web.multipart.MultipartFile;

public class ProductRequest {

	private String name;
	private Double price;
	private String img;
	private MultipartFile imgFile;
	private Long categoryId;
	private String description;
	private String ram;
	private String storage;
	private String screenResolution;
	private String screenTechnology;
	private String battery;
	private String color;
	private Double salePrice;

	public ProductRequest() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public MultipartFile getImgFile() {
		return imgFile;
	}

	public void setImgFile(MultipartFile imgFile) {
		this.imgFile = imgFile;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getRam() { return ram; }
	public void setRam(String ram) { this.ram = ram; }
	public String getStorage() { return storage; }
	public void setStorage(String storage) { this.storage = storage; }
	public String getScreenResolution() { return screenResolution; }
	public void setScreenResolution(String screenResolution) { this.screenResolution = screenResolution; }
	public String getScreenTechnology() { return screenTechnology; }
	public void setScreenTechnology(String screenTechnology) { this.screenTechnology = screenTechnology; }
	public String getBattery() { return battery; }
	public void setBattery(String battery) { this.battery = battery; }
	public String getColor() { return color; }
	public void setColor(String color) { this.color = color; }
	public Double getSalePrice() { return salePrice; }
	public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }
}
