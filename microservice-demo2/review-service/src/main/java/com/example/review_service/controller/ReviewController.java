package com.example.review_service.controller;

import com.example.review_service.dto.*;
import com.example.review_service.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public List<ReviewResponse> getProductReviews(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
    }

    @GetMapping("/product/{productId}/stats")
    public ReviewStatsResponse getProductStats(@PathVariable Long productId) {
        return reviewService.getProductStats(productId);
    }

    @GetMapping("/my")
    public Map<String, Object> getMyReview(HttpServletRequest request,
                                            @RequestParam Long userId,
                                            @RequestParam Long productId,
                                            @RequestParam Long orderId) {
        Long currentUserId = getCurrentUserId(request);
        if (!currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập");
        }
        return reviewService.getMyReview(userId, productId, orderId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponse> createReview(
            HttpServletRequest request,
            @RequestParam("productId") Long productId,
            @RequestParam("orderId") Long orderId,
            @RequestParam("rating") Integer rating,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        Long userId = getCurrentUserId(request);
        String userName = getUserName(request);
        ReviewRequest req = new ReviewRequest();
        req.setProductId(productId);
        req.setOrderId(orderId);
        req.setRating(rating);
        req.setDescription(description);
        req.setProductName(productName);
        try {
            ReviewResponse res = reviewService.createReview(userId, userName, req, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewResponse> updateReview(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam("rating") Integer rating,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        Long userId = getCurrentUserId(request);
        ReviewRequest req = new ReviewRequest();
        req.setRating(rating);
        req.setDescription(description);
        try {
            return ResponseEntity.ok(reviewService.updateReview(id, userId, req, files));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(HttpServletRequest request, @PathVariable Long id) {
        Long userId = getCurrentUserId(request);
        try {
            reviewService.deleteReview(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}/reply")
    public ResponseEntity<ReviewResponse> adminReply(HttpServletRequest request,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody AdminReplyRequest req) {
        requireAdmin(request);
        return ResponseEntity.ok(reviewService.adminReply(id, req.getAdminReply()));
    }

    @GetMapping("/admin/all")
    public List<ReviewResponse> getAllReviews(HttpServletRequest request) {
        requireAdmin(request);
        return reviewService.getAllReviews();
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        if (header != null) return Long.parseLong(header);
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng");
    }

    private String getUserName(HttpServletRequest request) {
        String header = request.getHeader("X-Username");
        return header != null ? header : "";
    }

    private void requireAdmin(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ ADMIN/MANAGER mới có quyền thực hiện thao tác này");
        }
    }
}
