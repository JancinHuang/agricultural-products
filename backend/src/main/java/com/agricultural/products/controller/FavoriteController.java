package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.Favorite;
import com.agricultural.products.mapper.FavoriteMapper;
import com.agricultural.products.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ObjectStorageService objectStorageService;

    @GetMapping("/list")
    public Result<List<Favorite>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Favorite> favoriteList = favoriteMapper.findByUserId(userId);
        favoriteList.forEach(this::fillProductImageUrl);
        return Result.success(favoriteList);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Favorite favorite) {
        Long userId = SecurityUtils.getCurrentUserId();
        Favorite existFavorite = favoriteMapper.findByUserIdAndProductId(userId, favorite.getProductId());

        if (existFavorite == null) {
            favorite.setUserId(userId);
            favoriteMapper.insert(favorite);
        }
        return Result.success();
    }

    @DeleteMapping("/{productId}")
    public Result<Void> delete(@PathVariable Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        favoriteMapper.deleteByUserIdAndProductId(userId, productId);
        return Result.success();
    }

    @GetMapping("/check/{productId}")
    public Result<Map<String, Object>> check(@PathVariable Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Favorite favorite = favoriteMapper.findByUserIdAndProductId(userId, productId);

        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", favorite != null);
        if (favorite != null) {
            result.put("favoriteId", favorite.getId());
        }
        return Result.success(result);
    }

    @GetMapping("/count/{productId}")
    public Result<Integer> count(@PathVariable Long productId) {
        int count = favoriteMapper.countByProductId(productId);
        return Result.success(count);
    }

    private void fillProductImageUrl(Favorite favorite) {
        if (favorite != null) {
            favorite.setProductImage(objectStorageService.toAccessibleUrl(favorite.getProductImage()));
        }
    }
}
