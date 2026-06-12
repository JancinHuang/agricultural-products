package com.agricultural.products.service.impl;

import com.agricultural.products.config.DeepSeekProperties;
import com.agricultural.products.entity.*;
import com.agricultural.products.mapper.*;
import com.agricultural.products.service.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 购物助手服务实现
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private DeepSeekProperties properties;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** 简易防刷：记录每个用户最后请求时间 */
    private final ConcurrentHashMap<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    private static final long REQUEST_INTERVAL_MS = 3000;

    @Override
    public String chat(Long userId, String userMessage, List<Map<String, String>> conversationHistory) {
        // 简易防刷检查
        String clientKey = userId != null ? "user_" + userId : "anon";
        Long lastTime = lastRequestTime.get(clientKey);
        long now = System.currentTimeMillis();
        if (lastTime != null && (now - lastTime) < REQUEST_INTERVAL_MS) {
            return "你的操作太频繁了，请稍等一下再试哦 ⏳";
        }
        lastRequestTime.put(clientKey, now);

        try {
            // 1. 构建商品上下文
            String productContext = buildProductContext(userId, userMessage);

            // 2. 构建 system prompt
            String systemPrompt = buildSystemPrompt(productContext);

            // 3. 组装消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            // 添加对话历史（最多保留 10 条）
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                int start = Math.max(0, conversationHistory.size() - 10);
                messages.addAll(conversationHistory.subList(start, conversationHistory.size()));
            }

            messages.add(Map.of("role", "user", "content", userMessage));

            // 4. 调用 DeepSeek API
            return callDeepSeekApi(messages);
        } catch (Exception e) {
            log.error("AI 助手服务异常", e);
            return "抱歉，AI助手暂时无法回答，请稍后再试 😥";
        }
    }

    /**
     * 构建商品上下文信息
     */
    private String buildProductContext(Long userId, String userMessage) {
        StringBuilder context = new StringBuilder();

        // 分类列表
        try {
            List<Category> categories = categoryMapper.findAll();
            if (categories != null && !categories.isEmpty()) {
                context.append("## 商城分类\n");
                StringBuilder names = new StringBuilder();
                for (Category cat : categories) {
                    if (names.length() > 0) names.append(", ");
                    names.append(cat.getName());
                }
                context.append(names).append("\n\n");
            }
        } catch (Exception e) {
            log.warn("获取分类列表失败", e);
        }

        // 热销商品
        try {
            List<Product> hotProducts = productMapper.findHotProducts(8);
            if (hotProducts != null && !hotProducts.isEmpty()) {
                context.append("## 热销商品\n");
                for (int i = 0; i < hotProducts.size(); i++) {
                    Product p = hotProducts.get(i);
                    context.append(String.format("%d. [ID:%d] %s - ¥%s/%s - 产地:%s - 已售%d\n",
                            i + 1, p.getId(), p.getName(),
                            p.getPrice() != null ? p.getPrice().toPlainString() : "暂无",
                            p.getUnit() != null ? p.getUnit() : "份",
                            p.getOrigin() != null ? p.getOrigin() : "未知",
                            p.getSales() != null ? p.getSales() : 0));
                }
                context.append("\n");
            }
        } catch (Exception e) {
            log.warn("获取热销商品失败", e);
        }

        // 与用户提问相关的商品搜索
        if (userMessage != null && userMessage.length() >= 2) {
            try {
                List<Product> relatedProducts = productMapper.searchProducts(
                        userMessage, null, null, null, "sales", 5, 0);
                if (relatedProducts != null && !relatedProducts.isEmpty()) {
                    context.append("## 与用户提问相关的商品\n");
                    for (int i = 0; i < relatedProducts.size(); i++) {
                        Product p = relatedProducts.get(i);
                        String desc = p.getDescription() != null && p.getDescription().length() > 50
                                ? p.getDescription().substring(0, 50) + "..."
                                : (p.getDescription() != null ? p.getDescription() : "");
                        context.append(String.format("%d. [ID:%d] %s - ¥%s/%s - 产地:%s - 已售%d - %s\n",
                                i + 1, p.getId(), p.getName(),
                                p.getPrice() != null ? p.getPrice().toPlainString() : "暂无",
                                p.getUnit() != null ? p.getUnit() : "份",
                                p.getOrigin() != null ? p.getOrigin() : "未知",
                                p.getSales() != null ? p.getSales() : 0,
                                desc));
                    }
                    context.append("\n");
                }
            } catch (Exception e) {
                log.warn("搜索相关商品失败", e);
            }
        }

        // 用户偏好（已登录时）
        if (userId != null) {
            try {
                // 最近订单
                List<Order> orders = orderMapper.findByUserId(userId);
                if (orders != null && !orders.isEmpty()) {
                    context.append("## 用户偏好\n");
                    context.append("最近购买: ");
                    List<String> boughtNames = new ArrayList<>();
                    int orderCount = 0;
                    for (Order order : orders) {
                        if (orderCount >= 3) break;
                        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
                        if (items != null) {
                            for (OrderItem item : items) {
                                boughtNames.add(item.getProductName());
                            }
                        }
                        orderCount++;
                    }
                    context.append(String.join(", ", boughtNames)).append("\n");

                    // 收藏商品
                    List<Favorite> favorites = favoriteMapper.findByUserId(userId);
                    if (favorites != null && !favorites.isEmpty()) {
                        List<String> favNames = new ArrayList<>();
                        int favCount = 0;
                        for (Favorite fav : favorites) {
                            if (favCount >= 5) break;
                            favNames.add(fav.getProductName());
                            favCount++;
                        }
                        context.append("收藏商品: ").append(String.join(", ", favNames)).append("\n");
                    }
                    context.append("\n");
                }
            } catch (Exception e) {
                log.warn("获取用户偏好失败", e);
            }
        }

        return context.toString();
    }

    /**
     * 构建 system prompt
     */
    private String buildSystemPrompt(String productContext) {
        return "你是\"助农商城\"的智能购物助手 🌿\n" +
                "\n" +
                "## 你的能力\n" +
                "- 根据用户需求推荐合适的农产品\n" +
                "- 比较不同商品的价格、产地、销量\n" +
                "- 提供农产品选购建议（如季节性、产地特点、营养价值）\n" +
                "- 回答关于商品分类、价格区间的问题\n" +
                "\n" +
                "## 回答格式规范（重要！必须遵守）\n" +
                "1. 使用 **Markdown** 格式回复，让内容结构清晰、美观\n" +
                "2. 使用 `###` 小标题分隔不同推荐类别或话题\n" +
                "3. 使用 **加粗** 突出商品名称、价格等关键信息\n" +
                "4. 使用无序列表 `- ` 或有序列表 `1. ` 来列举推荐商品\n" +
                "5. 推荐商品时**必须**使用以下格式（前端会渲染为可点击卡片）：\n" +
                "   [商品:商品名称:商品ID]\n" +
                "   例如：**[商品:有机红富士苹果:5]** — 山东烟台直供，口感脆甜！\n" +
                "6. 每次推荐不超过 3-5 个商品，每个商品说明推荐理由\n" +
                "7. 绝对不要编造不存在的商品，只推荐下方提供的商品列表中的商品\n" +
                "8. 如果用户的问题与购物无关，礼貌引导回购物话题\n" +
                "9. 如果没有合适的商品，诚实告知并建议替代方案\n" +
                "\n" +
                "## 回复示例\n" +
                "### 🔥 热销水果推荐\n" +
                "为你精选了几款热门水果：\n" +
                "\n" +
                "- **[商品:有机红富士苹果:5]** — 产地山东烟台，脆甜多汁，已售238份\n" +
                "- **[商品:云南蓝莓:8]** — 新鲜采摘，颗颗饱满，富含花青素\n" +
                "\n" +
                "> 💡 小贴士：现在的时令水果品质最好，建议优先选择当季产品哦！\n" +
                "\n" +
                productContext;
    }

    /**
     * 调用 DeepSeek API
     */
    private String callDeepSeekApi(List<Map<String, String>> messages) throws Exception {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("max_tokens", properties.getMaxTokens());
        requestBody.put("temperature", properties.getTemperature());
        requestBody.put("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();

        // 发送请求
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 检查HTTP状态码
        if (response.statusCode() != 200) {
            log.error("DeepSeek API 返回错误: status={}, body={}", response.statusCode(), response.body());
            throw new RuntimeException("DeepSeek API 调用失败: HTTP " + response.statusCode());
        }

        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(
                response.body(), new TypeReference<Map<String, Object>>() {});

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("DeepSeek API 返回空响应");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            throw new RuntimeException("DeepSeek API 响应格式异常");
        }

        String content = (String) message.get("content");
        return content != null ? content : "AI助手暂时无法回答，请稍后再试。";
    }
}
