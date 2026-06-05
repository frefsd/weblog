package com.blog.ai.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {
    SseEmitter chatStream(String sessionId, String question);
    boolean validateSession(String sessionId);
}
