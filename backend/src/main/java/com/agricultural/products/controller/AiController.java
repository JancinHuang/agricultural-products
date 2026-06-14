package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.service.AiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 购物助手控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 与 AI 购物助手对话
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> requestBody) {
        String message = (String) requestBody.get("message");

        if (message == null || message.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }
        if (message.length() > 500) {
            return Result.error("消息不能超过500字");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) requestBody.get("history");

        Long userId = SecurityUtils.getCurrentUserId();
        String aiReply = aiService.chat(userId, message.trim(), history);

        Map<String, Object> result = new HashMap<>();
        result.put("message", aiReply);
        result.put("timestamp", System.currentTimeMillis());

        return Result.success(result);
    }
}
