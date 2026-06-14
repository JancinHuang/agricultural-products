package com.agricultural.products.service;

import com.agricultural.products.entity.Review;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    List<Review> listByProduct(Long productId, Long currentUserId);

    List<Review> listByUser(Long userId);

    Map<String, Object> canReview(Long userId, Long productId, Long orderId);

    void add(Long userId, Review review);

    void reply(Long userId, Review review);

    void update(Long userId, Long id, Review review);

    void updateStatus(Long id, Integer status);

    void delete(Long userId, Long id);

    Map<String, Object> toggleLike(Long userId, Long id);

    Map<String, Object> getStats(Long productId);
}
