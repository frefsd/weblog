package com.blog.ai.service;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface ChatService {
    StreamingResponseBody chatStreamText(String sessionId, String question);
    boolean validateSession(String sessionId);
}
