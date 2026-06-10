package com.blog.ai.controller;

import com.blog.ai.dto.ChatRequestDTO;
import com.blog.ai.service.ChatService;
import com.blog.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final ChatService chatService;

    @Operation(summary = "流式对话（纯文本）")
    @PostMapping("/chat/stream/text")
    public StreamingResponseBody chatStreamText(@Valid @RequestBody ChatRequestDTO request) {
        return chatService.chatStreamText(request.getSessionId(), request.getQuestion());
    }

    @Operation(summary = "校验会话是否有效；无效时返回 sessionId=null")
    @GetMapping("/session/validate")
    public Result<Map<String, Object>> validateSession(@RequestParam(required = false) String sessionId) {
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
    @GetMapping("/session/history")
    public Result<List<Map<String, Object>>> getChatHistory(@RequestParam(required = false) String sessionId) {
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
