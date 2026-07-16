package com.example.review_service.service;

import com.example.review_service.client.OrderClient;
import com.example.review_service.dto.*;
import com.example.review_service.entity.Review;
import com.example.review_service.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private static final Path UPLOAD_ROOT = Paths.get("uploads", "reviews");

    private final ReviewRepository reviewRepository;
    private final OrderClient orderClient;

    public ReviewService(ReviewRepository reviewRepository, OrderClient orderClient) {
        this.reviewRepository = reviewRepository;
        this.orderClient = orderClient;
    }

    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndIsActiveTrueOrderByCreatedAtDesc(productId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ReviewStatsResponse getProductStats(Long productId) {
        ReviewStatsResponse stats = new ReviewStatsResponse();
        stats.setTotalReviews(reviewRepository.countByProductId(productId));
        stats.setAverageRating(Math.round(reviewRepository.averageRatingByProductId(productId) * 10.0) / 10.0);

        Map<Integer, Long> dist = new HashMap<>();
        for (int i = 1; i <= 5; i++) dist.put(i, 0L);
        for (Object[] row : reviewRepository.ratingDistributionByProductId(productId)) {
            dist.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        stats.setDistribution(dist);
        return stats;
    }

    public Map<String, Object> getMyReview(Long userId, Long productId, Long orderId) {
        Optional<Review> opt = reviewRepository.findByUserIdAndProductIdAndOrderId(userId, productId, orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("reviewed", opt.isPresent());
        opt.ifPresent(r -> result.put("review", toResponse(r)));
        return result;
    }

    @Transactional
    public ReviewResponse createReview(Long userId, String userName, ReviewRequest req, MultipartFile[] files) {
        Map<String, Boolean> check = orderClient.hasPurchased(userId, req.getProductId());
        if (!check.getOrDefault("purchased", false)) {
            throw new RuntimeException("Bạn cần mua sản phẩm trước khi đánh giá");
        }
        if (reviewRepository.findByUserIdAndProductIdAndOrderId(userId, req.getProductId(), req.getOrderId()).isPresent()) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này cho đơn hàng này rồi");
        }

        Review review = new Review(userId, userName, req.getProductId(), req.getProductName(), req.getOrderId(), req.getRating(), req.getDescription());
        if (files != null && files.length > 0) {
            review.setImages(String.join(",", saveImages(files)));
        }
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse updateReview(Long id, Long userId, ReviewRequest req, MultipartFile[] files) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền sửa đánh giá này");
        }
        review.setRating(req.getRating());
        review.setDescription(req.getDescription());
        if (files != null && files.length > 0) {
            review.setImages(String.join(",", saveImages(files)));
        }
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này");
        }
        reviewRepository.delete(review);
    }

    @Transactional
    public ReviewResponse adminReply(Long id, String reply) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        review.setAdminReply(reply);
        review.setAdminRepliedAt(LocalDateTime.now());
        return toResponse(reviewRepository.save(review));
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String[] saveImages(MultipartFile[] files) {
        try {
            Files.createDirectories(UPLOAD_ROOT);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục upload");
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            try {
                Files.copy(file.getInputStream(), UPLOAD_ROOT.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                urls.add("/uploads/reviews/" + filename);
            } catch (IOException e) {
                log.warn("Failed to save review image: {}", e.getMessage());
            }
        }
        return urls.toArray(new String[0]);
    }

    private ReviewResponse toResponse(Review r) {
        ReviewResponse res = new ReviewResponse();
        res.setId(r.getId());
        res.setUserId(r.getUserId());
        res.setUserName(r.getUserName());
        res.setProductId(r.getProductId());
        res.setProductName(r.getProductName());
        res.setOrderId(r.getOrderId());
        res.setRating(r.getRating());
        res.setDescription(r.getDescription());
        res.setImages(r.getImages() != null ? r.getImages().split(",") : new String[0]);
        res.setAdminReply(r.getAdminReply());
        res.setAdminRepliedAt(r.getAdminRepliedAt());
        res.setCreatedAt(r.getCreatedAt());
        res.setUpdatedAt(r.getUpdatedAt());
        return res;
    }
}
