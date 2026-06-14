package com.agricultural.products.service.impl;

import com.agricultural.products.entity.Favorite;
import com.agricultural.products.mapper.FavoriteMapper;
import com.agricultural.products.service.FavoriteService;
import com.agricultural.products.service.ObjectStorageService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ObjectStorageService objectStorageService;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper, ObjectStorageService objectStorageService) {
        this.favoriteMapper = favoriteMapper;
        this.objectStorageService = objectStorageService;
    }

    @Override
    public List<Favorite> list(Long userId) {
        List<Favorite> favoriteList = favoriteMapper.findByUserId(userId);
        favoriteList.forEach(this::fillProductImageUrl);
        return favoriteList;
    }

    @Override
    public void add(Long userId, Favorite favorite) {
        Favorite existFavorite = favoriteMapper.findByUserIdAndProductId(userId, favorite.getProductId());
        if (existFavorite != null) {
            return;
        }

        favorite.setUserId(userId);
        favoriteMapper.insert(favorite);
    }

    @Override
    public void delete(Long userId, Long productId) {
        favoriteMapper.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public Map<String, Object> check(Long userId, Long productId) {
        Favorite favorite = favoriteMapper.findByUserIdAndProductId(userId, productId);

        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", favorite != null);
        if (favorite != null) {
            result.put("favoriteId", favorite.getId());
        }
        return result;
    }

    @Override
    public int count(Long productId) {
        return favoriteMapper.countByProductId(productId);
    }

    private void fillProductImageUrl(Favorite favorite) {
        if (favorite != null) {
            favorite.setProductImage(objectStorageService.toAccessibleUrl(favorite.getProductImage()));
        }
    }
}
