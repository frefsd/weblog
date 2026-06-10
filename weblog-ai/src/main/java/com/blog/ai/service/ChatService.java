package com.blog.ai.service;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;

public interface ChatService {
    StreamingResponseBody chatStreamText(String sessionId, String question);
    boolean validateSession(String sessionId);
    List<Map<String, Object>> getChatHistory(String sessionId);
}
