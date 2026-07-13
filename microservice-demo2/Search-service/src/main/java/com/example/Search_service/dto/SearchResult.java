package com.example.Search_service.dto;

import java.util.List;

public class SearchResult {
    private List<ProductSearchHit> products;
    private List<PostSearchHit> posts;
    private long totalHits;

    public SearchResult() {}

    public SearchResult(List<ProductSearchHit> products, List<PostSearchHit> posts, long totalHits) {
        this.products = products;
        this.posts = posts;
        this.totalHits = totalHits;
    }

    public List<ProductSearchHit> getProducts() { return products; }
    public void setProducts(List<ProductSearchHit> products) { this.products = products; }
    public List<PostSearchHit> getPosts() { return posts; }
    public void setPosts(List<PostSearchHit> posts) { this.posts = posts; }
    public long getTotalHits() { return totalHits; }
    public void setTotalHits(long totalHits) { this.totalHits = totalHits; }

    public static class ProductSearchHit {
        private Long id;
        private String name;
        private Double price;
        private String img;
        private Long categoryId;
        private String categoryName;
        private String type = "product";

        public ProductSearchHit() {}

        public ProductSearchHit(Long id, String name, Double price, String img, Long categoryId, String categoryName) {
            this.id = id; this.name = name; this.price = price; this.img = img;
            this.categoryId = categoryId; this.categoryName = categoryName;
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
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class PostSearchHit {
        private Long id;
        private String title;
        private String content;
        private String img;
        private Long categoryId;
        private String categoryName;
        private String status;
        private String type = "post";

        public PostSearchHit() {}

        public PostSearchHit(Long id, String title, String content, String img, Long categoryId, String categoryName, String status) {
            this.id = id; this.title = title; this.content = content; this.img = img;
            this.categoryId = categoryId; this.categoryName = categoryName; this.status = status;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImg() { return img; }
        public void setImg(String img) { this.img = img; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
