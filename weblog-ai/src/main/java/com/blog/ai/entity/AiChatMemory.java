package com.blog.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_chat_memory")
public class AiChatMemory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private String userMessage;
    private String aiMessage;
    private String sources;       // JSON array string
    private LocalDateTime createTime;
}
