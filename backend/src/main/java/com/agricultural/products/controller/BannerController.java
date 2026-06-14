package com.agricultural.products.controller;

import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.Banner;
import com.agricultural.products.service.BannerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/list")
    public Result<List<Banner>> list() {
        return Result.success(bannerService.listEnabled());
    }

    @GetMapping("/page")
    public Result<PageResult<Banner>> page(PageRequest request) {
        return Result.success(bannerService.findByPage(request));
    }

    @GetMapping("/{id}")
    public Result<Banner> getById(@PathVariable Long id) {
        return Result.success(bannerService.findById(id));
    }

    @PostMapping
    public Result<String> save(@RequestBody Banner banner) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error("无权限操作");
        }
        return bannerService.save(banner) ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Banner banner) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error("无权限操作");
        }
        return bannerService.update(banner) ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error("无权限操作");
        }
        return bannerService.deleteById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
