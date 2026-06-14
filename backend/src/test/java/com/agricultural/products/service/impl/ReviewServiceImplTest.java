package com.agricultural.products.service.impl;

import com.agricultural.products.common.BusinessException;
import com.agricultural.products.entity.Review;
import com.agricultural.products.mapper.ReviewMapper;
import com.agricultural.products.service.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ObjectStorageService objectStorageService;

    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewMapper, objectStorageService);
    }

    @Test
    void listByProductLoadsRepliesAndLikeState() {
        Review review = new Review();
        review.setId(5L);
        review.setImages("a.png,b.png");
        review.setProductImage("product.png");
        Review reply = new Review();
        reply.setId(6L);
        reply.setProductImage("reply-product.png");

        when(reviewMapper.findByProductId(3L)).thenReturn(List.of(review));
        when(reviewMapper.findRepliesByParentId(5L)).thenReturn(List.of(reply));
        when(reviewMapper.checkUserLiked(5L, 2L)).thenReturn(1);
        when(reviewMapper.checkUserLiked(6L, 2L)).thenReturn(0);
        when(objectStorageService.toAccessibleUrl("a.png")).thenReturn("cdn/a.png");
        when(objectStorageService.toAccessibleUrl("b.png")).thenReturn("cdn/b.png");
        when(objectStorageService.toAccessibleUrl("product.png")).thenReturn("cdn/product.png");
        when(objectStorageService.toAccessibleUrl("reply-product.png")).thenReturn("cdn/reply-product.png");

        List<Review> result = reviewService.listByProduct(3L, 2L);

        assertTrue(result.get(0).getIsLiked());
        assertFalse(result.get(0).getReplies().get(0).getIsLiked());
        assertEquals("cdn/a.png,cdn/b.png", result.get(0).getImageUrls());
    }

    @Test
    void addRejectsWhenPurchaseNotCompleted() {
        Review review = review(3L, 100L, 5, "很好吃");
        when(reviewMapper.countCompletedOrderItem(2L, 3L, 100L)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> reviewService.add(2L, review));

        assertEquals(400, exception.getCode());
        verify(reviewMapper, never()).insert(review);
    }

    @Test
    void addRejectsDuplicateOrderProductReview() {
        Review review = review(3L, 100L, 5, "很好吃");
        when(reviewMapper.countCompletedOrderItem(2L, 3L, 100L)).thenReturn(1);
        when(reviewMapper.countUserOrderProductReviews(2L, 3L, 100L)).thenReturn(1);

        assertThrows(BusinessException.class, () -> reviewService.add(2L, review));

        verify(reviewMapper, never()).insert(review);
    }

    @Test
    void addRejectsInvalidRating() {
        Review review = review(3L, 100L, 6, "很好吃");
        when(reviewMapper.countCompletedOrderItem(2L, 3L, 100L)).thenReturn(1);
        when(reviewMapper.countUserOrderProductReviews(2L, 3L, 100L)).thenReturn(0);

        assertThrows(BusinessException.class, () -> reviewService.add(2L, review));

        verify(reviewMapper, never()).insert(review);
    }

    @Test
    void addInsertsValidReview() {
        Review review = review(3L, 100L, 5, "很好吃");
        when(reviewMapper.countCompletedOrderItem(2L, 3L, 100L)).thenReturn(1);
        when(reviewMapper.countUserOrderProductReviews(2L, 3L, 100L)).thenReturn(0);

        reviewService.add(2L, review);

        assertEquals(2L, review.getUserId());
        verify(reviewMapper).insert(review);
    }

    @Test
    void deleteRejectsOtherUsersReview() {
        Review existing = new Review();
        existing.setUserId(9L);
        when(reviewMapper.findById(5L)).thenReturn(existing);

        assertThrows(BusinessException.class, () -> reviewService.delete(2L, 5L));

        verify(reviewMapper, never()).deleteById(5L);
    }

    @Test
    void toggleLikeAddsLikeWhenNotLiked() {
        Review existing = new Review();
        existing.setLikeCount(4);
        when(reviewMapper.findById(5L)).thenReturn(existing);
        when(reviewMapper.checkUserLiked(5L, 2L)).thenReturn(0);

        Map<String, Object> result = reviewService.toggleLike(2L, 5L);

        assertTrue((Boolean) result.get("isLiked"));
        assertEquals(5, result.get("likeCount"));
        verify(reviewMapper).insertLike(5L, 2L);
        verify(reviewMapper).updateLikeCount(5L, 1);
    }

    @Test
    void toggleLikeRemovesLikeWhenAlreadyLiked() {
        Review existing = new Review();
        existing.setLikeCount(4);
        when(reviewMapper.findById(5L)).thenReturn(existing);
        when(reviewMapper.checkUserLiked(5L, 2L)).thenReturn(1);

        Map<String, Object> result = reviewService.toggleLike(2L, 5L);

        assertFalse((Boolean) result.get("isLiked"));
        assertEquals(3, result.get("likeCount"));
        verify(reviewMapper).deleteLike(5L, 2L);
        verify(reviewMapper).updateLikeCount(5L, -1);
    }

    private Review review(Long productId, Long orderId, Integer rating, String content) {
        Review review = new Review();
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(content);
        return review;
    }
}
