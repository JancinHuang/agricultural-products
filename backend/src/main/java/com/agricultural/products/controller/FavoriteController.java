package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.Favorite;
import com.agricultural.products.service.FavoriteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/list")
    public Result<List<Favorite>> list() {
        return Result.success(favoriteService.list(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Favorite favorite) {
        favoriteService.add(SecurityUtils.getCurrentUserId(), favorite);
        return Result.success();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> delete(@PathVariable Long productId) {
        favoriteService.delete(SecurityUtils.getCurrentUserId(), productId);
        return Result.success();
    }

    @GetMapping("/check/{productId}")
    public Result<Map<String, Object>> check(@PathVariable Long productId) {
        return Result.success(favoriteService.check(SecurityUtils.getCurrentUserId(), productId));
    }

    @GetMapping("/count/{productId}")
    public Result<Integer> count(@PathVariable Long productId) {
        return Result.success(favoriteService.count(productId));
    }
}
