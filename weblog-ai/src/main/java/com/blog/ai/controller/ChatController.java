package com.blog.ai.controller;

import com.blog.ai.dto.ChatRequestDTO;
import com.blog.ai.service.ChatService;
import com.blog.exception.BusinessException;
import com.blog.result.Result;
import com.blog.utils.IpUtil;
import com.blog.utils.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "AI 智能问答")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class ChatController {

    /** 流式对话限流：每 IP 每分钟最多 10 次（调用智谱 API，防止刷爆付费额度） */
    private static final int STREAM_MAX_PER_MINUTE = 10;
    /** 会话查询限流：每 IP 每分钟最多 30 次（不烧 token，仅防滥用） */
    private static final int SESSION_MAX_PER_MINUTE = 30;

    private final ChatService chatService;
    private final RateLimiter rateLimiter;

    @Operation(summary = "流式对话（纯文本）")
    @PostMapping("/chat/stream/text")
    public StreamingResponseBody chatStreamText(@Valid @RequestBody ChatRequestDTO request,
                                                HttpServletRequest servletRequest) {
        String ip = IpUtil.getClientIp(servletRequest);
        if (!rateLimiter.tryAcquire("ai:stream:" + ip, STREAM_MAX_PER_MINUTE, 60_000)) {
            throw new BusinessException("请求过于频繁，请稍后再试");
        }
        return chatService.chatStreamText(request.getSessionId(), request.getQuestion());
    }

    @Operation(summary = "校验会话是否有效；无效时返回 sessionId=null")
    @PostMapping("/session/validate")
    public Result<Map<String, Object>> validateSession(@RequestBody(required = false) Map<String, String> body,
                                                       HttpServletRequest servletRequest) {
        if (!rateLimiter.tryAcquire("ai:session:validate:" + IpUtil.getClientIp(servletRequest), SESSION_MAX_PER_MINUTE, 60_000)) {
            return Result.fail("请求过于频繁，请稍后再试");
        }
        String sessionId = body != null ? body.get("sessionId") : null;
        Map<String, Object> data = new HashMap<>();

        if (sessionId == null || sessionId.isBlank()) {
            data.put("valid", false);
            data.put("sessionId", null);
            return Result.ok(data);
        }

        boolean valid = chatService.validateSession(sessionId);
        data.put("valid", valid);
        data.put("sessionId", valid ? sessionId : null);
        return Result.ok(data);
    }

    @Operation(summary = "获取会话的历史聊天记录")
    @PostMapping("/session/history")
    public Result<List<Map<String, Object>>> getChatHistory(@RequestBody(required = false) Map<String, String> body,
                                                            HttpServletRequest servletRequest) {
        if (!rateLimiter.tryAcquire("ai:session:history:" + IpUtil.getClientIp(servletRequest), SESSION_MAX_PER_MINUTE, 60_000)) {
            return Result.fail("请求过于频繁，请稍后再试");
        }
        String sessionId = body != null ? body.get("sessionId") : null;
        if (sessionId == null || sessionId.isBlank()) {
            return Result.ok(List.of());
        }
        // 确保会话有效再返回记录
        if (!chatService.validateSession(sessionId)) {
            return Result.ok(List.of());
        }
        return Result.ok(chatService.getChatHistory(sessionId));
    }
}
