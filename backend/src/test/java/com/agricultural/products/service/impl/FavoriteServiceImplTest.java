package com.agricultural.products.service.impl;

import com.agricultural.products.entity.Favorite;
import com.agricultural.products.mapper.FavoriteMapper;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private ObjectStorageService objectStorageService;

    private FavoriteServiceImpl favoriteService;

    @BeforeEach
    void setUp() {
        favoriteService = new FavoriteServiceImpl(favoriteMapper, objectStorageService);
    }

    @Test
    void listSignsProductImages() {
        Favorite favorite = new Favorite();
        favorite.setProductImage("apple.png");

        when(favoriteMapper.findByUserId(2L)).thenReturn(List.of(favorite));
        when(objectStorageService.toAccessibleUrl("apple.png")).thenReturn("https://cdn.example/apple.png");

        List<Favorite> result = favoriteService.list(2L);

        assertEquals("https://cdn.example/apple.png", result.get(0).getProductImage());
    }

    @Test
    void addInsertsWhenFavoriteDoesNotExist() {
        Favorite favorite = new Favorite();
        favorite.setProductId(3L);
        favorite.setUserId(999L);

        when(favoriteMapper.findByUserIdAndProductId(2L, 3L)).thenReturn(null);

        favoriteService.add(2L, favorite);

        assertEquals(2L, favorite.getUserId());
        verify(favoriteMapper).insert(favorite);
    }

    @Test
    void addDoesNotInsertDuplicateFavorite() {
        Favorite favorite = new Favorite();
        favorite.setProductId(3L);

        when(favoriteMapper.findByUserIdAndProductId(2L, 3L)).thenReturn(new Favorite());

        favoriteService.add(2L, favorite);

        verify(favoriteMapper, never()).insert(favorite);
    }

    @Test
    void checkReturnsFavoriteState() {
        Favorite favorite = new Favorite();
        favorite.setId(99L);

        when(favoriteMapper.findByUserIdAndProductId(2L, 3L)).thenReturn(favorite);

        Map<String, Object> result = favoriteService.check(2L, 3L);

        assertTrue((Boolean) result.get("isFavorite"));
        assertEquals(99L, result.get("favoriteId"));
    }

    @Test
    void checkReturnsFalseWhenNotFavorited() {
        when(favoriteMapper.findByUserIdAndProductId(2L, 3L)).thenReturn(null);

        Map<String, Object> result = favoriteService.check(2L, 3L);

        assertFalse((Boolean) result.get("isFavorite"));
    }
}
