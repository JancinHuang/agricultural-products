package com.agricultural.products.service.impl;

import com.agricultural.products.mapper.CategoryMapper;
import com.agricultural.products.mapper.OrderMapper;
import com.agricultural.products.mapper.ProductMapper;
import com.agricultural.products.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Test
    void overviewReturnsCoreCounts() {
        when(userMapper.count()).thenReturn(16L);
        when(productMapper.count()).thenReturn(45L);
        when(orderMapper.count()).thenReturn(20L);
        when(categoryMapper.count()).thenReturn(8L);

        Map<String, Object> overview = statisticsService.overview();

        assertEquals(16L, overview.get("userCount"));
        assertEquals(45L, overview.get("productCount"));
        assertEquals(20L, overview.get("orderCount"));
        assertEquals(8L, overview.get("categoryCount"));
    }

    @Test
    void orderStatusFillsMissingStatusesWithZero() {
        when(jdbcTemplate.queryForList("SELECT status, COUNT(*) AS count FROM `order` GROUP BY status"))
                .thenReturn(List.of(
                        Map.of("status", 1, "count", 3L),
                        Map.of("status", 3, "count", 7L)
                ));

        List<Map<String, Object>> result = statisticsService.orderStatus();

        assertEquals(5, result.size());
        assertEquals("待付款", result.get(0).get("name"));
        assertEquals(0L, result.get(0).get("value"));
        assertEquals("待发货", result.get(1).get("name"));
        assertEquals(3L, result.get(1).get("value"));
        assertEquals("已完成", result.get(3).get("name"));
        assertEquals(7L, result.get(3).get("value"));
    }

    @Test
    void revenueReturnsPeriodAmounts() {
        when(jdbcTemplate.queryForObject(any(String.class), eq(BigDecimal.class)))
                .thenReturn(new BigDecimal("12.30"))
                .thenReturn(new BigDecimal("45.60"))
                .thenReturn(new BigDecimal("78.90"))
                .thenReturn(new BigDecimal("123.45"));

        Map<String, Object> revenue = statisticsService.revenue();

        assertEquals(new BigDecimal("12.30"), revenue.get("todayRevenue"));
        assertEquals(new BigDecimal("45.60"), revenue.get("weekRevenue"));
        assertEquals(new BigDecimal("78.90"), revenue.get("monthRevenue"));
        assertEquals(new BigDecimal("123.45"), revenue.get("yearRevenue"));
    }
}
