package com.example.Search_service.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products")
public class ProductDocument {
    @Id
    private Long id;
    private String name;
    private Double price;
    private String img;

    @Field(type = FieldType.Long)
    private Long categoryId;
    private String categoryName;

    public ProductDocument() {}

    public ProductDocument(Long id, String name, Double price, String img, Long categoryId, String categoryName) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.img = img;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
