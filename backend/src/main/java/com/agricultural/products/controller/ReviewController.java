package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.Review;
import com.agricultural.products.mapper.ReviewMapper;
import com.agricultural.products.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ObjectStorageService objectStorageService;

    @GetMapping("/product/{productId}")
    public Result<List<Review>> listByProduct(@PathVariable Long productId) {
        List<Review> reviewList = reviewMapper.findByProductId(productId);
        Long currentUserId = SecurityUtils.getCurrentUserId();

        for (Review review : reviewList) {
            List<Review> replies = reviewMapper.findRepliesByParentId(review.getId());
            review.setReplies(replies);
            fillImageUrls(review);
            review.setIsLiked(currentUserId != null && reviewMapper.checkUserLiked(review.getId(), currentUserId) > 0);

            for (Review reply : replies) {
                fillImageUrls(reply);
                reply.setIsLiked(currentUserId != null && reviewMapper.checkUserLiked(reply.getId(), currentUserId) > 0);
            }
        }

        return Result.success(reviewList);
    }

    @GetMapping("/user")
    public Result<List<Review>> listByUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Review> reviewList = reviewMapper.findByUserId(userId);
        reviewList.forEach(this::fillImageUrls);
        return Result.success(reviewList);
    }

    @GetMapping("/can-review/{productId}")
    public Result<Map<String, Object>> canReview(
            @PathVariable Long productId,
            @RequestParam(required = false) Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long reviewOrderId = null;
        boolean hasCompletedPurchase = false;
        boolean hasReviewed = false;

        if (userId != null) {
            if (orderId != null) {
                hasCompletedPurchase = reviewMapper.countCompletedOrderItem(userId, productId, orderId) > 0;
                hasReviewed = reviewMapper.countUserOrderProductReviews(userId, productId, orderId) > 0;
                reviewOrderId = orderId;
            } else {
                hasCompletedPurchase = reviewMapper.countCompletedPurchase(userId, productId) > 0;
                reviewOrderId = reviewMapper.findFirstReviewableOrderId(userId, productId);
                hasReviewed = hasCompletedPurchase && reviewOrderId == null;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("canReview", hasCompletedPurchase && !hasReviewed);
        result.put("hasCompletedPurchase", hasCompletedPurchase);
        result.put("hasReviewed", hasReviewed);
        result.put("orderId", reviewOrderId);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Review review) {
        Long userId = SecurityUtils.getCurrentUserId();
        review.setUserId(userId);

        if (review.getProductId() == null) {
            return Result.error("商品不能为空");
        }
        if (review.getOrderId() == null) {
            review.setOrderId(reviewMapper.findFirstReviewableOrderId(userId, review.getProductId()));
        }
        if (!hasCompletedPurchase(userId, review.getProductId(), review.getOrderId())) {
            return Result.error("购买并完成订单后才能发表评价");
        }
        if (reviewMapper.countUserOrderProductReviews(userId, review.getProductId(), review.getOrderId()) > 0) {
            return Result.error("该订单中的商品已评价，不能重复评价");
        }
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            return Result.error("评价内容不能为空");
        }
        if (review.getContent().length() > 500) {
            return Result.error("评价内容不能超过500字");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return Result.error("评分必须在1-5之间");
        }

        reviewMapper.insert(review);
        return Result.success();
    }

    @PostMapping("/reply")
    public Result<Void> reply(@RequestBody Review review) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (review.getParentId() == null) {
            return Result.error("回复的评论ID不能为空");
        }

        Review parentReview = reviewMapper.findById(review.getParentId());
        if (parentReview == null) {
            return Result.error("原评论不存在");
        }

        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            return Result.error("回复内容不能为空");
        }
        if (review.getContent().length() > 500) {
            return Result.error("回复内容不能超过500字");
        }

        review.setUserId(userId);
        review.setProductId(parentReview.getProductId());
        review.setRating(0);
        reviewMapper.insert(review);

        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Review review) {
        Long userId = SecurityUtils.getCurrentUserId();
        Review existReview = reviewMapper.findById(id);
        if (existReview == null) {
            return Result.error("评论不存在");
        }
        if (!existReview.getUserId().equals(userId)) {
            return Result.error("无权修改此评论");
        }

        if (review.getContent() != null && review.getContent().length() > 500) {
            return Result.error("评价内容不能超过500字");
        }
        if (review.getRating() != null && (review.getRating() < 1 || review.getRating() > 5)) {
            return Result.error("评分必须在1-5之间");
        }

        reviewMapper.updateContent(id, review.getRating(), review.getContent(), review.getImages());
        return Result.success();
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error("无权限操作");
        }
        reviewMapper.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Review existReview = reviewMapper.findById(id);
        if (existReview == null) {
            return Result.error("评论不存在");
        }
        if (!existReview.getUserId().equals(userId)) {
            return Result.error("无权删除此评论");
        }
        reviewMapper.deleteById(id);
        return Result.success();
    }

    @PostMapping("/like/{id}")
    @Transactional
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Review review = reviewMapper.findById(id);
        if (review == null) {
            return Result.error("评论不存在");
        }

        int isLiked = reviewMapper.checkUserLiked(id, userId);
        Map<String, Object> result = new HashMap<>();

        if (isLiked > 0) {
            reviewMapper.deleteLike(id, userId);
            reviewMapper.updateLikeCount(id, -1);
            result.put("isLiked", false);
            result.put("likeCount", review.getLikeCount() - 1);
        } else {
            reviewMapper.insertLike(id, userId);
            reviewMapper.updateLikeCount(id, 1);
            result.put("isLiked", true);
            result.put("likeCount", review.getLikeCount() + 1);
        }

        return Result.success(result);
    }

    @GetMapping("/stats/{productId}")
    public Result<Map<String, Object>> getStats(@PathVariable Long productId) {
        Double avgRating = reviewMapper.getAverageRating(productId);
        int count = reviewMapper.countByProductId(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("avgRating", avgRating != null ? avgRating : 0.0);
        result.put("count", count);
        return Result.success(result);
    }

    private boolean hasCompletedPurchase(Long userId, Long productId, Long orderId) {
        if (userId == null || productId == null || orderId == null) {
            return false;
        }
        return reviewMapper.countCompletedOrderItem(userId, productId, orderId) > 0;
    }

    private void fillImageUrls(Review review) {
        if (review == null) {
            return;
        }
        review.setImageUrls(toAccessibleImageCsv(review.getImages()));
        review.setProductImage(objectStorageService.toAccessibleUrl(review.getProductImage()));
    }

    private String toAccessibleImageCsv(String images) {
        if (images == null || images.isBlank()) {
            return images;
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(image -> !image.isEmpty())
                .map(objectStorageService::toAccessibleUrl)
                .collect(Collectors.joining(","));
    }
}
