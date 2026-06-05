package com.blog.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.ai.config.AiProperties;
import com.blog.ai.entity.AiChatMemory;
import com.blog.ai.mapper.AiChatMemoryMapper;
import com.blog.ai.service.ChatMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMemoryServiceImpl implements ChatMemoryService {

    private final AiChatMemoryMapper chatMemoryMapper;
    private final AiProperties aiProperties;

    @Override
    public void saveChat(String sessionId, String userMessage, String aiMessage, String sourcesJson) {
        AiChatMemory record = new AiChatMemory();
        record.setSessionId(sessionId);
        record.setUserMessage(userMessage);
        record.setAiMessage(aiMessage);
        record.setSources(sourcesJson);
        record.setCreateTime(LocalDateTime.now());
        chatMemoryMapper.insert(record);
    }

    @Override
    public List<AiChatMemory> getRecentChats(String sessionId, int maxRounds) {
        List<AiChatMemory> records = chatMemoryMapper.selectList(
                new LambdaQueryWrapper<AiChatMemory>()
                        .eq(AiChatMemory::getSessionId, sessionId)
                        .orderByDesc(AiChatMemory::getCreateTime)
                        .last("LIMIT " + maxRounds)
        );
        if (records.isEmpty()) {
            return records;
        }

        // 检验当前会话是否过期
        LocalDateTime lastActive = records.get(0).getCreateTime();
        int timeout = aiProperties.getChatMemory().getSessionTimeoutSeconds();
        if (lastActive != null && lastActive.plusSeconds(timeout).isBefore(LocalDateTime.now())) {
            // 会话已过期，返回空列表
            return List.of();
        }
        // Reverse to chronological order
        Collections.reverse(records);
        return records;
    }

    @Override
    public boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }
        List<AiChatMemory> records = chatMemoryMapper.selectList(
                new LambdaQueryWrapper<AiChatMemory>()
                        .eq(AiChatMemory::getSessionId, sessionId)
                        .orderByDesc(AiChatMemory::getCreateTime)
                        .last("LIMIT 1")
        );
        if (records.isEmpty()) {
            return false;
        }
        LocalDateTime lastActive = records.get(0).getCreateTime();
        int timeout = aiProperties.getChatMemory().getSessionTimeoutSeconds();
        return !(lastActive != null && lastActive.plusSeconds(timeout).isBefore(LocalDateTime.now()));
    }
}
