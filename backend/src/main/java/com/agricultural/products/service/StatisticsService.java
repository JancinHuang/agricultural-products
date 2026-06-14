package com.agricultural.products.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    Map<String, Object> overview();

    List<Map<String, Object>> salesTrend();

    List<Map<String, Object>> categoryDistribution();

    List<Map<String, Object>> orderStatus();

    List<Map<String, Object>> userGrowth();

    List<Map<String, Object>> hotProducts();

    Map<String, Object> revenue();
}
