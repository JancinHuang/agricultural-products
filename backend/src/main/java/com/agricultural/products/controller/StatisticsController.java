package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器 - 管理端首页概览数据
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(statisticsService.overview());
    }

    @GetMapping("/sales-trend")
    public Result<List<Map<String, Object>>> salesTrend() {
        return Result.success(statisticsService.salesTrend());
    }

    @GetMapping("/category-distribution")
    public Result<List<Map<String, Object>>> categoryDistribution() {
        return Result.success(statisticsService.categoryDistribution());
    }

    @GetMapping("/order-status")
    public Result<List<Map<String, Object>>> orderStatus() {
        return Result.success(statisticsService.orderStatus());
    }

    @GetMapping("/user-growth")
    public Result<List<Map<String, Object>>> userGrowth() {
        return Result.success(statisticsService.userGrowth());
    }

    @GetMapping("/hot-products")
    public Result<List<Map<String, Object>>> hotProducts() {
        return Result.success(statisticsService.hotProducts());
    }

    @GetMapping("/revenue")
    public Result<Map<String, Object>> revenue() {
        return Result.success(statisticsService.revenue());
    }
}
