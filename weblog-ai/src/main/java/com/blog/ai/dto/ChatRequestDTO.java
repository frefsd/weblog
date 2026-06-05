package com.blog.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 对话请求 DTO。
 *
 * <p>首次对话只需传 question，后端自动创建会话并返回 sessionId；
 * 后续对话带上 sessionId 以延续同一会话。</p>
 */
@Data
public class ChatRequestDTO {

    /**
     * 会话 ID（可选）。
     * <p>首次对话不传，后端自动创建新会话并返回 sessionId；
     * 后续对话传入此 ID，延续同一会话的历史上下文。</p>
     */
    private String sessionId;

    /** 用户输入的问题文本 */
    @NotBlank(message = "问题不能为空")
    private String question;
}

