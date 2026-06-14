package com.agricultural.products.service.impl;

import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.entity.Banner;
import com.agricultural.products.mapper.BannerMapper;
import com.agricultural.products.service.BannerService;
import com.agricultural.products.service.ObjectStorageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;
    private final ObjectStorageService objectStorageService;

    public BannerServiceImpl(BannerMapper bannerMapper, ObjectStorageService objectStorageService) {
        this.bannerMapper = bannerMapper;
        this.objectStorageService = objectStorageService;
    }

    @Override
    public List<Banner> listEnabled() {
        return signImages(bannerMapper.findEnabled());
    }

    @Override
    public PageResult<Banner> findByPage(PageRequest request) {
        List<Banner> list = signImages(bannerMapper.findByPage(request));
        Long total = bannerMapper.countByPage(request);
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public Banner findById(Long id) {
        return signImage(bannerMapper.findById(id));
    }

    @Override
    public boolean save(Banner banner) {
        fillDefaults(banner);
        return bannerMapper.insert(banner) > 0;
    }

    @Override
    public boolean update(Banner banner) {
        return bannerMapper.update(banner) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return bannerMapper.deleteById(id) > 0;
    }

    private void fillDefaults(Banner banner) {
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
    }

    private List<Banner> signImages(List<Banner> banners) {
        if (banners == null) {
            return null;
        }
        banners.forEach(this::signImage);
        return banners;
    }

    private Banner signImage(Banner banner) {
        if (banner == null) {
            return null;
        }
        banner.setImageUrl(objectStorageService.toAccessibleUrl(banner.getImage()));
        return banner;
    }
}
