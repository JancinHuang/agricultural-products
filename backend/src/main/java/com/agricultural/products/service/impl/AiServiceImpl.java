package com.agricultural.products.service.impl;

import com.agricultural.products.config.DeepSeekProperties;
import com.agricultural.products.entity.Category;
import com.agricultural.products.entity.Favorite;
import com.agricultural.products.entity.Order;
import com.agricultural.products.entity.OrderItem;
import com.agricultural.products.entity.Product;
import com.agricultural.products.mapper.CategoryMapper;
import com.agricultural.products.mapper.FavoriteMapper;
import com.agricultural.products.mapper.OrderItemMapper;
import com.agricultural.products.mapper.OrderMapper;
import com.agricultural.products.mapper.ProductMapper;
import com.agricultural.products.service.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 购物助手服务实现
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private static final long REQUEST_INTERVAL_MS = 3000;

    private final DeepSeekProperties properties;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final FavoriteMapper favoriteMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ConcurrentHashMap<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    public AiServiceImpl(
            DeepSeekProperties properties,
            ProductMapper productMapper,
            CategoryMapper categoryMapper,
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            FavoriteMapper favoriteMapper,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.favoriteMapper = favoriteMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public String chat(Long userId, String userMessage, List<Map<String, String>> conversationHistory) {
        String clientKey = userId != null ? "user_" + userId : "anon";
        Long lastTime = lastRequestTime.get(clientKey);
        long now = System.currentTimeMillis();
        if (lastTime != null && now - lastTime < REQUEST_INTERVAL_MS) {
            return "你的操作太频繁了，请稍等一下再试。";
        }
        lastRequestTime.put(clientKey, now);

        String productContext = "";
        try {
            productContext = buildProductContext(userId, userMessage);
            if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
                log.warn("DeepSeek API key 未配置，使用本地商品数据兜底回复");
                return buildLocalProductReply(userMessage);
            }
            String systemPrompt = buildSystemPrompt(productContext);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                int start = Math.max(0, conversationHistory.size() - 10);
                messages.addAll(conversationHistory.subList(start, conversationHistory.size()));
            }

            messages.add(Map.of("role", "user", "content", userMessage));
            return callDeepSeekApi(messages);
        } catch (Exception e) {
            log.error("AI 助手服务异常", e);
            return productContext.isBlank()
                    ? "抱歉，AI 助手暂时无法回答，请稍后再试。"
                    : buildLocalProductReply(userMessage);
        }
    }

    private String buildProductContext(Long userId, String userMessage) {
        StringBuilder context = new StringBuilder();

        appendCategoryContext(context);
        appendCategoryMatchedProductContext(context, userMessage);
        appendAvailableProductContext(context);
        appendHotProductContext(context);
        appendRelatedProductContext(context, userMessage);
        appendUserPreferenceContext(context, userId);

        return context.toString();
    }

    private void appendCategoryContext(StringBuilder context) {
        try {
            List<Category> categories = categoryMapper.findAll();
            if (categories == null || categories.isEmpty()) {
                return;
            }

            context.append("## 商城分类\n");
            List<String> categoryNames = new ArrayList<>();
            for (Category category : categories) {
                categoryNames.add(category.getName());
            }
            context.append(String.join(", ", categoryNames)).append("\n\n");
        } catch (Exception e) {
            log.warn("获取分类列表失败", e);
        }
    }

    private void appendAvailableProductContext(StringBuilder context) {
        try {
            List<Product> products = productMapper.searchProducts(
                    null, null, null, null, "sales", 100, 0);
            if (products == null || products.isEmpty()) {
                return;
            }

            context.append("## 当前可售商品库（优先从这里推荐）\n");
            appendProductLines(context, products);
            context.append("\n");
        } catch (Exception e) {
            log.warn("获取当前可售商品失败", e);
        }
    }

    private void appendCategoryMatchedProductContext(StringBuilder context, String userMessage) {
        Category category = findMentionedCategory(userMessage);
        if (category == null) {
            return;
        }

        try {
            List<Product> products = productMapper.findByCategoryId(category.getId());
            if (products == null || products.isEmpty()) {
                return;
            }

            context.append("## 用户问题命中的分类：")
                    .append(category.getName())
                    .append("\n");
            appendProductLines(context, products);
            context.append("\n");
        } catch (Exception e) {
            log.warn("获取分类命中商品失败", e);
        }
    }

    private void appendHotProductContext(StringBuilder context) {
        try {
            List<Product> hotProducts = productMapper.findHotProducts(8);
            if (hotProducts == null || hotProducts.isEmpty()) {
                return;
            }

            context.append("## 热销商品\n");
            appendProductLines(context, hotProducts);
            context.append("\n");
        } catch (Exception e) {
            log.warn("获取热销商品失败", e);
        }
    }

    private void appendRelatedProductContext(StringBuilder context, String userMessage) {
        if (userMessage == null || userMessage.length() < 2) {
            return;
        }

        try {
            List<Product> relatedProducts = productMapper.searchProducts(
                    userMessage, null, null, null, "sales", 5, 0);
            if (relatedProducts == null || relatedProducts.isEmpty()) {
                return;
            }

            context.append("## 与用户问题相关的商品\n");
            appendProductLines(context, relatedProducts);
            context.append("\n");
        } catch (Exception e) {
            log.warn("搜索相关商品失败", e);
        }
    }

    private void appendUserPreferenceContext(StringBuilder context, Long userId) {
        if (userId == null) {
            return;
        }

        try {
            List<Order> orders = orderMapper.findByUserId(userId);
            if (orders == null || orders.isEmpty()) {
                return;
            }

            context.append("## 用户偏好\n");
            appendRecentOrderItems(context, orders);
            appendFavoriteProducts(context, userId);
            context.append("\n");
        } catch (Exception e) {
            log.warn("获取用户偏好失败", e);
        }
    }

    private void appendRecentOrderItems(StringBuilder context, List<Order> orders) {
        List<String> boughtNames = new ArrayList<>();
        int orderCount = 0;
        for (Order order : orders) {
            if (orderCount >= 3) {
                break;
            }
            List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
            if (items != null) {
                for (OrderItem item : items) {
                    boughtNames.add(item.getProductName());
                }
            }
            orderCount++;
        }

        if (!boughtNames.isEmpty()) {
            context.append("最近购买: ").append(String.join(", ", boughtNames)).append("\n");
        }
    }

    private void appendFavoriteProducts(StringBuilder context, Long userId) {
        List<Favorite> favorites = favoriteMapper.findByUserId(userId);
        if (favorites == null || favorites.isEmpty()) {
            return;
        }

        List<String> favoriteNames = new ArrayList<>();
        int favoriteCount = 0;
        for (Favorite favorite : favorites) {
            if (favoriteCount >= 5) {
                break;
            }
            favoriteNames.add(favorite.getProductName());
            favoriteCount++;
        }

        if (!favoriteNames.isEmpty()) {
            context.append("收藏商品: ").append(String.join(", ", favoriteNames)).append("\n");
        }
    }

    private void appendProductLines(StringBuilder context, List<Product> products) {
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            context.append(String.format("%d. [ID:%d] %s - 分类:%s - ￥%s/%s - 产地:%s - 库存%d - 已售%d - %s\n",
                    i + 1,
                    product.getId(),
                    product.getName(),
                    product.getCategoryName() != null ? product.getCategoryName() : "未分类",
                    product.getPrice() != null ? product.getPrice().toPlainString() : "暂无",
                    product.getUnit() != null ? product.getUnit() : "件",
                    product.getOrigin() != null ? product.getOrigin() : "未知",
                    product.getStock() != null ? product.getStock() : 0,
                    product.getSales() != null ? product.getSales() : 0,
                    truncateDescription(product.getDescription())));
        }
    }

    private String truncateDescription(String description) {
        if (description == null) {
            return "";
        }
        return description.length() > 50 ? description.substring(0, 50) + "..." : description;
    }

    private String buildLocalProductReply(String userMessage) {
        List<Product> products = findProductsForLocalReply(userMessage);
        if (products.isEmpty()) {
            return """
                    我查了当前商城商品库，暂时没有找到适合推荐的在售商品。

                    你可以换个关键词试试，比如水果、蔬菜、米面粮油、产地或价格范围。
                    """;
        }

        StringBuilder reply = new StringBuilder();
        reply.append("我根据当前商城数据库里的在售商品，为你筛了这些：\n\n");
        for (Product product : products) {
            reply.append("- [商品:")
                    .append(product.getName())
                    .append(":")
                    .append(product.getId())
                    .append("]：￥")
                    .append(product.getPrice() != null ? product.getPrice().toPlainString() : "暂无价格")
                    .append("/")
                    .append(product.getUnit() != null ? product.getUnit() : "件")
                    .append("，产地：")
                    .append(product.getOrigin() != null ? product.getOrigin() : "未知")
                    .append("，库存：")
                    .append(product.getStock() != null ? product.getStock() : 0)
                    .append("，已售：")
                    .append(product.getSales() != null ? product.getSales() : 0)
                    .append("。\n");
        }
        reply.append("\n这些推荐来自当前数据库商品。你也可以告诉我预算、想买的品类或用途，我再继续筛。");
        return reply.toString();
    }

    private List<Product> findProductsForLocalReply(String userMessage) {
        List<Product> products = new ArrayList<>();
        try {
            Category category = findMentionedCategory(userMessage);
            if (category != null) {
                products = productMapper.findByCategoryId(category.getId());
            }
            if (userMessage != null && userMessage.length() >= 2) {
                if (products == null || products.isEmpty()) {
                    products = productMapper.searchProducts(userMessage, null, null, null, "sales", 100, 0);
                }
            }
            if (products == null || products.isEmpty()) {
                products = productMapper.searchProducts(null, null, null, null, "sales", 5, 0);
            }
        } catch (Exception e) {
            log.warn("本地商品推荐失败", e);
            return List.of();
        }
        return products == null ? List.of() : products;
    }

    private Category findMentionedCategory(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return null;
        }

        try {
            List<Category> categories = categoryMapper.findAll();
            if (categories == null || categories.isEmpty()) {
                return null;
            }

            String normalizedMessage = normalizeCategoryText(userMessage);
            for (Category category : categories) {
                String categoryName = category.getName();
                String normalizedCategoryName = normalizeCategoryText(categoryName);
                if (!normalizedCategoryName.isBlank()
                        && (normalizedMessage.contains(normalizedCategoryName)
                        || normalizedMessage.contains(categoryName))) {
                    return category;
                }
            }
        } catch (Exception e) {
            log.warn("识别用户提到的商品分类失败", e);
        }
        return null;
    }

    private String normalizeCategoryText(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("类", "")
                .replace("商品", "")
                .replace("产品", "")
                .replaceAll("\\s+", "")
                .trim();
    }

    private String buildSystemPrompt(String productContext) {
        return """
                你是“助农商城”的智能购物助手。

                ## 你的任务
                - 根据用户需求推荐合适的农产品。
                - 比较不同商品的价格、产地、销量和适用场景。
                - 提供农产品选购建议，例如季节性、产地特点、储存方式和搭配建议。
                - 用户问题与购物无关时，礼貌引导回农产品选购主题。

                ## 回答规范
                1. 使用 Markdown，让内容结构清楚。
                2. 推荐商品时必须使用格式：[商品:商品名称:商品ID]，前端会把它渲染为可点击商品卡片。
                3. 每次最多推荐 3-5 个商品，并说明推荐理由。
                4. 只能推荐下方商品上下文中真实存在的商品，不要编造不存在的商品。
                5. 回答必须体现你参考了当前商城商品库，例如商品名、价格、产地、销量或库存。
                6. 如果用户问“目前有卖哪些/都有什么/有哪些”这类清单问题，必须列出命中分类下的全部在售商品，不要只推荐部分商品。
                7. 如果商品上下文里有“用户问题命中的分类”，必须优先且只从该分类推荐，除非用户明确要求跨分类。
                8. 没有合适商品时，直接说明原因，并给出替代搜索建议。

                ## 商品上下文
                %s
                """.formatted(productContext);
    }

    private String callDeepSeekApi(List<Map<String, String>> messages) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("max_tokens", properties.getMaxTokens());
        requestBody.put("temperature", properties.getTemperature());
        requestBody.put("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            log.error("DeepSeek API 返回错误: status={}, body={}", response.statusCode(), response.body());
            throw new RuntimeException("DeepSeek API 调用失败: HTTP " + response.statusCode());
        }

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
        return content != null ? content : "AI 助手暂时无法回答，请稍后再试。";
    }
}
