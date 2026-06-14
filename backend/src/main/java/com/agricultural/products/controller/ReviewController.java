package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.Review;
import com.agricultural.products.service.ReviewService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public Result<List<Review>> listByProduct(@PathVariable Long productId) {
        return Result.success(reviewService.listByProduct(productId, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/user")
    public Result<List<Review>> listByUser() {
        return Result.success(reviewService.listByUser(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/can-review/{productId}")
    public Result<Map<String, Object>> canReview(
            @PathVariable Long productId,
            @RequestParam(required = false) Long orderId) {
        return Result.success(reviewService.canReview(SecurityUtils.getCurrentUserId(), productId, orderId));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Review review) {
        reviewService.add(SecurityUtils.getCurrentUserId(), review);
        return Result.success();
    }

    @PostMapping("/reply")
    public Result<Void> reply(@RequestBody Review review) {
        reviewService.reply(SecurityUtils.getCurrentUserId(), review);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Review review) {
        reviewService.update(SecurityUtils.getCurrentUserId(), id, review);
        return Result.success();
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error("无权限操作");
        }
        reviewService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(SecurityUtils.getCurrentUserId(), id);
        return Result.success();
    }

    @PostMapping("/like/{id}")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long id) {
        return Result.success(reviewService.toggleLike(SecurityUtils.getCurrentUserId(), id));
    }

    @GetMapping("/stats/{productId}")
    public Result<Map<String, Object>> getStats(@PathVariable Long productId) {
        return Result.success(reviewService.getStats(productId));
    }
}
