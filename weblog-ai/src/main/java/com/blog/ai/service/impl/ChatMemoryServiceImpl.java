package com.blog.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.ai.config.AiProperties;
import com.blog.ai.entity.AiChatMemory;
import com.blog.ai.mapper.AiChatMemoryMapper;
import com.blog.ai.service.ChatMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMemoryServiceImpl implements ChatMemoryService {

    private final AiChatMemoryMapper chatMemoryMapper;
    private final AiProperties aiProperties;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveChat(String sessionId, String userMessage, String aiMessage, String sourcesJson) {
        AiChatMemory record = new AiChatMemory();
        record.setSessionId(sessionId);
        record.setUserMessage(userMessage);
        record.setAiMessage(aiMessage);
        record.setSources(sourcesJson);
        record.setCreateTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
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
        if (lastActive != null && lastActive.plusSeconds(timeout).isBefore(LocalDateTime.now(ZoneId.of("Asia/Shanghai")))) {
            // 会话已过期，返回空列表
            return List.of();
        }
        // Reverse to chronological order
        Collections.reverse(records);
        return records;
    }

    @Override
    public List<AiChatMemory> getChatHistory(String sessionId) {
        return chatMemoryMapper.selectList(
                new LambdaQueryWrapper<AiChatMemory>()
                        .eq(AiChatMemory::getSessionId, sessionId)
                        .orderByAsc(AiChatMemory::getCreateTime)
        );
    }

    @Override
    public boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            log.debug("validateSession: sessionId为空");
            return false;
        }
        List<AiChatMemory> records = chatMemoryMapper.selectList(
                new LambdaQueryWrapper<AiChatMemory>()
                        .eq(AiChatMemory::getSessionId, sessionId)
                        .orderByDesc(AiChatMemory::getCreateTime)
                        .last("LIMIT 1")
        );
        if (records.isEmpty()) {
            log.debug("validateSession: 未找到记录, sessionId={}", sessionId);
            return false;
        }
        LocalDateTime lastActive = records.get(0).getCreateTime();
        int timeout = aiProperties.getChatMemory().getSessionTimeoutSeconds();
        log.debug("validateSession: 找到记录, sessionId={}, createTime={}, timeout={}s",
                sessionId, lastActive, timeout);
        return !(lastActive != null && lastActive.plusSeconds(timeout).isBefore(LocalDateTime.now(ZoneId.of("Asia/Shanghai"))));
    }
}
