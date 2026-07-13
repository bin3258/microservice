package com.example.orderservice.payload;

public class ProductInfo {

	private Long id;
	private String name;
	private Double price;
	private Double salePrice;
	private String img;

	public ProductInfo() {
	}

	public ProductInfo(Long id, String name, Double price, Double salePrice, String img) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.salePrice = salePrice;
		this.img = img;
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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(Double salePrice) {
		this.salePrice = salePrice;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}
}
