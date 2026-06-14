package com.agricultural.products.service.impl;

import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.entity.Banner;
import com.agricultural.products.mapper.BannerMapper;
import com.agricultural.products.service.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @Mock
    private BannerMapper bannerMapper;

    @Mock
    private ObjectStorageService objectStorageService;

    private BannerServiceImpl bannerService;

    @BeforeEach
    void setUp() {
        bannerService = new BannerServiceImpl(bannerMapper, objectStorageService);
    }

    @Test
    void listEnabledSignsImages() {
        Banner banner = new Banner();
        banner.setImage("banner.png");

        when(bannerMapper.findEnabled()).thenReturn(List.of(banner));
        when(objectStorageService.toAccessibleUrl("banner.png")).thenReturn("https://cdn.example/banner.png");

        List<Banner> result = bannerService.listEnabled();

        assertEquals("https://cdn.example/banner.png", result.get(0).getImageUrl());
    }

    @Test
    void findByPageWrapsPageResultAndSignsImages() {
        PageRequest request = new PageRequest();
        request.setPageNum(2);
        request.setPageSize(5);

        Banner banner = new Banner();
        banner.setImage("banner.png");

        when(bannerMapper.findByPage(request)).thenReturn(List.of(banner));
        when(bannerMapper.countByPage(request)).thenReturn(11L);
        when(objectStorageService.toAccessibleUrl("banner.png")).thenReturn("https://cdn.example/banner.png");

        PageResult<Banner> result = bannerService.findByPage(request);

        assertEquals(11L, result.getTotal());
        assertEquals(2, result.getPageNum());
        assertEquals(5, result.getPageSize());
        assertEquals("https://cdn.example/banner.png", result.getList().get(0).getImageUrl());
    }

    @Test
    void saveFillsDefaults() {
        Banner banner = new Banner();
        banner.setTitle("首页轮播");
        when(bannerMapper.insert(banner)).thenReturn(1);

        boolean success = bannerService.save(banner);

        assertTrue(success);
        assertEquals(0, banner.getSort());
        assertEquals(1, banner.getStatus());
        verify(bannerMapper).insert(banner);
    }

    @Test
    void updateReturnsMapperResult() {
        Banner banner = new Banner();
        banner.setId(1L);
        when(bannerMapper.update(banner)).thenReturn(1);

        boolean success = bannerService.update(banner);

        assertTrue(success);
    }

    @Test
    void deleteReturnsMapperResult() {
        when(bannerMapper.deleteById(1L)).thenReturn(1);

        boolean success = bannerService.deleteById(1L);

        assertTrue(success);
    }
}
