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
import java.util.Map;
import java.util.UUID;

@Tag(name = "AI 智能问答")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "流式对话（纯文本，无需 SSE 解析）")
    @PostMapping("/chat/stream/text")
    public StreamingResponseBody chatStreamText(@Valid @RequestBody ChatRequestDTO request) {
        return chatService.chatStreamText(request.getSessionId(), request.getQuestion());
    }

    @Operation(summary = "校验会话是否有效（未过期）")
    @GetMapping("/session/validate")
    public Result<Map<String, Object>> validateSession(@RequestParam(required = false) String sessionId) {
        boolean valid = chatService.validateSession(sessionId);
        Map<String, Object> data = new HashMap<>();
        if (valid) {
            data.put("valid", true);
            data.put("sessionId", sessionId);
        } else {
            data.put("valid", false);
            data.put("sessionId", UUID.randomUUID().toString());
        }
        return Result.ok(data);
    }
}
