package com.blog.ai.service;

import com.blog.ai.entity.AiChatMemory;

import java.util.List;

public interface ChatMemoryService {
    void saveChat(String sessionId, String userMessage, String aiMessage, String sourcesJson);
    List<AiChatMemory> getRecentChats(String sessionId, int maxRounds);
    List<AiChatMemory> getChatHistory(String sessionId);
    boolean validateSession(String sessionId);
}
