package com.example.review_service.dto;

import java.util.Map;

public class ReviewStatsResponse {

    private long totalReviews;
    private double averageRating;
    private Map<Integer, Long> distribution;

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public Map<Integer, Long> getDistribution() { return distribution; }
    public void setDistribution(Map<Integer, Long> distribution) { this.distribution = distribution; }
}
