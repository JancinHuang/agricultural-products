package com.agricultural.products.service;

import com.agricultural.products.entity.Category;
import com.agricultural.products.entity.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Redis cache middleware used by high-frequency mall read APIs.
 */
@Service
public class RedisCacheService {

    private static final Duration SHORT_CACHE_TTL = Duration.ofMinutes(10);
    private static final String CATEGORY_LIST_KEY = "mall:category:list";
    private static final String HOT_PRODUCTS_KEY_PREFIX = "mall:product:hot:";

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public RedisCacheService(ObjectMapper objectMapper, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    public Optional<List<Category>> getCategoryList() {
        return readList(CATEGORY_LIST_KEY, new TypeReference<List<Category>>() {});
    }

    public void setCategoryList(List<Category> categories) {
        write(CATEGORY_LIST_KEY, categories);
    }

    public Optional<List<Product>> getHotProducts(int limit) {
        return readList(HOT_PRODUCTS_KEY_PREFIX + limit, new TypeReference<List<Product>>() {});
    }

    public void setHotProducts(int limit, List<Product> products) {
        write(HOT_PRODUCTS_KEY_PREFIX + limit, products);
    }

    public void evictCategoryList() {
        delete(CATEGORY_LIST_KEY);
    }

    public void evictProductCaches() {
        if (redisTemplate == null) {
            return;
        }
        try {
            var keys = redisTemplate.keys(HOT_PRODUCTS_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ignored) {
            // Redis is optional for local course demos; database reads remain authoritative.
        }
    }

    private <T> Optional<T> readList(String key, TypeReference<T> typeReference) {
        if (redisTemplate == null) {
            return Optional.empty();
        }
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, typeReference));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private void write(String key, Object value) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), SHORT_CACHE_TTL);
        } catch (Exception ignored) {
            // Cache failures must not affect shopping or admin flows.
        }
    }

    private void delete(String key) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
            // Ignore cache eviction failures in local demo mode.
        }
    }
}
