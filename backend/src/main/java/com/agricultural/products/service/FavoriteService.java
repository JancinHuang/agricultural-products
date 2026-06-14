package com.agricultural.products.service;

import com.agricultural.products.entity.Favorite;

import java.util.List;
import java.util.Map;

public interface FavoriteService {

    List<Favorite> list(Long userId);

    void add(Long userId, Favorite favorite);

    void delete(Long userId, Long productId);

    Map<String, Object> check(Long userId, Long productId);

    int count(Long productId);
}
