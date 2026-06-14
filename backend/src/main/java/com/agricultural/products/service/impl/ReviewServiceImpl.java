package com.agricultural.products.service.impl;

import com.agricultural.products.common.BusinessException;
import com.agricultural.products.entity.Review;
import com.agricultural.products.mapper.ReviewMapper;
import com.agricultural.products.service.ObjectStorageService;
import com.agricultural.products.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final ObjectStorageService objectStorageService;

    public ReviewServiceImpl(ReviewMapper reviewMapper, ObjectStorageService objectStorageService) {
        this.reviewMapper = reviewMapper;
        this.objectStorageService = objectStorageService;
    }

    @Override
    public List<Review> listByProduct(Long productId, Long currentUserId) {
        List<Review> reviewList = reviewMapper.findByProductId(productId);

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

        return reviewList;
    }

    @Override
    public List<Review> listByUser(Long userId) {
        List<Review> reviewList = reviewMapper.findByUserId(userId);
        reviewList.forEach(this::fillImageUrls);
        return reviewList;
    }

    @Override
    public Map<String, Object> canReview(Long userId, Long productId, Long orderId) {
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
        return result;
    }

    @Override
    public void add(Long userId, Review review) {
        review.setUserId(userId);

        if (review.getProductId() == null) {
            throw new BusinessException("商品不能为空");
        }
        if (review.getOrderId() == null) {
            review.setOrderId(reviewMapper.findFirstReviewableOrderId(userId, review.getProductId()));
        }
        if (!hasCompletedPurchase(userId, review.getProductId(), review.getOrderId())) {
            throw new BusinessException("购买并完成订单后才能发表评价");
        }
        if (reviewMapper.countUserOrderProductReviews(userId, review.getProductId(), review.getOrderId()) > 0) {
            throw new BusinessException("该订单中的商品已评价，不能重复评价");
        }
        validateContent(review.getContent(), "评价内容");
        validateRating(review.getRating());

        reviewMapper.insert(review);
    }

    @Override
    public void reply(Long userId, Review review) {
        if (review.getParentId() == null) {
            throw new BusinessException("回复的评论ID不能为空");
        }

        Review parentReview = reviewMapper.findById(review.getParentId());
        if (parentReview == null) {
            throw new BusinessException("原评论不存在");
        }

        validateContent(review.getContent(), "回复内容");

        review.setUserId(userId);
        review.setProductId(parentReview.getProductId());
        review.setRating(0);
        reviewMapper.insert(review);
    }

    @Override
    public void update(Long userId, Long id, Review review) {
        Review existReview = reviewMapper.findById(id);
        if (existReview == null) {
            throw new BusinessException("评论不存在");
        }
        if (!existReview.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此评论");
        }

        if (review.getContent() != null && review.getContent().length() > 500) {
            throw new BusinessException("评价内容不能超过500字");
        }
        if (review.getRating() != null) {
            validateRating(review.getRating());
        }

        reviewMapper.updateContent(id, review.getRating(), review.getContent(), review.getImages());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        reviewMapper.updateStatus(id, status);
    }

    @Override
    public void delete(Long userId, Long id) {
        Review existReview = reviewMapper.findById(id);
        if (existReview == null) {
            throw new BusinessException("评论不存在");
        }
        if (!existReview.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }
        reviewMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long id) {
        Review review = reviewMapper.findById(id);
        if (review == null) {
            throw new BusinessException("评论不存在");
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

        return result;
    }

    @Override
    public Map<String, Object> getStats(Long productId) {
        Double avgRating = reviewMapper.getAverageRating(productId);
        int count = reviewMapper.countByProductId(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("avgRating", avgRating != null ? avgRating : 0.0);
        result.put("count", count);
        return result;
    }

    private boolean hasCompletedPurchase(Long userId, Long productId, Long orderId) {
        if (userId == null || productId == null || orderId == null) {
            return false;
        }
        return reviewMapper.countCompletedOrderItem(userId, productId, orderId) > 0;
    }

    private void validateContent(String content, String fieldName) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(fieldName + "不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException(fieldName + "不能超过500字");
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }
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
