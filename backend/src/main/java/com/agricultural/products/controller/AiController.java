package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 购物助手控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * 与 AI 购物助手对话
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> requestBody) {
        String message = (String) requestBody.get("message");

        // 参数校验
        if (message == null || message.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }
        if (message.length() > 500) {
            return Result.error("消息不能超过500字");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) requestBody.get("history");

        // 获取当前用户ID（未登录时为 null）
        Long userId = SecurityUtils.getCurrentUserId();

        // 调用 AI 服务
        String aiReply = aiService.chat(userId, message.trim(), history);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("message", aiReply);
        result.put("timestamp", System.currentTimeMillis());

        return Result.success(result);
    }
}
