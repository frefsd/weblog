package com.blog.utils;

import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 简易滑动窗口限流器，基于 IP 维度。
 */
@Component
public class RateLimiter {

    private final Map<String, Deque<Long>> store = new ConcurrentHashMap<>();

    /**
     * 检查是否允许通过，线程安全。
     *
     * @param key      限流维度（如 IP）
     * @param maxCount  时间窗口内最大请求数
     * @param windowMs  时间窗口（毫秒）
     * @return true 允许通过，false 被限流
     */
    public boolean tryAcquire(String key, int maxCount, long windowMs) {
        Deque<Long> deque = store.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        long now = System.currentTimeMillis();
        long cutoff = now - windowMs;

        while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
            deque.pollFirst();
        }

        if (deque.size() >= maxCount) {
            return false;
        }

        deque.addLast(now);
        return true;
    }

    /** 获取当前窗口内的请求数 */
    public int getCount(String key, long windowMs) {
        Deque<Long> deque = store.get(key);
        if (deque == null) return 0;
        long cutoff = System.currentTimeMillis() - windowMs;
        while (!deque.isEmpty() && deque.peekFirst() < cutoff) {
            deque.pollFirst();
        }
        return deque.size();
    }

    /** 清理某个 key */
    public void reset(String key) {
        store.remove(key);
    }
}
