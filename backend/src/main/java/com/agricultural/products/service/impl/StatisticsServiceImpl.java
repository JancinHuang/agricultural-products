package com.agricultural.products.service.impl;

import com.agricultural.products.mapper.CategoryMapper;
import com.agricultural.products.mapper.OrderMapper;
import com.agricultural.products.mapper.ProductMapper;
import com.agricultural.products.mapper.UserMapper;
import com.agricultural.products.service.StatisticsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final String PAID_ORDER_STATUS = "1,2,3";
    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final CategoryMapper categoryMapper;
    private final JdbcTemplate jdbcTemplate;

    public StatisticsServiceImpl(
            UserMapper userMapper,
            ProductMapper productMapper,
            OrderMapper orderMapper,
            CategoryMapper categoryMapper,
            JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.categoryMapper = categoryMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Object> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.count());
        data.put("productCount", productMapper.count());
        data.put("orderCount", orderMapper.count());
        data.put("categoryCount", categoryMapper.count());
        return data;
    }

    @Override
    public List<Map<String, Object>> salesTrend() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        Map<LocalDate, BigDecimal> revenueByDay = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            revenueByDay.put(startDate.plusDays(i), BigDecimal.ZERO);
        }

        String sql = """
                SELECT DATE(create_time) AS stat_date, COALESCE(SUM(total_amount), 0) AS amount
                FROM `order`
                WHERE status IN (1, 2, 3)
                  AND create_time >= ?
                GROUP BY DATE(create_time)
                ORDER BY stat_date
                """;
        jdbcTemplate.queryForList(sql, Date.valueOf(startDate)).forEach(row -> {
            LocalDate date = ((Date) row.get("stat_date")).toLocalDate();
            revenueByDay.put(date, toBigDecimal(row.get("amount")));
        });

        List<Map<String, Object>> list = new ArrayList<>();
        revenueByDay.forEach((date, amount) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", date.format(DAY_LABEL_FORMATTER));
            item.put("value", amount);
            list.add(item);
        });
        return list;
    }

    @Override
    public List<Map<String, Object>> categoryDistribution() {
        String sql = """
                SELECT c.name, COUNT(p.id) AS value
                FROM category c
                LEFT JOIN product p ON p.category_id = c.id
                GROUP BY c.id, c.name
                ORDER BY value DESC, c.sort ASC
                """;
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> orderStatus() {
        Map<Integer, Long> countByStatus = new HashMap<>();
        jdbcTemplate.queryForList("SELECT status, COUNT(*) AS count FROM `order` GROUP BY status").forEach(row -> {
            countByStatus.put(((Number) row.get("status")).intValue(), ((Number) row.get("count")).longValue());
        });

        List<Map<String, Object>> list = new ArrayList<>();
        addOrderStatusItem(list, "待付款", countByStatus.getOrDefault(0, 0L), "#409EFF");
        addOrderStatusItem(list, "待发货", countByStatus.getOrDefault(1, 0L), "#E6A23C");
        addOrderStatusItem(list, "待收货", countByStatus.getOrDefault(2, 0L), "#67C23A");
        addOrderStatusItem(list, "已完成", countByStatus.getOrDefault(3, 0L), "#909399");
        addOrderStatusItem(list, "已取消", countByStatus.getOrDefault(4, 0L), "#F56C6C");
        return list;
    }

    @Override
    public List<Map<String, Object>> userGrowth() {
        YearMonth startMonth = YearMonth.now().minusMonths(5);
        LocalDate startDate = startMonth.atDay(1);
        long cumulative = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE create_time < ?",
                Long.class,
                Date.valueOf(startDate)
        );

        Map<YearMonth, Long> newUsersByMonth = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
            newUsersByMonth.put(startMonth.plusMonths(i), 0L);
        }

        String sql = """
                SELECT DATE_FORMAT(create_time, '%Y-%m') AS month_key, COUNT(*) AS count
                FROM user
                WHERE create_time >= ?
                GROUP BY DATE_FORMAT(create_time, '%Y-%m')
                ORDER BY month_key
                """;
        jdbcTemplate.queryForList(sql, Date.valueOf(startDate)).forEach(row -> {
            YearMonth month = YearMonth.parse((String) row.get("month_key"));
            newUsersByMonth.put(month, ((Number) row.get("count")).longValue());
        });

        List<Map<String, Object>> list = new ArrayList<>();
        long totalUsers = cumulative;
        for (Map.Entry<YearMonth, Long> entry : newUsersByMonth.entrySet()) {
            long newUsers = entry.getValue();
            totalUsers += newUsers;
            Map<String, Object> item = new HashMap<>();
            item.put("month", entry.getKey().getMonthValue() + "月");
            item.put("newUsers", newUsers);
            item.put("totalUsers", totalUsers);
            list.add(item);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> hotProducts() {
        String sql = """
                SELECT oi.product_name AS name, COALESCE(SUM(oi.quantity), 0) AS sales
                FROM order_item oi
                JOIN `order` o ON o.id = oi.order_id
                WHERE o.status IN (1, 2, 3)
                GROUP BY oi.product_id, oi.product_name
                ORDER BY sales DESC
                LIMIT 10
                """;
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> revenue() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayRevenue", queryRevenue("DATE(create_time) = CURDATE()"));
        data.put("weekRevenue", queryRevenue("YEARWEEK(create_time, 1) = YEARWEEK(CURDATE(), 1)"));
        data.put("monthRevenue", queryRevenue("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')"));
        data.put("yearRevenue", queryRevenue("YEAR(create_time) = YEAR(CURDATE())"));
        return data;
    }

    private BigDecimal queryRevenue(String dateCondition) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM `order` WHERE status IN (" + PAID_ORDER_STATUS + ") AND " + dateCondition;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    private void addOrderStatusItem(List<Map<String, Object>> list, String name, Long value, String color) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        item.put("color", color);
        list.add(item);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
