package com.agricultural.products.service;

import java.util.List;
import java.util.Map;

/**
 * AI 购物助手服务接口
 */
public interface AiService {

    /**
     * 与 AI 购物助手对话
     *
     * @param userId              用户ID（可为 null，表示未登录）
     * @param userMessage         用户消息
     * @param conversationHistory 对话历史
     * @return AI 回复内容
     */
    String chat(Long userId, String userMessage, List<Map<String, String>> conversationHistory);
}
