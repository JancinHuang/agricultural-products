package com.agricultural.products.controller;

import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.Banner;
import com.agricultural.products.mapper.BannerMapper;
import com.agricultural.products.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private ObjectStorageService objectStorageService;

    @GetMapping("/list")
    public Result<List<Banner>> list() {
        return Result.success(signImages(bannerMapper.findEnabled()));
    }

    @GetMapping("/page")
    public Result<PageResult<Banner>> page(PageRequest request) {
        List<Banner> list = signImages(bannerMapper.findByPage(request));
        Long total = bannerMapper.countByPage(request);
        return Result.success(new PageResult<>(list, total, request.getPageNum(), request.getPageSize()));
    }

    @GetMapping("/{id}")
    public Result<Banner> getById(@PathVariable Long id) {
        return Result.success(signImage(bannerMapper.findById(id)));
    }

    @PostMapping
    public Result<String> save(@RequestBody Banner banner) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error("无权限操作");
        }
        fillDefaults(banner);
        return bannerMapper.insert(banner) > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Banner banner) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error("无权限操作");
        }
        return bannerMapper.update(banner) > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error("无权限操作");
        }
        return bannerMapper.deleteById(id) > 0 ? Result.success("删除成功") : Result.error("删除失败");
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
